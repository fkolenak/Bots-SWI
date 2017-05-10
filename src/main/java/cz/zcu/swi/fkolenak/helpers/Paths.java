package cz.zcu.swi.fkolenak.helpers;

import cz.cuni.amis.pathfinding.alg.astar.AStarResult;
import cz.cuni.amis.pathfinding.map.IPFMapView;
import cz.cuni.amis.pogamut.base.agent.navigation.impl.PrecomputedPathFuture;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.zcu.swi.fkolenak.SmartHunterBot;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by japan on 09-May-17.
 */
public class Paths extends BotReference {
    private NavPoint myBase;
    private NavPoint enemyBase;

    private Color[] colors = {Color.cyan, Color.RED, Color.GREEN, Color.MAGENTA, Color.YELLOW, Color.PINK, Color.orange};

    private List<List<NavPoint>> enemyBasePaths = new ArrayList<List<NavPoint>>();
    private List<List<NavPoint>> ourBasePaths = new ArrayList<List<NavPoint>>();


    public Paths(SmartHunterBot bot) {
        super(bot);
        myBase = getCTF().getOurBase();
        enemyBase = getCTF().getEnemyBase();
        getDraw().clearAll();
    }

    public List<List<NavPoint>> generatePathsToEnemyBase() {

        myBase = getCTF().getOurBase();
        enemyBase = getCTF().getEnemyBase();

        List<NavPoint> nearest = getFwMap().getPath(myBase, enemyBase);
        enemyBasePaths.add(nearest);
        drawPath(nearest, Color.WHITE);
        for (int i = 0; i < 5; i++) {
            PrecomputedPathFuture<NavPoint> newPath = generateNextCTFPath(enemyBasePaths, myBase, enemyBase);
            if (newPath != null && newPath.get() != null && newPath.get().size() != 0) {
                enemyBasePaths.add(newPath.get());
                drawPath(newPath.get(), colors[i]);

            } else {
                break;
            }
        }


        return enemyBasePaths;
    }

    public List<List<NavPoint>> generatePathsToOurBase() {
        List<NavPoint> nearest = getFwMap().getPath(enemyBase, myBase);
        ourBasePaths.add(nearest);
        getDraw().clearAll();
        drawPath(nearest, Color.WHITE);

        for (int i = 0; i < 5; i++) {
            PrecomputedPathFuture<NavPoint> newPath = generateNextCTFPath(ourBasePaths, enemyBase, myBase);
            if (newPath != null && newPath.get() != null && newPath.get().size() != 0) {
                ourBasePaths.add(newPath.get());
                drawPath(newPath.get(), colors[colors.length - i - 1]);

            } else {
                break;
            }
        }


        return ourBasePaths;
    }

    private PrecomputedPathFuture<NavPoint> generateNextCTFPath(List<List<NavPoint>> existingPaths, NavPoint from, NavPoint to) {

        AStarResult<NavPoint> result = getAStar().findPath(from, to, new DifferentPathMapView(existingPaths, getBot()));

        PrecomputedPathFuture<NavPoint> pathFuture = new PrecomputedPathFuture<NavPoint>(from, to, result.getPath());

        return pathFuture;
    }


    public List<List<NavPoint>> getEnemyBasePaths() {
        return enemyBasePaths;
    }

    public List<List<NavPoint>> getOurBasePaths() {
        return ourBasePaths;
    }

    private void drawPath(List<? extends ILocated> path, Color color) {

        if (Constants.DEBUG) {
            getDraw().setColor(color);
            for (int i = 1; i < path.size(); ++i) {
                getDraw().drawLine(path.get(i - 1), path.get(i));
            }

        }
    }


    /**
     * Penalize nodes that are too near to existing paths!
     */
    private class DifferentPathMapView implements IPFMapView<NavPoint> {

        private List<List<NavPoint>> existingPaths = new ArrayList<List<NavPoint>>();
        private SmartHunterBot bot;

        public DifferentPathMapView(List<List<NavPoint>> existingPaths, SmartHunterBot bot) {
            this.bot = bot;
            this.existingPaths = existingPaths;
        }

        @Override
        public Collection<NavPoint> getExtraNeighbors(NavPoint node, Collection<NavPoint> mapNeighbors) {
            if (bot.getGame().getMapName().equals("CTF-Citadel")) {
                UnrealId id = node.getId();
                if (id.getStringId().equals("CTF-Citadel.Teleporter0")) {
                    Collection<NavPoint> newNav = new ArrayList<NavPoint>();
                    newNav.add(bot.getNavPoints().getNavPoint("CTF-Citadel.InventorySpot215"));
                    return newNav;
                }
            }


            return null;
        }

        @Override
        public int getNodeExtraCost(NavPoint node, int mapCost) {
            if (node.getItem() != null) {
                if (node.getItemClass().getCategory().equals(ItemType.Category.WEAPON)
                        || node.getItemClass().getCategory().equals(ItemType.Category.HEALTH)
                        || node.getItemClass().getCategory().equals(ItemType.Category.SHIELD)) {
                    return -100;
                } else if (node.getItemClass().getGroup().equals(UT2004ItemType.UT2004Group.UDAMAGE)) {
                    return -250;
                }
            }
            for (List<NavPoint> existingPath : existingPaths) {
                if (existingPath == null) {
                    return 0;
                }
                if (existingPath.contains(node)) {
                    return 150;
                }
            }

            return 10;
        }

        @Override
        public int getArcExtraCost(NavPoint nodeFrom, NavPoint nodeTo, int mapCost) {
            return 0;
        }

        @Override
        public boolean isNodeOpened(NavPoint node) {
            if (node.getId().getStringId().equals("CTF-Maul.JumpSpot3")
                    || node.getId().getStringId().equals("CTF-Maul.JumpSpot6")) {
                return false;
            }
            /*
            if(existingPaths == null){
                return true;
            }
            if(node.equals(myBase) || node.equals(enemyBase)) {
                return true;
            }
            if(node.getId().getStringId().equals("CTF-Maul.UTJumppad0")
                    || node.getId().getStringId().equals("CTF-Maul.PathNode57")
                    || node.getId().getStringId().equals("CTF-Maul.UTJumppad1")
                    || node.getId().getStringId().equals("CTF-Maul.PathNode59")){
                return true;
            }
            for (List<NavPoint> existingPath : existingPaths) {
                if(existingPath == null){
                    return true;
                }
                if(existingPath.contains(node)){
                    return false;
                }
            }*/
            return true;
        }

        @Override
        public boolean isArcOpened(NavPoint nodeFrom, NavPoint nodeTo) {
            // ALL ARCS ARE OPENED
            return true;
        }

    }
}
