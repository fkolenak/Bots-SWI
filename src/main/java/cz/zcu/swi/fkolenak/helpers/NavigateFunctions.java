package cz.zcu.swi.fkolenak.helpers;

import cz.cuni.amis.pathfinding.alg.astar.AStarResult;
import cz.cuni.amis.pogamut.base.agent.navigation.impl.PrecomputedPathFuture;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.zcu.swi.fkolenak.SmartHunterBot;

import java.awt.*;
import java.util.List;

/**
 * Created by japan on 09-May-17.
 */
public class NavigateFunctions extends BotReference {

    private List<NavPoint> currentPath;
    public NavigateFunctions(SmartHunterBot smartHunterBot) {
        super(smartHunterBot);
    }


    private PrecomputedPathFuture<NavPoint> generatePath(NavPoint from, NavPoint to) {
        AStarResult<NavPoint> result = getAStar().findPath(from, to);
        return new PrecomputedPathFuture<NavPoint>(from, to, result.getPath());
    }

    /**
     * Navigate to the item based on type
     *
     * @param type item type to navigate to
     * @return isNavigating?
     */
    public boolean navigateTo(UT2004ItemType type) {
        if (getNavigation().isNavigatingToItem() && getNavigation().getCurrentTargetItem().getType() == type)
            return true;
        Item item = getFwMap().getNearestItem(getItems().getSpawnedItems(type).values(), getNavPoints().getNearestNavPoint());

        if (item != null) {
            getBot().getName().setInfo("To", item.getType().getName());
            navigateTo(item);
        } else {
            getBot().getName().setInfo("No item to run to.");
            return false;
        }
        return true;
    }

    public List<NavPoint> navigateTo(Item target) {
        if (target == null) {
            return null;
        }

        NavPoint playerPosition = getInfo().getNearestNavPoint();
        NavPoint targetNavPoint = target.getNavPoint();

        PrecomputedPathFuture resultPath = generatePath(playerPosition, targetNavPoint);
        currentPath = resultPath.get();
        drawPath(currentPath);
        getNavigation().navigate(resultPath);
        return currentPath;
    }

    public List<NavPoint> navigateTo(NavPoint target) {
        if (target == null) {
            return null;
        }

        NavPoint playerPosition = getInfo().getNearestNavPoint();

        PrecomputedPathFuture resultPath = generatePath(playerPosition, target);
        currentPath = resultPath.get();
        drawPath(currentPath);
        getNavigation().navigate(resultPath);
        return currentPath;
    }

    public List<NavPoint> navigateTo(Location location) {
        if (location == null) {
            return null;
        }
        NavPoint playerPosition = getInfo().getNearestNavPoint();
        NavPoint closesToLoc = getNavPoints().getNearestNavPoint(location);

        PrecomputedPathFuture resultPath = generatePath(playerPosition, closesToLoc);


        currentPath = resultPath.get();
        drawPath(currentPath);

        getNavigation().navigate(resultPath);

        return currentPath;
    }


    private void drawPath(List<NavPoint> path) {
        if (Constants.DRAW) {
            getDraw().clearAll();
            getDraw().setColor(Color.WHITE);
            if (path == null || path.size() == 0) {
                return;
            }
            List<NavPoint> pathPoints = path;
            for (int i = 0; i < pathPoints.size() - 1; i++) {
                getDraw().drawLine(pathPoints.get(i), pathPoints.get(i + 1));
            }
        }
    }


    public List<NavPoint> getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(List<NavPoint> currentPath) {
        this.currentPath = currentPath;
        navigateTo(currentPath);
    }


    public List<NavPoint> navigateTo(List<NavPoint> navPoints) {
        this.currentPath = navPoints;
        if (navPoints == null && navPoints.size() == 0) {
            return null;
        }
        NavPoint from = navPoints.get(0);
        NavPoint to = navPoints.get(navPoints.size() - 1);

        PrecomputedPathFuture resultPath = new PrecomputedPathFuture<NavPoint>(from, to, navPoints);

        currentPath = resultPath.get();
        drawPath(currentPath);

        getNavigation().navigate(resultPath);

        return currentPath;
    }
}
