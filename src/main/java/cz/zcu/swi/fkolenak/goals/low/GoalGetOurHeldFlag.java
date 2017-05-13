package cz.zcu.swi.fkolenak.goals.low;

import cz.zcu.swi.fkolenak.SmartHunterBot;
import cz.zcu.swi.fkolenak.goals.helpers.Goal;
import cz.zcu.swi.fkolenak.helpers.Constants;
import cz.zcu.swi.fkolenak.helpers.NavigateFunctions;
import cz.zcu.swi.fkolenak.helpers.Paths;
import cz.zcu.swi.fkolenak.helpers.State;

/**
 * Created by japan on 12-May-17.
 */
public class GoalGetOurHeldFlag extends Goal {


    public GoalGetOurHeldFlag(SmartHunterBot bot, NavigateFunctions fNavigate, Paths paths, State state) {
        super(bot, fNavigate, paths, state);
    }

    @Override
    public boolean triggering() {
        return getCTF().isOurFlagHeld();
    }

    @Override
    public double getPriority() {
        return Constants.GOAL_PRIORITY_OUR_FLAG_RETURN;
    }

    @Override
    public void abandon() {
        if (getNavigation().isNavigating()) {
            getNavigation().stopNavigation();
        }
    }

    @Override
    public void perform() {
        if (!getNavigation().isNavigating() || !getNavigation().getCurrentTarget().equals(getCTF().getEnemyBase())) {
            getNavigation().navigate(getCTF().getEnemyBase());
        }
    }
}
