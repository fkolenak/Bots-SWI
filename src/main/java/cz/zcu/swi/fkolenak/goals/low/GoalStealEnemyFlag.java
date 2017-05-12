package cz.zcu.swi.fkolenak.goals.low;

import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.zcu.swi.fkolenak.SmartHunterBot;
import cz.zcu.swi.fkolenak.goals.helpers.Goal;
import cz.zcu.swi.fkolenak.helpers.*;

import java.util.List;

/**
 * Created by japan on 11-May-17.
 */
public class GoalStealEnemyFlag extends Goal {


    public GoalStealEnemyFlag(SmartHunterBot bot, NavigateFunctions fNavigate, Paths paths, State state) {
        super(bot, fNavigate, paths, state);


    }


    @Override
    public boolean triggering() {
        return getCTF().isEnemyFlagHome();
    }

    @Override
    public double getPriority() {
        return Constants.GOAL_PRIORITY_ENEMY_FLAG_STEAL;
    }

    @Override
    public void abandon() {
        if (getNavigation().isNavigating()) {
            getNavigation().stopNavigation();
        }
    }

    @Override
    public void perform() {

        if (getBot().getCombo().canPerformCombo()) {
            if (getBot().getPlayers().getNearestVisibleEnemy() != null
                    && getBot().getPlayers().getNearestVisibleEnemy().getLocation().getDistance(getInfo().getLocation()) < 2500) {
                getBot().getCombo().performBerserk();
            }
        }

        // TODO form a group
        // IF not navigating or not navigating to either base

        if (getNavigation().isNavigating()
                && getNavigation().getCurrentTargetNavPoint().equals(getCTF().getEnemyBase())) {
            return;
        }

        // To be sure no navpoint is closer to me even if I am standing next to base
        double distance = getInfo().getNearestNavPoint().getLocation().getDistance(getCTF().getOurBase().getLocation());


        if (getInfo().getNearestNavPoint().equals(getCTF().getOurBase()) || distance < 150) {
            List<List<NavPoint>> paths = this.paths.getEnemyBasePaths();
            fNavigate.navigateTo(paths.get(getBot().getRandom().nextInt(paths.size())));

            state.setCurrentStateLow(State.LOW.FLAG_STEAL);
            LetKnow.debugState(this.getBot(), state);
        } else {
            fNavigate.navigateTo(getCTF().getEnemyBase());
        }

        //}
    }

    @Override
    public void pathFailed() {

    }

    @Override
    public void targetReached() {

    }
}
