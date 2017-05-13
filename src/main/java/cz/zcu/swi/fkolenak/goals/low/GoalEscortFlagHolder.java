package cz.zcu.swi.fkolenak.goals.low;

import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.zcu.swi.fkolenak.SmartHunterBot;
import cz.zcu.swi.fkolenak.goals.helpers.Goal;
import cz.zcu.swi.fkolenak.helpers.Constants;
import cz.zcu.swi.fkolenak.helpers.NavigateFunctions;
import cz.zcu.swi.fkolenak.helpers.Paths;
import cz.zcu.swi.fkolenak.helpers.State;

/**
 * Created by japan on 12-May-17.
 */
public class GoalEscortFlagHolder extends Goal {

    private NavPoint targetLocation;

    public GoalEscortFlagHolder(SmartHunterBot bot, NavigateFunctions fNavigate, Paths paths, State state) {
        super(bot, fNavigate, paths, state);
    }

    @Override
    public boolean triggering() {
        if (getCTF().isOurTeamCarryingEnemyFlag()) {
            double dist = getCTF().getOurBase().getLocation().getDistance(bot.getWorldState().getEnemyFlag().object);
            return !(dist < 300);
        }
        return false;
    }

    @Override
    public double getPriority() {
        return Constants.GOAL_PRIORITY_ESCORT;
    }

    @Override
    public void abandon() {
        if (getNavigation().isNavigating()) {
            getNavigation().stopNavigation();
        }
        targetLocation = null;
    }

    @Override
    public void perform() {

        if (getCTF().getEnemyFlag().isVisible()) {
            getNavigation().navigate(getPlayers().getPlayer(getCTF().getEnemyFlag().getHolder()));
            return;
        }
        getLog().info(bot.getWorldState().getEnemyFlag().object.toString());
        if (getNavPoints().getNearestNavPoint(getInfo().getLocation()).equals(targetLocation)) {
            this.targetLocation = getNavPoints().getNearestNavPoint(bot.getWorldState().getEnemyFlag().object);
            getNavigation().navigate(targetLocation);
            return;
        }

        if (getNavigation().isNavigating()) {
            return;
        }

        if (!getNavigation().isNavigating() || targetLocation == null) {
            this.targetLocation = getNavPoints().getNearestNavPoint(bot.getWorldState().getEnemyFlag().object);
            getNavigation().navigate(bot.getWorldState().getEnemyFlag().object);
        }
    }
}
