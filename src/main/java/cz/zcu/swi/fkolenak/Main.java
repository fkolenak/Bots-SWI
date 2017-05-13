package cz.zcu.swi.fkolenak;

import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import cz.zcu.swi.fkolenak.helpers.Constants;

/**
 * Created by japan on 10-May-17.
 */
public class Main {
    public static int YEAR = Constants.YEAR;
    public static int TEAM = Constants.TEAM;
    public static int SKILL = Constants.SKILL;
    public static int NUMBER_OF_BOTS = Constants.NUM_BOTS;
    public static String SERVER_IP = "128.0.0.8";
    public static int PORT = 3000;

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

                String[] split = args[4].split(":");
                SERVER_IP = split[0];
                PORT = 3000;
                if (split.length > 1) {
                    PORT = Integer.parseInt(split[1]);
                }
            } catch (Exception e) {
                System.err.println("Chybne parametry");
                return;
            }
        } else {
            System.out.println("Zadne parametry zadavam defaultni.");
        }

        // wrapped logic for bots executions, suitable to run single bot in single JVM
        new UT2004BotRunner(SmartHunterBot.class, "HunterBot").setHost(SERVER_IP).setPort(PORT).setMain(true).startAgents(NUMBER_OF_BOTS);
    }
}
