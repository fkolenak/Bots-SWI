package cz.zcu.swi.fkolenak;

import java.util.*;
import java.util.logging.Level;

import cz.cuni.amis.pathfinding.alg.astar.AStarResult;
import cz.cuni.amis.pogamut.base.agent.navigation.IPathExecutorState;
import cz.cuni.amis.pogamut.base.agent.navigation.IPathFuture;
import cz.cuni.amis.pogamut.base.agent.navigation.impl.PrecomputedPathFuture;
import cz.cuni.amis.pogamut.base.communication.worldview.event.IWorldEvent;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.AnnotationListenerRegistrator;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.ObjectClassEventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.object.IWorldObject;
import cz.cuni.amis.pogamut.base.communication.worldview.object.event.WorldObjectUpdatedEvent;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.base.utils.math.DistanceUtils;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.base3d.worldview.object.Velocity;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.TabooSet;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.NavigationState;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004PathAutoFixer;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.PlayAnimation;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Rotate;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.*;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import cz.zcu.swi.fkolenak.helpers.Premades;
import cz.zcu.swi.fkolenak.helpers.State;

/**
 * We advise you to use simple: DM-1on1-Albatross map with this example: otherwise NavMesh navigation won't work...
 *
 * @author Jakub Gemrot aka Jimmy
 */
@AgentScoped
public class SmartHunterBot extends UT2004BotModuleController {
    protected static final int NUM_OF_BOTS = 1;
    private static final boolean DEBUG = true;

    protected static int skill = 5;
    protected State state = State.GEAR_UP_MINIMAL;


    protected Item navigatingToItem;

    // Atributes to continuously navigate - Enable strafing
    protected IPathFuture currentPath;

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


    protected List<NavPoint> tabooNodes = new ArrayList<NavPoint>();


    protected Premades premades = new Premades();


    private Collection<UT2004ItemType> healtItemsTypes = new ArrayList();
    private Collection<UT2004ItemType> shieldItemsTypes = new ArrayList();
    private Collection<UT2004ItemType> healItemsTypes = new ArrayList();

    /**
     * This method is called when the bot is started either from IDE or from
     * command line.
     *
     * @param args
     */
    public static void main(String args[]) throws PogamutException {

        // wrapped logic for bots executions, suitable to run single bot in single JVM
        new UT2004BotRunner(SmartHunterBot.class, "HunterBot").setMain(true).startAgents(NUM_OF_BOTS);
    }

    /**
     * Here we can modify initialize command for our bot if we want to.
     *
     * @return
     */
    @Override
    public Initialize getInitializeCommand() {
        return new Initialize().setName("Hunter-Bot").setDesiredSkill(skill);
    }

    @Override
    public void botInitialized(GameInfo gameInfo, ConfigChange currentConfig, InitedMessage init) {
        if(game.getMapName().equals("DM-1on1-Albatross")){
            navBuilder.removeEdge("PathNode35","JumpSpot11");
            navBuilder.removeEdge("LiftCenter1","LiftExit4");
            navBuilder.removeEdge("PathNode88","JumpSpot8");
            navBuilder.removeEdge("PathNode27","JumpSpot12");
        }

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


        healtItemsTypes.add(UT2004ItemType.SUPER_HEALTH_PACK);
        healtItemsTypes.add(UT2004ItemType.HEALTH_PACK);
        healtItemsTypes.add(UT2004ItemType.MINI_HEALTH_PACK);
        shieldItemsTypes.add(UT2004ItemType.SHIELD_PACK);
        shieldItemsTypes.add(UT2004ItemType.SUPER_SHIELD_PACK);

        healItemsTypes.addAll(healtItemsTypes);
        healItemsTypes.addAll(shieldItemsTypes);

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
        super.beforeFirstLogic();
    }


    /**
     * Main method that controls the bot - makes decisions what to do next.
     * <p><p> Notice that the method is empty as this bot is completely
     * event-driven.
     */
    @Override
    public void logic() throws PogamutException {
        shoot();
        // Go for weapon
        if(state == State.GEAR_UP_MINIMAL){
            debugGoal("STATE GEAR_UP");
            obtainGoods();
            return;
        }


        if(state == State.READY) {
            debugGoal("STATE READY");

            if(checkHealth()){
                state = State.HEAL;
            } else {
                Player enemy = players.getNearestVisibleEnemy();
                if(enemy != null){
                    navigation.navigate(enemy);
                }
            }

        }

        if(state == State.HEAL){
            debugGoal("STATE HEAL");

            if(!getHealth()){
                state = State.READY;
            }
        }
        if( pickUpItems()){
            return;
        }

    }

    //******************************************\\
    //                                          \\
    //          STATES                          \\
    //                                          \\
    //******************************************\\











    private void shoot() {
        // Shoot enemy
        if (canSeeEnemies()) {
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
        if(needArmor()){
            return true;
        }
        return false;
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
        if(requiredWeapons.size() == premades.getRequiredWeapons().size() - 1 && info.getArmor() < 30){
            if(pickUpNearestArmor()){
               return true;
            }
        }
        // #1 Priority
        // If no good weapons found
        if(requiredWeapons.size() > 2){
            if(!pickupNearestGoodWeapon(requiredWeapons)){
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
        state = State.READY;
        return true;
    }






    /**
     * BEHAVIOR - try to collect 'requiredWeapons'; start with the nearest first.
     * @return success
     */
    private boolean pickupNearestGoodWeapon(Collection<UT2004ItemType> requiredWeapons) {
        Set<Item> nearest = getNearestSpawnedItems(requiredWeapons);

        Item target = DistanceUtils.getNearest(nearest, info.getNearestNavPoint(),
                new DistanceUtils.IGetDistance<Item>() {
                    @Override
                    public double getDistance(final Item object, ILocated target) {
                        return fwMap.getDistance(object.getNavPoint(), info.getNearestNavPoint());
                    }
                });
        if (target == null) {
            //log.severe("No item to navigate to! requiredWeapons.size() = " + requiredWeapons.size());
            return false;
        }
        debugGoal("C:" + target.getType().getName() + " | " + (int)(fwMap.getDistance(target.getNavPoint(), info.getNearestNavPoint())));

        navigatingToItem = target;
        //navigation.navigate(target);
        navigateTo(target);
        return true;
    }


    private boolean pickUpSomeWeapon() {
        if (!weaponry.hasLoadedWeapon(UT2004ItemType.LIGHTNING_GUN)) {
            if (navigateTo(UT2004ItemType.LIGHTNING_GUN)) return true;
        }
        if (!weaponry.hasLoadedWeapon(UT2004ItemType.ROCKET_LAUNCHER)) {
            if (navigateTo(UT2004ItemType.ROCKET_LAUNCHER)) return true;
        }
        if (!weaponry.hasLoadedWeapon(UT2004ItemType.ASSAULT_RIFLE)) {
            if (navigateTo(UT2004ItemType.ASSAULT_RIFLE)) return true;
        }
        if (!weaponry.hasLoadedWeapon(UT2004ItemType.BIO_RIFLE)) {
            if (navigateTo(UT2004ItemType.BIO_RIFLE)) return true;
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



    private boolean pickUpItems() {


        if(weaponry.getLoadedWeapons().size() < 2){
            state = State.GEAR_UP_MINIMAL;
            return true;
        }
        if(navigation.isNavigating()){
            return false;
        }

        if(needArmor()){
            if(pickUpNearestArmor()){
                return true;
            }
        }
        if (needHealthUrgent()) {
            if (pickUpNearestHealth()){
                return true;
            }
        }
        if (pickUpSomeWeapon()) {
            return true;
        }

        if(pickUpSomeItem()){
            return true;
        }

        return false;
    }

    private boolean pickUpSomeItem() {
        Collection<UT2004ItemType> wanted = new ArrayList();
        wanted.add(UT2004ItemType.SUPER_HEALTH_PACK);
        wanted.add(UT2004ItemType.MINI_HEALTH_PACK);
        wanted.add(UT2004ItemType.SHIELD_PACK);
        wanted.add(UT2004ItemType.SUPER_SHIELD_PACK);

        Set<Item> items = getNearestSpawnedItems(wanted);
        if(items != null && items.size() > 0){
            navigateTo((Item) items.toArray()[random.nextInt(items.size())]);
            return true;
        } else {
            navigateTo(navPoints.getRandomNavPoint());
            return false;
        }

    }

    private boolean needHealth(){
        return info.getHealth() < 76;
    }

    private boolean needArmor(){
        return info.getArmor() < 40;
    }

    private boolean needHealthUrgent() {
        return info.getHealth() < 40 || info.getHealth() + info.getArmor() < 40;
    }

    private boolean pickUpNearestHealth() {
        Item nearestHealth = getNearestSpawnedItem(UT2004ItemType.HEALTH_PACK);
        if(nearestHealth == null){
            return false;
        }
        navigateTo(nearestHealth);
        state = State.READY;
        return true;
    }

    private boolean pickUpNearestArmor() {
        Item nearestArmor = getNearestSpawnedItem(UT2004ItemType.SHIELD_PACK);
        if(nearestArmor == null){
            return false;
        }
        navigateTo(nearestArmor);
        return true;
    }

    //******************************************\\
    //                                          \\
    //          NAVIGATION FUNCTIONS            \\
    //                                          \\
    //******************************************\\

    private IPathFuture navigateTo(Item target){
        if(target == null){
            return null;
        }

        NavPoint playerPosition = info.getNearestNavPoint();
        NavPoint targetNavPoint = target.getNavPoint();

        IPathFuture resultPath = generatePath(playerPosition,targetNavPoint);
        currentPath = resultPath;
        navigation.navigate(resultPath);
        return currentPath;
    }

    private IPathFuture navigateTo(NavPoint target){
        if(target == null){
            return null;
        }

        NavPoint playerPosition = info.getNearestNavPoint();

        IPathFuture resultPath = generatePath(playerPosition,target);
        currentPath = resultPath;
        navigation.navigate(resultPath);
        return currentPath;
    }

    /**
     * Navigate to the item based on type
     * @param type item type to navigate to
     * @return isNavigating?
     */
    private boolean navigateTo(UT2004ItemType type) {
        if (navigation.isNavigatingToItem() && navigation.getCurrentTargetItem().getType() == type) return true;
        Item item = fwMap.getNearestItem(items.getSpawnedItems(type).values(), navPoints.getNearestNavPoint());

        if (item != null) {
            bot.getBotName().setInfo("To", item.getType().getName());
            navigateTo(item);
        } else {
            bot.getBotName().setInfo("No item to run to.");
            return false;
        }
        return true;
    }

    private IPathFuture<NavPoint> generatePath(NavPoint from, NavPoint to) {


        AStarResult<NavPoint> result = aStar.findPath(from, to);

        return  new PrecomputedPathFuture<NavPoint>(from, to, result.getPath());
    }

    /**
     * Translates 'types' to the set of "nearest spawned items" of those 'types'.
     * @param types
     * @return
     */
    private Set<Item> getNearestSpawnedItems(Collection<UT2004ItemType> types) {
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
     * Returns the nearest spawned item of 'type'.
     * @param type
     * @return
     */
    private Item getNearestSpawnedItem(UT2004ItemType type) {
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
     * @param types
     * @return
     */
    private Item getNearestSpawnedItem(Collection<UT2004ItemType> types) {
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
            state = State.HEAL;
        }
        return true;
    }

    private void lookAtEnemyLastKnownPostition() {
        Player enemy = players.getNearestEnemy(500);

        if (enemy != null){
            move.turnTo(enemy);
        } else {
            move.turnHorizontal(20000);
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

    private boolean randomDodgeMove() {
        int dodgeMoveNumber = random.nextInt(4) + 1;
        if(dodgeMoveNumber == 1){
            move.jump();
        } else if(dodgeMoveNumber == 2){
            move.doubleJump();
        } else if(dodgeMoveNumber == 3){
            move.dodge(info.getNearestItem().getLocation(),true);
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

        //move.strafeAlong(navigation.getCurrentTarget(),navigation.ge);
        //move.turnTo(enemy);
        //move.strafeTo(navigation.getCurrentTarget(),players.getNearestVisibleEnemy());
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
        state = State.GEAR_UP_MINIMAL;
        navigatingToItem = null;
        currentPath = null;
        log.info(info.getKills() + " " + (info.getDeaths()+1));
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


    @EventListener(eventClass = HearNoise.class)
    protected void hearNoise (HearNoise event) {
        //move.strafeTo(event.getRotation().toLocation(),event.getSource());
        //move.turnTo(event.getRotation().toLocation());
        // TODO not usefull now
    }


    ///////////////
    // STATE HIT //
    ///////////////

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
            debugGoal("Cant See you");
            getAct().act(new Rotate().setAmount(32000));
        } else {
            if(navigation.isNavigating()){
                debugGoal("I see you, strafing");
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


    @EventListener(eventClass = AdrenalineGained .class)
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
        randomDodgeMove();
    }

    /**
     * Displays info tag on the bot.
     * @param goal
     */
    private void debugGoal(String goal) {
        if(DEBUG)
            bot.getBotName().setInfo(goal);
    }

}
