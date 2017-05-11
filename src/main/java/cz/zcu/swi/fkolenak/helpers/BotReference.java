package cz.zcu.swi.fkolenak.helpers;

import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.*;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.IUT2004Navigation;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.astar.UT2004AStar;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.floydwarshall.FloydWarshallMap;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.drawing.UT2004Draw;
import cz.zcu.swi.fkolenak.SmartHunterBot;

/**
 * Created by japan on 09-May-17.
 */
public class BotReference {
    private SmartHunterBot bot;

    public BotReference(SmartHunterBot bot) {
        this.bot = bot;
    }

    protected IUT2004Navigation getNavigation() {
        return bot.getNavigation();
    }

    protected Items getItems() {
        return bot.getItems();
    }

    protected FloydWarshallMap getFwMap() {
        return bot.getFwMap();
    }

    protected AgentInfo getInfo() {
        return bot.getInfo();
    }

    protected UT2004AStar getAStar() {
        return bot.getAStar();
    }

    protected CTF getCTF() {
        return bot.getCTF();
    }

    protected UT2004Draw getDraw() {
        return bot.getDraw();
    }

    protected NavPoints getNavPoints() {
        return bot.getNavPoints();
    }

    protected LogCategory getLog() {
        return bot.getLog();
    }

    protected SmartHunterBot getBot() {
        return bot;
    }

    protected Game getGame() {
        return bot.getGame();
    }


}
