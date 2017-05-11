package cz.zcu.swi.fkolenak;

import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;

/**
 * Created by japan on 10-May-17.
 */
public class Main {
    public static int YEAR;
    public static int TEAM = 0;
    public static int SKILL = 5;
    public static int NUMBER_OF_BOTS = 3;

    /**
     * This method is called when the bot is started either from IDE or from
     * command line.
     *
     * @param args
     */
    public static void main(String args[]) throws PogamutException {
        if (args.length == 5) {
            try {
                YEAR = Integer.parseInt(args[0]);

                TEAM = Integer.parseInt(args[1]);

                SKILL = Integer.parseInt(args[2]);

                NUMBER_OF_BOTS = Integer.parseInt(args[3]);

            } catch (Exception e) {
                System.err.println("Chybne parametry");
                return;
            }
        } else {
            System.out.println("Zadne parametry zadavam defaultni.");
        }

        // wrapped logic for bots executions, suitable to run single bot in single JVM
        new UT2004BotRunner(SmartHunterBot.class, "HunterBot").setMain(true).startAgents(NUMBER_OF_BOTS);
    }
}
