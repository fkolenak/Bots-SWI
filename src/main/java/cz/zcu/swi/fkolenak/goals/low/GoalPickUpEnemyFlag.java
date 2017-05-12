package cz.zcu.swi.fkolenak.goals.low;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.zcu.swi.fkolenak.SmartHunterBot;
import cz.zcu.swi.fkolenak.communication.classes.WorldState;
import cz.zcu.swi.fkolenak.goals.helpers.Goal;
import cz.zcu.swi.fkolenak.helpers.Constants;
import cz.zcu.swi.fkolenak.helpers.NavigateFunctions;
import cz.zcu.swi.fkolenak.helpers.Paths;
import cz.zcu.swi.fkolenak.helpers.State;

/**
 * Created by japan on 12-May-17.
 */
public class GoalPickUpEnemyFlag extends Goal {
    private WorldState worldState;

    public GoalPickUpEnemyFlag(SmartHunterBot bot, NavigateFunctions fNavigate, Paths paths, State state, WorldState worldState) {
        super(bot, fNavigate, paths, state);
        this.worldState = worldState;
    }

    @Override
    public boolean triggering() {
        if (worldState.getEnemyFlag().object == null) {
            return false;
        }
        double distance = getBot().getFwMap().getDistance(getNavPoints().getNearestNavPoint(getInfo().getLocation()), getNavPoints().getNearestNavPoint(worldState.getEnemyFlag().object));
        if (getCTF().isEnemyFlagDropped()) {
            return distance < 3000;
        }
        return false;
    }

    @Override
    public double getPriority() {
        return Constants.GOAL_PRIORITY_ENEMY_FLAG_PICK_UP;
    }

    @Override
    public void abandon() {
        if (getNavigation().isNavigating()) {
            getNavigation().stopNavigation();
        }
    }

    @Override
    public void perform() {
        if (worldState.getEnemyFlag() == null && worldState.getEnemyFlag().object == null) {
            return;
        }
        if (!getNavigation().isNavigating()) {
            Location enemyFlagLocation = worldState.getEnemyFlag().object;
            getNavigation().navigate(enemyFlagLocation);
        }
    }

    @Override
    public void pathFailed() {

    }

    @Override
    public void targetReached() {

    }
}
