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
public class GoalPickUpOurFlag extends Goal {
    private WorldState worldState;

    public GoalPickUpOurFlag(SmartHunterBot bot, NavigateFunctions fNavigate, Paths paths, State state, WorldState worldState) {
        super(bot, fNavigate, paths, state);
        this.worldState = worldState;
    }

    @Override
    public boolean triggering() {
        if (worldState.getOurFlag().object == null) {
            return false;
        }
        double distance = getBot().getFwMap().getDistance(getNavPoints().getNearestNavPoint(getInfo().getLocation()), getNavPoints().getNearestNavPoint(worldState.getOurFlag().object));
        if (getCTF().isOurFlagDropped()) {
            if (getCTF().isBotCarryingEnemyFlag() && distance < 600) {
                return true;
            }
            return distance < 3000;
        }
        return false;
    }

    @Override
    public double getPriority() {
        return Constants.GOAL_PRIORITY_OUR_FLAG_PICK_UP;
    }

    @Override
    public void abandon() {
        if (getNavigation().isNavigating()) {
            getNavigation().stopNavigation();
        }
    }

    @Override
    public void perform() {
        if (worldState.getOurFlag() == null && worldState.getOurFlag().object == null) {
            return;
        }
        Location ourFlagLocation = worldState.getOurFlag().object;

        double distance = ourFlagLocation.getDistance(getInfo().getLocation());
        if (distance < 300) {
            getMove().moveTo(ourFlagLocation);
        }

        if (!getNavigation().isNavigating()) {
            getNavigation().navigate(ourFlagLocation);
            return;
        }
        if (getNavigation().getCurrentTarget() != null && !getNavigation().getCurrentTarget().equals(ourFlagLocation)) {
            getNavigation().navigate(ourFlagLocation);
        }
    }


}