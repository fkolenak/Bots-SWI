package cz.zcu.swi.fkolenak.goals.low;

import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.zcu.swi.fkolenak.SmartHunterBot;
import cz.zcu.swi.fkolenak.goals.helpers.Goal;
import cz.zcu.swi.fkolenak.helpers.Constants;
import cz.zcu.swi.fkolenak.helpers.NavigateFunctions;
import cz.zcu.swi.fkolenak.helpers.Paths;
import cz.zcu.swi.fkolenak.helpers.State;

import java.util.List;

/**
 * Created by japan on 11-May-17.
 */
public class GoalStealingEnemyFlag extends Goal {


    public GoalStealingEnemyFlag(SmartHunterBot bot, NavigateFunctions fNavigate, Paths paths, State state) {
        super(bot, fNavigate, paths, state);
    }

    @Override
    public boolean triggering() {
        return getCTF().isEnemyFlagHeld() && getCTF().isBotCarryingEnemyFlag();
    }

    @Override
    public double getPriority() {
        return Constants.GOAL_PRIORITY_STEALING_ENEMY_FLAG;
    }

    @Override
    public void abandon() {
        state.setCurrentStateLow(State.LOW.NONE);
    }

    @Override
    public void perform() {
        if (getBot().getCombo().canPerformCombo()) {
            getBot().getCombo().performDefensive();
        }


        double distance = getInfo().getNearestNavPoint().getLocation().getDistance(getCTF().getOurBase().getLocation());
        if (getInfo().getNearestNavPoint().equals(getCTF().getOurBase()) || distance < 170) {
            if (getCTF().isOurFlagHome()) {
                getNavigation().navigate(getCTF().getOurBase());
                return;
            }
            getNavigation().stopNavigation();
            getMove().turnHorizontal(32000);
            getMove().doubleJump();
            return;
        }


        boolean isNavigating = getNavigation().isNavigating();
        if (!isNavigating) {
            List<List<NavPoint>> paths = this.paths.getOurBasePaths();
            fNavigate.navigateTo(paths.get(getBot().getRandom().nextInt(paths.size())));
            state.setCurrentStateLow(State.LOW.FLAG_STEALING);
            return;
            // TODO cover me
        }
        if (getNavigation().getCurrentTargetNavPoint().equals(getCTF().getOurBase())) {
            return;
        } else {
            fNavigate.navigateTo(getCTF().getOurBase());
        }

        if (!getCTF().canBotScore()) {
            // TODO get our flag
            getMove().doubleJump();
            // This bot just waits until teammates returns our flag
        } else {

        }
    }

    @Override
    public void pathFailed() {

    }

    @Override
    public void targetReached() {

    }
}
