package com.mycompany.navbot;

import java.util.logging.Level;

import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Rotate;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.*;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;

/**
 * We advise you to use simple: DM-1on1-Albatross map with this example: otherwise NavMesh navigation won't work...
 *
 * @author Jakub Gemrot aka Jimmy
 */
@AgentScoped
public class NavBot extends UT2004BotModuleController {


    /**
     * This method is called when the bot is started either from IDE or from
     * command line.
     *
     * @param args
     */
    public static void main(String args[]) throws PogamutException {

        // wrapped logic for bots executions, suitable to run single bot in single JVM
        new UT2004BotRunner(NavBot.class, "SimpleBot").setMain(true).startAgents(1);
    }

    /**
     * Here we can modify initialize command for our bot if we want to.
     *
     * @return
     */
    @Override
    public Initialize getInitializeCommand() {
        return new Initialize().setName("Hunter-Bot").setDesiredSkill(5);
    }

    @Override
    public void botInitialized(GameInfo gameInfo, ConfigChange currentConfig, InitedMessage init) {
        weaponPrefs.addGeneralPref(UT2004ItemType.LIGHTNING_GUN, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.MINIGUN, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.LINK_GUN, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.LIGHTNING_GUN, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.FLAK_CANNON, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.ROCKET_LAUNCHER, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.ASSAULT_RIFLE, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.SHIELD_GUN, false);
        weaponPrefs.addGeneralPref(UT2004ItemType.BIO_RIFLE, true);

        weaponPrefs.newPrefsRange(500).add(UT2004ItemType.FLAK_CANNON, true)
                .add(UT2004ItemType.LINK_GUN, true);

        weaponPrefs.newPrefsRange(1000).add(UT2004ItemType.MINIGUN, true)
                .add(UT2004ItemType.LINK_GUN, false);

        weaponPrefs.newPrefsRange(10000).add(UT2004ItemType.LIGHTNING_GUN, true)
                .add(UT2004ItemType.SHOCK_RIFLE, true);

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

    /**
     * Main method that controls the bot - makes decisions what to do next.
     * <p><p> Notice that the method is empty as this bot is completely
     * event-driven.
     */
    @Override
    public void logic() throws PogamutException {


        if (canSeeEnemies()) {
            if (combat()) {
                return;
            }
        } else {
            if (info.isShooting()) {
                shoot.stopShooting();
            }
        }
        // 3) are you being shot? 	-> go to HIT (turn around - try to find your enemy)
        if (!canSeeEnemies() && senses.isBeingDamaged()) {
            this.stateHit();
            return;
        }


        pickUpItems();
    }

    ///////////////
    // STATE HIT //
    ///////////////
    protected void stateHit() {
        //log.info("Decision is: HIT");
        bot.getBotName().setInfo("HIT");
        if (navigation.isNavigating()) {
            navigation.stopNavigation();
        }
        getAct().act(new Rotate().setAmount(32000));
    }

    private boolean pickUpItems() {
        if (hasDecentWeapon()) {

        }

        if (needHealthUrgent()) {
            if (pickUpNearestHealth()) return true;
        }
        if (pickUpSomeWeapon()) {
            return true;
        }
        if (pickUpGoodItem()) {
            return true;
        }


        return false;
    }

    private boolean hasDecentWeapon() {

        return false;
    }

    private boolean pickUpGoodItem() {

        return false;


    }

    private boolean pickUpSomeWeapon() {

        if (weaponry.hasLoadedWeapon(UT2004ItemType.MINIGUN)) {
            if (navigateTo(UT2004ItemType.MINIGUN)) return true;
        }
        if (weaponry.hasLoadedWeapon(UT2004ItemType.SHOCK_RIFLE)) {
            if (navigateTo(UT2004ItemType.SHOCK_RIFLE)) return true;
        }
        if (weaponry.hasLoadedWeapon(UT2004ItemType.LINK_GUN)) {
            if (navigateTo(UT2004ItemType.LINK_GUN)) return true;
        }
        return false;
    }

    private boolean needHealthUrgent() {
        return info.getHealth() < 40 || info.getHealth() + info.getArmor() < 40;
    }

    private boolean pickUpNearestHealth() {
        return navigateTo(UT2004ItemType.HEALTH_PACK);
    }

    private boolean navigateTo(UT2004ItemType type) {
        if (navigation.isNavigatingToItem() && navigation.getCurrentTargetItem().getType() == type) return true;
        Item item = fwMap.getNearestItem(items.getSpawnedItems(type).values(), navPoints.getNearestNavPoint());

        if (item != null) {
            bot.getBotName().setInfo("To", item.getType().getName());
            navigation.navigate(item);
        } else {
            bot.getBotName().setInfo("No item to run to.");
            return false;
        }
        return true;
    }

    private boolean combat() {
        if (!players.canSeeEnemies()) {
            return false;
        }
        //navigation.navigate(players.getNearestVisibleEnemy());
        shoot.shoot(weaponPrefs, players.getNearestVisibleEnemy());

        return true;
    }

    private boolean canSeeEnemies() {
        return players.canSeeEnemies();
    }

    /**
     * Called each time our bot die. Good for reseting all bot state dependent
     * variables.
     *
     * @param event
     */
    @Override
    public void botKilled(BotKilled event) {
    }
}
