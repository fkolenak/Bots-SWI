package cz.zcu.swi.fkolenak;

import cz.cuni.amis.introspection.java.JProp;
import cz.cuni.amis.pogamut.base.agent.module.comm.PogamutJVMComm;
import cz.cuni.amis.pogamut.base.communication.worldview.event.IWorldEvent;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.AnnotationListenerRegistrator;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.ObjectClassEventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.object.IWorldObject;
import cz.cuni.amis.pogamut.base.communication.worldview.object.event.WorldObjectUpdatedEvent;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.base.utils.math.DistanceUtils;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.TabooSet;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.NavigationState;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004PathAutoFixer;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Configuration;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Rotate;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.*;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;
import cz.cuni.amis.pogamut.ut2004.teamcomm.mina.messages.TCMessageData;
import cz.cuni.amis.pogamut.ut2004.teamcomm.server.UT2004TCServer;
import cz.cuni.amis.pogamut.ut2004.utils.UnrealUtils;
import cz.cuni.amis.utils.Cooldown;
import cz.cuni.amis.utils.exception.PogamutException;
import cz.cuni.amis.utils.flag.FlagListener;
import cz.zcu.swi.fkolenak.communication.classes.RequestHelpDefense;
import cz.zcu.swi.fkolenak.communication.classes.TimeStampedObject;
import cz.zcu.swi.fkolenak.communication.classes.WorldState;
import cz.zcu.swi.fkolenak.communication.events.TCRequestHelpDefense;
import cz.zcu.swi.fkolenak.communication.events.TCShareWorldState;
import cz.zcu.swi.fkolenak.goals.GoalManager;
import cz.zcu.swi.fkolenak.goals.StateReady;
import cz.zcu.swi.fkolenak.goals.low.*;
import cz.zcu.swi.fkolenak.helpers.*;

import javax.vecmath.Vector3d;
import java.util.*;
import java.util.logging.Level;

/**
 * We advise you to use simple: DM-1on1-Albatross map with this example: otherwise NavMesh navigation won't work...
 *
 * @author Jakub Gemrot aka Jimmy
 */
@AgentScoped
public class SmartHunterBot extends UT2004BotTCController {

    private StateReady stateReady;


    private static int _ID = 0;

    protected State state = new State(State.HIGH.GEAR_UP_MINIMAL, State.LOW.NONE);

    protected static int CHANGE_WEAPON_COOLDOWN = 1400;

    // Atributes to continuously navigate
    protected List<NavPoint> currentPath;

    // Remove automatically nodes
    /**
     * Taboo set is working as "black-list", that is you might add some
     * NavPoints to it for a certain time, marking them as "unavailable".
     */
    protected TabooSet<NavPoint> tabooNavPoints;
    /**
     * Path auto fixer watches for navigation failures and if some navigation
     * link is found to be unwalkable, it removes it from underlying navigation
     * graph.
     *
     * Note that UT2004 navigation graphs are some times VERY stupid or contains
     * VERY HARD TO FOLLOW links...
     */
    protected UT2004PathAutoFixer autoFixer;




    protected Premades premades = new Premades();
    private NavigateFunctions fNavigate;
    private Paths paths;

    private Collection<UT2004ItemType> healtItemsTypes = new ArrayList<UT2004ItemType>();
    private Collection<UT2004ItemType> shieldItemsTypes = new ArrayList<UT2004ItemType>();
    private Collection<UT2004ItemType> healItemsTypes = new ArrayList<UT2004ItemType>();

    // Rays
    protected static final String LEFT_GROUND_45 = "leftGround45Ray";
    protected static final String RIGHT_GROUND_45 = "RightGround45Ray";

    @JProp
    protected final int rayLength = (int) (UnrealUtils.CHARACTER_COLLISION_HEIGHT * 3);
    // settings for the rays
    @JProp
    protected boolean fastTrace = true;        // perform only fast trace == we just need true/false information
    @JProp
    protected boolean floorCorrection = false; // provide floor-angle correction for the ray (when the bot is running on the skewed floor, the ray gets rotated to match the skew)
    @JProp
    protected boolean traceActor = false;      // whether the ray should collid with other actors == bots/players as well

    private AutoTraceRay leftGround, rightGround;

    // GOAL manager
    private GoalManager goalManager;
    // GOALS FOR MANAGER
    private GoalStealingEnemyFlag goalStealingEnemyFlag;
    private GoalStealEnemyFlag goalStealEnemyFlag;
    private GoalPickupItems poalPickupItems;
    private GoalPickUpEnemyFlag goalPickUpEnemyFlag;
    private GoalPickUpOurFlag goalPickUpOurFlag;
    private GoalHuntEnemyFlagStealer goalHuntEnemyFlagStealer;

    public static UT2004TCServer tcServer;

    private WorldState worldState;
    /**
     * Here we can modify initialize command for our bot if we want to.
     *
     * @return
     */
    @Override
    public Initialize getInitializeCommand() {
        // TODO change back
        //Initialize i = new Initialize().setName("Hunter-Bot" + getId()).setDesiredSkill(Main.SKILL).setTeam(Main.TEAM);
        Initialize i = new Initialize().setName("Hunter-Bot" + getId()).setDesiredSkill(Main.SKILL).setTeam(getId() % 2);

        incrementID();
        return i;
    }

    public int getId() {
        return _ID;
    }

    private void incrementID() {
        synchronized (this) {
            _ID++;
        }
    }

    public WorldState getWorldState() {
        return worldState;
    }

    @Override
    public void botInitialized(GameInfo gameInfo, ConfigChange currentConfig, InitedMessage init) {
        TamperNavBuilder.removeBadEdges(this);

        // Add taboo and resolver for nodes
        // initialize taboo set where we store temporarily unavailable navpoints
        tabooNavPoints = new TabooSet<NavPoint>(bot);

        // auto-removes wrong navigation links between navpoints
        autoFixer = new UT2004PathAutoFixer(bot, navigation.getPathExecutor(), fwMap, aStar, navBuilder);

        // Set preferences for weapons
        HashMap<UT2004ItemType,Boolean> generalPref = premades.getGeneralPref();
        for(UT2004ItemType key :generalPref.keySet()){
            weaponPrefs.addGeneralPref(key,generalPref.get(key));
        }
        weaponPrefs.newPrefsRange(100).add(UT2004ItemType.FLAK_CANNON, true).add(UT2004ItemType.BIO_RIFLE, true).add(UT2004ItemType.SHIELD_GUN, true);
        weaponPrefs.newPrefsRange(300).add(UT2004ItemType.FLAK_CANNON, true).add(UT2004ItemType.BIO_RIFLE, true);
        weaponPrefs.newPrefsRange(600).add(UT2004ItemType.FLAK_CANNON, true).add(UT2004ItemType.LIGHTNING_GUN, false).add(UT2004ItemType.MINIGUN, true).add(UT2004ItemType.LINK_GUN, true);
        weaponPrefs.newPrefsRange(1800).add(UT2004ItemType.SHOCK_RIFLE, true).add(UT2004ItemType.LIGHTNING_GUN, true).add(UT2004ItemType.MINIGUN, true).add(UT2004ItemType.LINK_GUN, true).add(UT2004ItemType.SNIPER_RIFLE, true).add(UT2004ItemType.ROCKET_LAUNCHER, true).add(UT2004ItemType.FLAK_CANNON, true);
        weaponPrefs.newPrefsRange(10000).add(UT2004ItemType.SNIPER_RIFLE, true).add(UT2004ItemType.SHOCK_RIFLE, true).add(UT2004ItemType.MINIGUN, false).add(UT2004ItemType.LIGHTNING_GUN, true);


        healtItemsTypes = Premades.getHealingTypes();
        shieldItemsTypes = Premades.getShieldTypes();

        healItemsTypes.addAll(healtItemsTypes);
        healItemsTypes.addAll(shieldItemsTypes);

        shoot.setChangeWeaponCooldown(CHANGE_WEAPON_COOLDOWN);

        //setUpRayCasting();
        fNavigate = new NavigateFunctions(this);

        PogamutJVMComm.getInstance().getLog().setLevel(Level.WARNING);
        PogamutJVMComm.getInstance().registerAgent(bot, Constants.COMM_CHANEL);


    }

    //TODO add front ray to know if I can dodge left or right
    private void setUpRayCasting(){

        // 2. create new rays
        raycasting.createRay(LEFT_GROUND_45,  new Vector3d(300, -350, -160), rayLength, fastTrace, floorCorrection, traceActor);
        raycasting.createRay(RIGHT_GROUND_45, new Vector3d(300, 350, -160), rayLength, fastTrace, floorCorrection, traceActor);


        // register listener called when all rays are set up in the UT engine
        raycasting.getAllRaysInitialized().addListener(new FlagListener<Boolean>() {

            public void flagChanged(Boolean changedValue) {
                // once all rays were initialized store the AutoTraceRay objects
                // that will come in response in local variables, it is just
                // for convenience
                leftGround = raycasting.getRay(LEFT_GROUND_45);
                rightGround = raycasting.getRay(RIGHT_GROUND_45);
            }
        });
        // 3. declare that we are not going to setup any other rays, so the 'raycasting' object may know what "all" is
        raycasting.endRayInitSequence();


        // The most important thing is this line that ENABLES AUTO TRACE functionality,
        // without ".setAutoTrace(true)" the AddRay command would be useless as the bot won't get
        // trace-lines feature activated
        getAct().act(new Configuration().setDrawTraceLines(true).setAutoTrace(true));

    }

    /**
     * The bot is initialized in the environment - a physical representation of
     * the bot is present in the game.
     *
     * @param config information about configuration
     * @param init   information about configuration
     */
    @Override
    public void botFirstSpawn(GameInfo gameInfo, ConfigChange config, InitedMessage init, Self self) {
        // Uncomment this to silence Yylex...
        bot.getLogger().getCategory("Yylex").setLevel(Level.OFF);

        // notify the world (i.e., send message to UT2004) that the bot is up and running
        body.getCommunication().sendGlobalTextMessage("Ready for some serious rocket dodging!");
    }

    @Override
    public void beforeFirstLogic() {


        paths = new Paths(this);
        paths.generatePathsToEnemyBase();
        paths.generatePathsToOurBase();
        stateReady = new StateReady(this, fNavigate, paths);

        goalManager = new GoalManager(this);
        worldState = new WorldState();

        goalStealingEnemyFlag = new GoalStealingEnemyFlag(this, fNavigate, paths, state);
        goalStealEnemyFlag = new GoalStealEnemyFlag(this, fNavigate, paths, state);
        poalPickupItems = new GoalPickupItems(this, fNavigate, paths, state);
        goalPickUpEnemyFlag = new GoalPickUpEnemyFlag(this, fNavigate, paths, state, worldState);
        goalPickUpOurFlag = new GoalPickUpOurFlag(this, fNavigate, paths, state, worldState);
        goalHuntEnemyFlagStealer = new GoalHuntEnemyFlagStealer(this, fNavigate, paths, state);

        goalManager.addGoal(goalStealingEnemyFlag);
        goalManager.addGoal(goalStealEnemyFlag);
        goalManager.addGoal(poalPickupItems);
        goalManager.addGoal(goalPickUpEnemyFlag);
        goalManager.addGoal(goalPickUpOurFlag);
        goalManager.addGoal(goalHuntEnemyFlagStealer);



    }


    /**
     * Main method that controls the bot - makes decisions what to do next.
     * <p><p> Notice that the method is empty as this bot is completely
     * event-driven.
     */
    @Override
    public void logic() throws PogamutException {
        shoot();
        updateWorldState();
        needHelp();
        // Go for weapon
        if (state.getCurrentStateHigh() == State.HIGH.GEAR_UP_MINIMAL) {
            obtainGoods();
            debugState();
            return;
        }


        if (state.getCurrentStateHigh() == State.HIGH.READY) {
            goalManager.executeGoalWithHighestPriority();
            //stateReady();
        }

        if (state.getCurrentStateHigh() == State.HIGH.HEAL) {

            if(!getHealth()){
                state.setCurrentStateHigh(State.HIGH.READY);
            }
        }

        shareWorldState();
    }




    //******************************************\\
    //                                          \\
    //          STATES                          \\
    //                                          \\
    //******************************************\\

    private void shoot() {
        // Shoot enemy
        if (canSeeEnemies()) {
            // TODO improve targets
            combat();
        } else {
            // No enemy stop shooting
            stopShooting();
            lookAtEnemyLastKnownPostition();
        }
    }

    private boolean getHealth() {
        if(navigation.isNavigating()){
            if(navigation.getCurrentTargetNavPoint() != null){
                ItemType item = navigation.getCurrentTargetNavPoint().getItemClass();
                if(healtItemsTypes.contains(item) || shieldItemsTypes.contains(item)){
                    return true;
                }
            }
        }
        if(needHealthUrgent()){
            if(navigateTo(getNearestSpawnedItem(healItemsTypes)) != null){
                return true;
            }
        }
        if(needHealth()){
            if(navigateTo(getNearestSpawnedItem(UT2004ItemType.HEALTH_PACK)) != null){
                return true;
            }
            if(navigateTo(getNearestSpawnedItem(UT2004ItemType.SUPER_HEALTH_PACK)) != null){
                return true;
            }
        }
        if(needArmor()){
            if(info.getHighArmor() < 100){
                if( navigateTo(getNearestSpawnedItem(UT2004ItemType.SUPER_SHIELD_PACK))  != null){
                    return true;
                }
            }
            if(info.getLowArmor() < 50){
                if( navigateTo(getNearestSpawnedItem(UT2004ItemType.SHIELD_PACK))  != null){
                    return true;
                }
            }
        }
        return false;
    }


    private boolean checkHealth() {
        if(needHealth()){
            return true;
        }
        return needArmor();
    }


    private void stopShooting(){
        if (info.isShooting()){
            shoot.stopShooting();
        }
    }

    private boolean obtainGoods() {
        if(navigation.isNavigating()){
            return false;
        }

        // #2 Priority if already has one good weapon get armor
        Collection<UT2004ItemType> requiredWeapons = removeLoadedWeapons(premades.getRequiredWeapons());
        if (weaponry.getLoadedRangedWeapons().size() > 1) {
            if (pickUpNearestArmor()) {
                return true;
            }
            state.setCurrentStateHigh(State.HIGH.READY);

            return true;
            //}
        }
        // #1 Priority
        // If no good weapons found
        if (requiredWeapons.size() > 3) {
            if (!pickupNearestWeapon(requiredWeapons)) {
                // pick up some weapon if bot has no better weapon than default
                if(weaponry.getLoadedRangedWeapons().size() == 1){
                    if(pickUpSomeWeapon()){
                        return true;
                    }
                }
            } else {
                return true;
            }
        }
        state.setCurrentStateHigh(State.HIGH.READY);
        return true;
    }






    /**
     * BEHAVIOR - try to collect 'requiredWeapons'; start with the nearest first.
     * @return success
     */
    private boolean pickupNearestWeapon(Collection<UT2004ItemType> requiredWeapons) {
        Set<Item> nearest = getNearestSpawnedItems(requiredWeapons);

        Item target = DistanceUtils.getNearest(nearest, info.getNearestNavPoint(),
                new DistanceUtils.IGetDistance<Item>() {
                    @Override
                    public double getDistance(final Item object, ILocated target) {
                        return fwMap.getDistance(object.getNavPoint(), info.getNearestNavPoint());
                    }
                });
        Item closestWeapon = getNearestSpawnedWeapon();

        if (target != null && closestWeapon != null) {
            double distanceBetweenItem1 = fwMap.getDistance(target.getNavPoint(), info.getNearestNavPoint());
            double distanceBetweenItem2 = fwMap.getDistance(closestWeapon.getNavPoint(), info.getNearestNavPoint());
            if (distanceBetweenItem1 < distanceBetweenItem2) {
                if (Constants.DEBUG) {
                    getLog().info("Navigate:" + target.getType().getName());
                    //LetKnow.debugGoal(this, "Navigate", target.getType().getName());
                }
                navigateTo(target);
                return true;
            } else {
                if (Constants.DEBUG) {
                    getLog().info("Navigate:" + closestWeapon.getType().getName());
                    //LetKnow.debugGoal(this, "Navigate", closestWeapon.getType().getName());
                }
                navigateTo(closestWeapon);
                return true;
            }


        } else if (target != null) {
            //LetKnow.debugGoal(this, "Navigate", target.getType().getName() + " | " + (int) (fwMap.getDistance(target.getNavPoint(), info.getNearestNavPoint())));
            navigateTo(target);
        } else if (closestWeapon != null) {
            //LetKnow.debugGoal(this, "Navigate", closestWeapon.getType().getName() + " | " + (int) (fwMap.getDistance(closestWeapon.getNavPoint(), info.getNearestNavPoint())));
            navigateTo(closestWeapon);
        }


        return false;
    }


    public boolean pickUpSomeWeapon() {
        if (!weaponry.hasLoadedWeapon(UT2004ItemType.LIGHTNING_GUN)) {
            if (fNavigate.navigateTo(UT2004ItemType.LIGHTNING_GUN)) return true;
        }
        if (!weaponry.hasLoadedWeapon(UT2004ItemType.ROCKET_LAUNCHER)) {
            if (fNavigate.navigateTo(UT2004ItemType.ROCKET_LAUNCHER)) return true;
        }
        if (!weaponry.hasLoadedWeapon(UT2004ItemType.ASSAULT_RIFLE)) {
            if (fNavigate.navigateTo(UT2004ItemType.ASSAULT_RIFLE)) return true;
        }
        if (!weaponry.hasLoadedWeapon(UT2004ItemType.BIO_RIFLE)) {
            if (fNavigate.navigateTo(UT2004ItemType.BIO_RIFLE)) return true;
        }
        return false;
    }

    /**
     * Checks if bot has this type of a weapon and has ammo for it
     * @param requiredWeapons required weapons
     * @return required weapons without loaded weapons
     */
    private Collection<UT2004ItemType> removeLoadedWeapons(Collection<UT2004ItemType> requiredWeapons){
        Collection<UT2004ItemType> weaponsToGet = new ArrayList<UT2004ItemType>();
        for (UT2004ItemType item : requiredWeapons){
            if(!weaponry.hasLoadedWeapon(item)){
                weaponsToGet.add(item);
            }
        }
        return weaponsToGet;
    }


    public boolean needHealth() {
        return info.getHealth() < 76;
    }

    public boolean needArmor() {
        return info.getArmor() < 40;
    }

    public boolean needHealthUrgent() {
        return info.getHealth() < 40 || info.getHealth() + info.getArmor() < 40;
    }


    public boolean pickUpNearestArmor() {
        Item nearestArmor = getNearestSpawnedItem(UT2004ItemType.SHIELD_PACK);
        Item nearestSuperArmor = getNearestSpawnedItem(UT2004ItemType.SUPER_SHIELD_PACK);

        if (nearestArmor == null && nearestSuperArmor == null) {
            return false;
        }
        Item navigateToItem;

        double distanceToEnemy, distanceToUs, distanceToBot;

        if (nearestSuperArmor == null) {
            // Filter armor that is far from our base
            navigateToItem = nearestArmor;
        } else if (nearestArmor == null) {
            navigateToItem = nearestSuperArmor;
        } else {
            distanceToBot = fwMap.getDistance(nearestArmor.getNavPoint(), info.getNearestNavPoint());
            double distanceToBot2 = fwMap.getDistance(nearestSuperArmor.getNavPoint(), info.getNearestNavPoint());
            if (distanceToBot < distanceToBot2) {
                navigateToItem = nearestArmor;
            } else {
                navigateToItem = nearestSuperArmor;
            }
        }

        distanceToEnemy = fwMap.getDistance(navigateToItem.getNavPoint(), ctf.getEnemyBase());
        distanceToUs = fwMap.getDistance(navigateToItem.getNavPoint(), ctf.getOurBase());
        distanceToBot = fwMap.getDistance(navigateToItem.getNavPoint(), info.getNearestNavPoint());


        if (distanceToUs < distanceToEnemy) {
            navigateTo(navigateToItem);
            return true;
        } else if (distanceToBot < 8000 || distanceToBot < distanceToEnemy) {
            navigateTo(navigateToItem);
            return true;
        }


        return false;
    }


    private void needHelp() {

        double distance = getFwMap().getDistance(getNavPoints().getNearestNavPoint(getInfo().getLocation()), getNavPoints().getNearestNavPoint(getCTF().getOurBase()));
        if (distance < 1500 && players.canSeeEnemies()) {
            Player enemy = players.getNearestVisibleEnemy();
            distance = getCTF().getOurBase().getLocation().getDistance(enemy.getLocation());
            if (distance < 1500) {
                requestHelp(enemy);
            }
        }
    }


    //******************************************\\
    //                                          \\
    //          NAVIGATION FUNCTIONS            \\
    //                                          \\
    //******************************************\\

    private List<NavPoint> navigateTo(Item target) {
        currentPath = fNavigate.navigateTo(target);
        return currentPath;
    }

    private List<NavPoint> navigateTo(NavPoint target) {
        currentPath = fNavigate.navigateTo(target);
        return currentPath;
    }




    /**
     * Translates 'types' to the set of "nearest spawned items" of those 'types'.
     * @param types
     * @return
     */
    public Set<Item> getNearestSpawnedItems(Collection<UT2004ItemType> types) {
        Set<Item> result = new HashSet<Item>();
        for (UT2004ItemType type : types) {
            Item n = getNearestSpawnedItem(type);
            if (n != null) {
                result.add(n);
            }
        }
        return result;
    }

    /**
     * Translates 'types' to the set of "nearest spawned items" of those 'types'.
     *
     * @return
     */
    public Item getNearestSpawnedWeapon() {
        return getNearestSpawnedItem(ItemType.Category.WEAPON);
    }

    /**
     * Returns the nearest spawned item of 'type'.
     * @param type
     * @return
     */
    public Item getNearestSpawnedItem(UT2004ItemType type) {
        final NavPoint nearestNavPoint = info.getNearestNavPoint();
        Item nearest = DistanceUtils.getNearest(
                items.getSpawnedItems(type).values(),
                info.getNearestNavPoint(),
                new DistanceUtils.IGetDistance<Item>() {
                    @Override
                    public double getDistance(Item object, ILocated target) {
                        return fwMap.getDistance(object.getNavPoint(), nearestNavPoint);
                    }

                });
        return nearest;
    }

    /**
     * Returns the nearest spawned item of 'type'.
     *
     * @param type
     * @return
     */
    public Item getNearestSpawnedItem(ItemType.Category type) {
        final NavPoint nearestNavPoint = info.getNearestNavPoint();
        return DistanceUtils.getNearest(
                items.getSpawnedItems(type).values(),
                info.getNearestNavPoint(),
                new DistanceUtils.IGetDistance<Item>() {
                    @Override
                    public double getDistance(Item object, ILocated target) {
                        return fwMap.getDistance(object.getNavPoint(), nearestNavPoint);
                    }

                });
    }
    /**
     * Returns the nearest spawned item of 'type'.
     * @param types
     * @return
     */
    public Item getNearestSpawnedItem(Collection<UT2004ItemType> types) {
        Set<Item> result = getNearestSpawnedItems(types);

        Item target = DistanceUtils.getNearest(result, info.getNearestNavPoint(),
                new DistanceUtils.IGetDistance<Item>() {
                    @Override
                    public double getDistance(final Item object, ILocated target) {
                        return fwMap.getDistance(object.getNavPoint(), info.getNearestNavPoint());
                    }
                });
        if (target == null) {
            //log.severe("No item to navigate to! requiredWeapons.size() = " + requiredWeapons.size());
            return null;
        }
        return target;
    }

    //******************************************\\
    //                                          \\
    //              COMBAT FUNCTIONS            \\
    //                                          \\
    //******************************************\\


    private boolean combat() {
        if (!canSeeEnemies()) {
            return false;
        }
        Player enemy = players.getNearestVisibleEnemy();
        navigation.setFocus(enemy.getLocation());
        strafe();
        //navigation.navigate(players.getNearestVisibleEnemy());
        shoot.shoot(weaponPrefs, enemy);
        if(needHealthUrgent()) {
            state.setCurrentStateHigh(State.HIGH.HEAL);
        }
        return true;
    }

    private void lookAtEnemyLastKnownPostition() {
        Player enemy = players.getNearestEnemy(500);

        if (enemy != null){
            move.turnTo(enemy);
        } else {
            move.turnHorizontal(32000);
        }
    }

    private boolean canSeeEnemies() {
        if( !players.canSeeEnemies()) {
            if (players.getNearestEnemy(350) == null) {
                navigation.setFocus(null);
            }
            return false;
        }
        return true;
    }

    public boolean randomDodgeMove() {
        int dodgeMoveNumber = random.nextInt(4) + 1;
        if(dodgeMoveNumber == 1){
            move.jump();
        } else if(dodgeMoveNumber == 2){
            move.doubleJump();
        } else if(dodgeMoveNumber == 3) {
            move.dodge(info.getNearestItem().getLocation(),false);
        } else if(dodgeMoveNumber == 4){
            move.dodge(info.getLocation().addXYZ(random.nextInt(100)-50,random.nextInt(100)-50,0),false);
        }

        return true;
    }

    /**
     * Strafe while shooting
     * @return success
     */
    private boolean strafe() {
        if(!navigation.isNavigating()){
            return false;
        }
        if(!canSeeEnemies()){
            return false;
        }
        Player enemy = players.getNearestVisibleEnemy();
        navigation.setFocus(enemy.getLocation());
        shoot.shoot(weaponPrefs,enemy);

        move.strafeLeft(250 , enemy);

        move.strafeRight(250 , enemy);

        return true;
    }



    /**
     * Called each time our bot die. Good for reseting all bot state dependent
     * variables.
     *
     * @param event
     */
    @Override
    public void botKilled(BotKilled event) {
        state.setCurrentStateHigh(State.HIGH.GEAR_UP_MINIMAL);
        state.setCurrentStateLow(State.LOW.NONE);

        currentPath = null;
        log.info(bot.getBotName() + " -> KILLS: " + info.getKills() + " DEATHS: " + (info.getDeaths()+1));
    }

    //******************************************\\
    //                                          \\
    //              LISTENERS                   \\
    //                                          \\
    //******************************************\\


    /**
     * Path executor has changed its state (note that {@link UT2004BotModuleController}
     * is internally used by
     * {@link UT2004BotModuleController#getNavigation()} as well!).
     *
     * @param state
     */
    protected void pathExecutorStateChange(NavigationState state) {
//        switch (state) {
//
//
//            /** For some reason path to target could not be computed. */
//            case PATH_COMPUTATION_FAILED:
//                log.info("FLAG PATH_COMPUTATION_FAILED");
//
//                // if path computation fails to whatever reason, just try another navpoint
//                // taboo bad navpoint for 3 minutes
//                tabooNodes.add(navigation.getCurrentTargetNavPoint());
//                break;
//
//            /** Navigation reached target (and is stopped). */
//            case TARGET_REACHED:
//                // Check if path exists and has nodes
//                if(currentPath != null){
//                    // Check if the state wasn't changed
//                    if(sendTo == null){
//                        //sendTo = currentPath.get().get(0);
//                        if(currentPath.get().size() > 1){
//                           // afterSendTo = currentPath.get(0);
//                        } else {
//                            afterSendTo = null;
//                        }
//                    }
//                    else {
//                        log.info("Changing to next navpoint");
//                        sendTo = afterSendTo;
//                        // Check if there is another node to go to
//                        /*if(currentPath.indexOf(sendTo) + 1 < currentPath.size()){
//                            afterSendTo = currentPath.get(1 + currentPath.indexOf(sendTo));
//                            log.info("Obtaining new navpoint");
//                        } else {
//                            log.info("Getting nearest item");
//                            afterSendTo = null;
//                        }*/
//                    }
//
//                } else {
//                    sendTo = null;
//                    afterSendTo = null;
//                }
//                break;
//
//            /** The bot is stuck for some reason when attempting to reach the target */
//            case STUCK:
//                // the bot has stuck! ... target nav point is unavailable currently
//                tabooNavPoints.add(navigation .getCurrentTargetNavPoint(), 60);
//                tabooNodes.add(navigation.getCurrentTargetNavPoint());
//                break;
//
//            /** Navigation was stopped for some reason. */
//            case STOPPED:
//                log.info("FLAG STOPPED");
//
//                // path execution has stopped
//                //targetNavPoint = null;
//                break;
//
//            /** Navigation is working and running somewhere. */
//            case NAVIGATING:
//                log.info("FLAG NAVIGATING");
//                if(sendTo != null){
//                    if(afterSendTo != null){
//                        if(sendTo.equals(navigation.getCurrentTargetNavPoint())){
//                            if(navigation.isNavigating()) {
//                                //navigation.setContinueTo(afterSendTo);
//                            }
//                        }
//                    }
//                }
//        }
    }

    /**
     * If hit rotate to see enemy
     * @param event
     */
    @EventListener(eventClass = BotDamaged.class)
    protected void botDamagedListener  (BotDamaged event) {
        /*if (navigation.isNavigating()) {
            navigation.getCurrentTarget();
            //navigation.stopNavigation();
        }*/
        randomDodgeMove();
        if(!canSeeEnemies()){
            getAct().act(new Rotate().setAmount(32000));
        } else {
            if(navigation.isNavigating()){
                navigation.getCurrentTarget();
                move.strafeTo(navigation.getCurrentTarget(), players.getNearestVisibleEnemy().getLocation());
            }
        }
    }

    /**
     * Listener called when someone/something bumps into the bot. The bot
     * responds by moving in the opposite direction than the bump come from.
     *
     * <p><p>
     * We're using {@link EventListener} here that is registered by the {@link AnnotationListenerRegistrator}
     * to listen for {@link Bumped} events.
     *
     * <p><p>
     * Notice that {@link Bumped} is {@link IWorldEvent} only, it's not {@link IWorldObject},
     * thus we're using {@link EventListener}.
     */
    @ObjectClassEventListener(objectClass = IncomingProjectile.class, eventClass = WorldObjectUpdatedEvent.class)
    protected void incomingProjectile(WorldObjectUpdatedEvent<IncomingProjectile> event) {
        if(!event.getObject().getType().equals("XWeapons.RocketProj") || !event.getObject().getType().equals("XWeapons.ShockProjectile")){
            return;
        }
        if(event.getObject().getLocation().getDistance(info.getLocation()) < 750){

           //move.doubleJump();
            if(random.nextDouble() < 0.5) {
                move.dodgeLeft(info.getLocation().add(info.getRotation().toLocation()), false);
            } else {
                move.dodgeRight(info.getLocation().add(info.getRotation().toLocation()), false);
            }
        }
    }

    /**
     * If hit rotate to see enemy
     * @param event
     */
    @EventListener(eventClass = AddInventoryMsg .class)
    protected void addInventoryListener  (AddInventoryMsg event) {
        UT2004ItemType pickedUpItem = (UT2004ItemType) event.getPickupType();
        if(ItemType.Category.WEAPON != pickedUpItem.getCategory()) {
            return;
        }

        if(pickedUpItem.equals(UT2004ItemType.ASSAULT_RIFLE)){
            return;
        }
        if(weaponry.getCurrentWeapon() == null){
            return;
        }
        if(weaponry.getCurrentWeapon().getType().equals(UT2004ItemType.ASSAULT_RIFLE)){
            weaponry.changeWeapon(pickedUpItem);
        }
    }


    @EventListener(eventClass = AdrenalineGained.class)
    protected void adrenalineGainedListener  (AdrenalineGained event) {
       if(info.isAdrenalineSufficient()){
           if(info.getHealth() > 80){
               if(canSeeEnemies()){
                   combo.performBerserk();
               }
           }
           if(info.getHealth() > 40 && info.getHealth() <= 80){
               combo.performDefensive();
           }
       }
    }
    @EventListener(eventClass = Bumped .class)
    protected void bumpedListener  (Bumped event) {
        for (UnrealId id : players.getEnemies().keySet()) {
            if (id.equals(event.getId())) {
                move.dodgeBack(info.getLocation().addXYZ(1, 0, 0), false);
            }
        }
        for (UnrealId id : players.getFriends().keySet()) {
            if (id.equals(event.getId())) {
                move.strafeLeft(200);
                break;
            }
        }
        //randomDodgeMove();
    }


    public void debugState() {

        // LetKnow.debugState(this, state);
    }

    /*************COMM ****************/


    public void sendTeamMessage(TCMessageData message) {
        tcClient.sendToTeamOthers(message);
    }


    //          WORLD STATE
    @EventListener(eventClass = TCShareWorldState.class)
    public void getWorldStatePacket(TCShareWorldState event) {
        this.worldState.updateWorldState(event.getWorldState());
    }

    public void updateWorldState() {
        long timestamp = System.currentTimeMillis();
        if (this.ctf.isEnemyFlagHome()) {
            this.worldState.updateEnemyFlag(this.ctf.getEnemyBase().getLocation(), timestamp);
        } else if (this.ctf.isBotCarryingEnemyFlag()) {
            this.worldState.updateEnemyFlag(this.info.getLocation(), timestamp);
        } else if (this.ctf.getEnemyFlag().isVisible()) {
            this.worldState.updateEnemyFlag(this.ctf.getEnemyFlag().getLocation(), timestamp);
        }

        if (this.ctf.isOurFlagHome()) {
            this.worldState.updateOurFlag(this.ctf.getOurBase().getLocation(), timestamp);
        } else if (this.ctf.getOurFlag().isVisible()) {
            this.worldState.updateOurFlag(this.ctf.getOurFlag().getLocation(), timestamp);
        }
    }

    public void shareWorldState() {
        this.sendTeamMessage(new TCShareWorldState(this.worldState));
    }
    //=================
    //  Request Help
    //=================

    private TimeStampedObject<RequestHelpDefense> helpRequest;

    public void setHelpRequest(TimeStampedObject<RequestHelpDefense> helpRequest) {
        this.helpRequest = helpRequest;
    }

    public RequestHelpDefense getHelpRequest() {
        return helpRequest.object;
    }


    private Cooldown politenes = new Cooldown(1500);

    private void requestHelp(Player enemy) {
        if (politenes.tryUse()) {
            boolean urgent = false;

            if (getCTF().isBotCarryingEnemyFlag()) urgent = true;
            else if (needHealthUrgent()) urgent = true;
            else if (this.state.getCurrentStateHigh() != State.HIGH.READY) urgent = true;

            RequestHelpDefense request = new RequestHelpDefense(enemy, info.getLocation(), urgent);
            this.sendTeamMessage(new TCRequestHelpDefense(new TimeStampedObject<RequestHelpDefense>(request, System.currentTimeMillis())));
        }
    }

    //          WORLD STATE
    @EventListener(eventClass = TCRequestHelpDefense.class)
    public void getHelpRequest(TCRequestHelpDefense event) {
        if (event.getHelpDefense().timestamp > helpRequest.timestamp) {
            helpRequest = event.getHelpDefense();
        }
    }


}
