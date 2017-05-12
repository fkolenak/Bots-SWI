package cz.zcu.swi.fkolenak.goals.low;

import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.utils.Heatup;
import cz.zcu.swi.fkolenak.SmartHunterBot;
import cz.zcu.swi.fkolenak.goals.helpers.Goal;
import cz.zcu.swi.fkolenak.helpers.Constants;
import cz.zcu.swi.fkolenak.helpers.NavigateFunctions;
import cz.zcu.swi.fkolenak.helpers.Paths;
import cz.zcu.swi.fkolenak.helpers.State;

/**
 * Created by japan on 12-May-17.
 */
public class GoalHuntEnemyFlagStealer extends Goal {


    public GoalHuntEnemyFlagStealer(SmartHunterBot bot, NavigateFunctions fNavigate, Paths paths, State state) {
        super(bot, fNavigate, paths, state);
    }

    private Heatup pursue = new Heatup(500);


    @Override
    public boolean triggering() {
        if (pursue.isHot()) {
            return true;
        }
        if (!getCTF().isEnemyTeamCarryingOurFlag() || !getCTF().getOurFlag().isVisible()) {
            return false;
        }

        double distance = getBot().getFwMap().getDistance(getNavPoints().getNearestNavPoint(getInfo().getLocation()), getNavPoints().getNearestNavPoint(getCTF().getOurFlag().getLocation()));
        return distance < 2000;
    }

    @Override
    public double getPriority() {
        return Constants.GOAL_PRIORITY_HUNT_ENEMY_FLAG_STEALER;
    }

    @Override
    public void abandon() {
        if (getNavigation().isNavigating()) {
            getNavigation().stopNavigation();
        }
    }

    @Override
    public void perform() {
        UnrealId stealerId = getCTF().getOurFlag().getHolder();
        if (stealerId == null) {
            return;
        }
        if (pursue.isCool()) {
            pursue.heat();
            Player stealer = getBot().getPlayers().getPlayer(stealerId);
            getNavigation().navigate(stealer);
        }

    }

}
