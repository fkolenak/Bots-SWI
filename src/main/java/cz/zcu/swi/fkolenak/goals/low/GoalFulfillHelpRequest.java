package cz.zcu.swi.fkolenak.goals.low;

import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.zcu.swi.fkolenak.SmartHunterBot;
import cz.zcu.swi.fkolenak.communication.classes.RequestHelpDefense;
import cz.zcu.swi.fkolenak.goals.helpers.Goal;
import cz.zcu.swi.fkolenak.helpers.Constants;
import cz.zcu.swi.fkolenak.helpers.NavigateFunctions;
import cz.zcu.swi.fkolenak.helpers.Paths;
import cz.zcu.swi.fkolenak.helpers.State;

/**
 * Created by japan on 12-May-17.
 */
public class GoalFulfillHelpRequest extends Goal {


    public GoalFulfillHelpRequest(SmartHunterBot bot, NavigateFunctions fNavigate, Paths paths, State state) {
        super(bot, fNavigate, paths, state);
    }

    @Override
    public boolean triggering() {
        if (bot.getHelpRequest() == null) {
            return false;
        }
        RequestHelpDefense helpRequest = bot.getHelpRequest();
        double distance = getBot().getFwMap().getDistance(getNavPoints().getNearestNavPoint(getInfo().getLocation()), getNavPoints().getNearestNavPoint(helpRequest.getEnemyLocation()));

        if (helpRequest.isUrgent() && distance < 2500) {
            return true;
        } else if (distance < 1500) {
            return true;
        }
        return false;
    }

    @Override
    public double getPriority() {
        return Constants.GOAL_PRIORITY_FULFILL_HELP_REQUEST;
    }

    @Override
    public void abandon() {
        bot.setHelpRequest(null);
    }

    @Override
    public void perform() {
        NavPoint navigateTo;
        if (getGame().getMapName().equals("CTF-BP2-Concentrate")) {
            if (getCTF().getOurBase().getId().getStringId().equals("CTF-BP2-Concentrate.xRedFlagBase1")) {
                navigateTo = getNavPoints().getNavPoint("CTF-BP2-Concentrate.PathNode86");
            } else {
                navigateTo = getNavPoints().getNavPoint("CTF-BP2-Concentrate.PathNode5");
            }
        } else {
            navigateTo = getCTF().getOurBase();
        }

        if (!getNavigation().isNavigating()) {
            fNavigate.navigateTo(navigateTo);
        } else if (!getNavigation().getCurrentTarget().equals(getCTF().getOurBase())) {
            fNavigate.navigateTo(navigateTo);
        }
        if (!getPlayers().canSeeEnemies()) {
            getNavigation().setFocus(bot.getHelpRequest().getEnemyLocation());
        }
        double dist = getInfo().getLocation().getDistance(getCTF().getEnemyBase().getLocation());

        if (dist < 300) {
            getMove().jump();
            // Helped or killed enemy
            if (getPlayers().getNearestVisibleEnemy() == null) {
                bot.setHelpRequest(null);
            }

        }
    }
}
