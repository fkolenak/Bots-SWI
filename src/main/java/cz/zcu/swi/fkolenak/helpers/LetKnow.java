package cz.zcu.swi.fkolenak.helpers;

import cz.zcu.swi.fkolenak.SmartHunterBot;

/**
 * Created by japan on 11-May-17.
 */
public class LetKnow {


    /**
     * Displays info tag on the bot.
     *
     * @param bot
     * @param state current bot goals to display
     */
    public static void debugState(SmartHunterBot bot, State state) {
        if (Constants.VERBOSE) {
            bot.getLog().info("Current bot state: " + state.toString());
            //bot.getBot().getBotName().setInfo("State", state.toString());
        }
    }

    /**
     * Displays info tag on the bot.
     */
    public static void debugGoal(SmartHunterBot bot, String key, String value) {
        if (Constants.DEBUG) {
            bot.getLog().info("Setting info:" + key + " -> " + value);
        }
        if (Constants.VERBOSE) {
            bot.getBot().getBotName().setInfo(key, value);
        }
    }
}
