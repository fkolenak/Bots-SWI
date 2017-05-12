package cz.zcu.swi.fkolenak.goals.low;

import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.zcu.swi.fkolenak.SmartHunterBot;
import cz.zcu.swi.fkolenak.goals.helpers.Goal;
import cz.zcu.swi.fkolenak.helpers.Constants;
import cz.zcu.swi.fkolenak.helpers.NavigateFunctions;
import cz.zcu.swi.fkolenak.helpers.Paths;
import cz.zcu.swi.fkolenak.helpers.State;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.Set;

/**
 * Created by japan on 11-May-17.
 */
public class GoalPickupItems extends Goal {


    public GoalPickupItems(SmartHunterBot bot, NavigateFunctions fNavigate, Paths paths, State state) {
        super(bot, fNavigate, paths, state);
    }

    @Override
    public boolean triggering() {
        return true;
    }

    @Override
    public double getPriority() {
        return Constants.GOAL_PRIORITY_PICK_UP_ITEMS;
    }

    @Override
    public void abandon() {
        if (getNavigation().isNavigating()) {
            getNavigation().stopNavigation();
        }
    }

    @Override
    public void perform() {
        if (getWeaponry().getLoadedWeapons().size() < 2) {
            state.setCurrentStateHigh(State.HIGH.GEAR_UP_MINIMAL);
            return;
        }
        if (getNavigation().isNavigating()) {
            return;
        }

        if (getBot().needArmor()) {
            if (getBot().pickUpNearestArmor()) {
                return;
            }
        }
        if (getBot().needHealthUrgent()) {
            if (pickUpNearestHealth()) {
                return;
            }
        }
        if (getBot().pickUpSomeWeapon()) {
            return;
        }
        pickUpSomeItem();
    }


    private boolean pickUpSomeItem() {
        Collection<UT2004ItemType> wanted = new ArrayList<UT2004ItemType>();
        wanted.add(UT2004ItemType.SUPER_HEALTH_PACK);
        wanted.add(UT2004ItemType.MINI_HEALTH_PACK);
        wanted.add(UT2004ItemType.SHIELD_PACK);
        wanted.add(UT2004ItemType.SUPER_SHIELD_PACK);

        Set<Item> items = getBot().getNearestSpawnedItems(wanted);
        if (items != null && items.size() > 0) {
            Random random = new Random();
            fNavigate.navigateTo((Item) items.toArray()[random.nextInt(items.size())]);
            return true;
        } else {
            fNavigate.navigateTo(getNavPoints().getRandomNavPoint());
            return false;
        }
    }

    private boolean pickUpNearestHealth() {
        Item nearestHealth = getBot().getNearestSpawnedItem(UT2004ItemType.HEALTH_PACK);
        if (nearestHealth == null) {
            return false;
        }
        fNavigate.navigateTo(nearestHealth);
        state.setCurrentStateHigh(State.HIGH.READY);
        return true;
    }
}
