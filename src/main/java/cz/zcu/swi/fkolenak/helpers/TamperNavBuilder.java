package cz.zcu.swi.fkolenak.helpers;

import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.NavigationGraphBuilder;
import cz.zcu.swi.fkolenak.SmartHunterBot;

/**
 * Created by japan on 08-May-17.
 */
public class TamperNavBuilder {


    public static void removeBadEdges(SmartHunterBot bot) {
        if (bot.getGame().getMapName().equals("DM-1on1-Albatross")) {
            bot.getNavBuilder().removeEdge("PathNode35", "JumpSpot11");
            bot.getNavBuilder().removeEdge("LiftCenter1", "LiftExit4");
            bot.getNavBuilder().removeEdge("PathNode88", "JumpSpot8");
            bot.getNavBuilder().removeEdge("PathNode27", "JumpSpot12");
        }
        if (bot.getGame().getMapName().equals("CTF-Citadel")) {
            //if(bot.getGame().getMapName().equals("CTF-Citadel")){
            bot.getNavBuilder().removeEdge("CTF-Citadel.PathNode23", "CTF-Citadel.AssaultPath9");
            bot.getNavBuilder().removeEdge("CTF-Citadel.PathNode75", "CTF-Citadel.JumpSpot26");

            NavigationGraphBuilder.NewNavPointBuilder a = bot.getNavBuilder().newNavPoint("CTF-Citadel.JumpSpot001");
            a.setLocation(-3352.11, 69.54, -1957.88);
            a.createSimpleEdgeTo("CTF-Citadel.PathNode46");
            a.createNavPoint();

            a = bot.getNavBuilder().newNavPoint("CTF-Citadel.JumpSpot002");
            a.setLocation(2253.17, -1420.94, -1959.53);
            a.createSimpleEdgeTo("CTF-Citadel.AssaultPath9");
            a.createNavPoint();

            bot.getNavBuilder().removeEdgesBetween("CTF-Citadel.PathNode18", "CTF-Citadel.AssaultPath1");
            bot.getNavBuilder().createSimpleEdgesBetween("CTF-Citadel.Teleporter0", "CTF-Citadel.InventorySpot215");
        }


        if (bot.getGame().getMapName().equals("CTF-BP2-Concentrate")) {
            bot.getNavBuilder().modifyNavPoint("CTF-BP2-Concentrate.PathNode39").modifyEdgeTo("CTF-BP2-Concentrate.xBlueFlagBase0").clearFlags();



            bot.getNavBuilder().removeEdge("CTF-BP2-Concentrate.xRedFlagBase1", "CTF-BP2-Concentrate.PathNode81");
            bot.getNavBuilder().removeEdge("CTF-BP2-Concentrate.xRedFlagBase1", "CTF-BP2-Concentrate.PathNode74");

            bot.getNavBuilder().removeEdgesBetween("CTF-BP2-Concentrate.JumpSpot2", "CTF-BP2-Concentrate.PathNode74");
            bot.getNavBuilder().removeEdgesBetween("CTF-BP2-Concentrate.JumpSpot2", "CTF-BP2-Concentrate.PathNode81");

            bot.getNavBuilder().removeEdgesBetween("CTF-BP2-Concentrate.JumpSpot13", "CTF-BP2-Concentrate.JumpSpot11");
            bot.getNavBuilder().removeEdgesBetween("CTF-BP2-Concentrate.PathNode40", "CTF-BP2-Concentrate.JumpSpot11");
            bot.getNavBuilder().removeEdgesBetween("CTF-BP2-Concentrate.JumpSpot14", "CTF-BP2-Concentrate.JumpSpot11");
            bot.getNavBuilder().removeEdgesBetween("CTF-BP2-Concentrate.PathNode31", "CTF-BP2-Concentrate.JumpSpot11");
            bot.getNavBuilder().removeEdge("CTF-BP2-Concentrate.PathNode76", "CTF-BP2-Concentrate.JumpSpot11");

            bot.getNavBuilder().removeEdgesBetween("CTF-BP2-Concentrate.PathNode78", "CTF-BP2-Concentrate.JumpSpot14");
            bot.getNavBuilder().removeEdgesBetween("CTF-BP2-Concentrate.PathNode77", "CTF-BP2-Concentrate.JumpSpot13");


            bot.getNavBuilder().removeEdge("CTF-BP2-Concentrate.PathNode75", "CTF-BP2-Concentrate.JumpSpot2");
            bot.getNavBuilder().removeEdgesBetween("CTF-BP2-Concentrate.InventorySpot59", "CTF-BP2-Concentrate.PathNode81");
            bot.getNavBuilder().removeEdge("CTF-BP2-Concentrate.PathNode81", "CTF-BP2-Concentrate.AssaultPath12");
            bot.getNavBuilder().removeEdge("CTF-BP2-Concentrate.PathNode74", "CTF-BP2-Concentrate.AssaultPath12");

            bot.getNavBuilder().removeEdge("CTF-BP2-Concentrate.xBlueFlagBase0", "CTF-BP2-Concentrate.PathNode44");
            bot.getNavBuilder().removeEdge("CTF-BP2-Concentrate.xBlueFlagBase0", "CTF-BP2-Concentrate.PathNode0");

            bot.getNavBuilder().removeEdgesBetween("CTF-BP2-Concentrate.JumpSpot4", "CTF-BP2-Concentrate.JumpSpot6");
            bot.getNavBuilder().removeEdgesBetween("CTF-BP2-Concentrate.JumpSpot4", "CTF-BP2-Concentrate.JumpSpot5");
            bot.getNavBuilder().removeEdgesBetween("CTF-BP2-Concentrate.JumpSpot4", "CTF-BP2-Concentrate.PathNode18");
            bot.getNavBuilder().removeEdgesBetween("CTF-BP2-Concentrate.JumpSpot4", "CTF-BP2-Concentrate.PathNode30");
            bot.getNavBuilder().removeEdge("CTF-BP2-Concentrate.PathNode43", "CTF-BP2-Concentrate.JumpSpot4");
            bot.getNavBuilder().removeEdge("CTF-BP2-Concentrate.PathNode23", "CTF-BP2-Concentrate.JumpSpot5");
            bot.getNavBuilder().removeEdge("CTF-BP2-Concentrate.PathNode35", "CTF-BP2-Concentrate.JumpSpot6");

            bot.getNavBuilder().removeEdgesBetween("CTF-BP2-Concentrate.PathNode39", "CTF-BP2-Concentrate.JumpSpot6");
            bot.getNavBuilder().removeEdgesBetween("CTF-BP2-Concentrate.PathNode39", "CTF-BP2-Concentrate.JumpSpot5");

            bot.getNavBuilder().removeEdgesBetween("CTF-BP2-Concentrate.PathNode39", "CTF-BP2-Concentrate.JumpSpot3");


            bot.getNavBuilder().removeEdgesBetween("CTF-BP2-Concentrate.AssaultPath5", "CTF-BP2-Concentrate.PathNode0");
            bot.getNavBuilder().removeEdgesBetween("CTF-BP2-Concentrate.AssaultPath5", "CTF-BP2-Concentrate.PathNode44");
            bot.getNavBuilder().removeEdge("CTF-BP2-Concentrate.PathNode0", "CTF-BP2-Concentrate.JumpSpot3");
            bot.getNavBuilder().removeEdgesBetween("CTF-BP2-Concentrate.JumpSpot3", "CTF-BP2-Concentrate.PathNode44");
            bot.getNavBuilder().removeEdgesBetween("CTF-BP2-Concentrate.InventorySpot55", "CTF-BP2-Concentrate.PathNode44");


            bot.getNavBuilder().removeEdge("CTF-BP2-Concentrate.PathNode68", "CTF-BP2-Concentrate.JumpSpot12");
            bot.getNavBuilder().removeEdge("CTF-BP2-Concentrate.PathNode69", "CTF-BP2-Concentrate.JumpSpot10");

            // WTF edge
            bot.getNavBuilder().removeEdgesBetween("CTF-BP2-Concentrate.JumpSpot11", "CTF-BP2-Concentrate.PathNode44");

        }

        if (bot.getGame().getMapName().equals("CTF-Maul")) {


            bot.getNavBuilder().removeEdge("CTF-Maul.PathNode66", "CTF-Maul.JumpSpot2");
            bot.getNavBuilder().removeEdge("CTF-Maul.PathNode66", "CTF-Maul.JumpSpot18");
            bot.getNavBuilder().removeEdge("CTF-Maul.PathNode67", "CTF-Maul.JumpSpot18");
            bot.getNavBuilder().removeEdge("CTF-Maul.PathNode95", "CTF-Maul.JumpSpot3");

            bot.getNavBuilder().removeEdgesBetween("CTF-Maul.JumpSpot23", "CTF-Maul.PathNode91");
            bot.getNavBuilder().removeEdgesBetween("CTF-Maul.JumpSpot23", "CTF-Maul.PathNode127");
            bot.getNavBuilder().removeEdgesBetween("CTF-Maul.JumpSpot23", "CTF-Maul.PathNode128");
            bot.getNavBuilder().removeEdgesBetween("CTF-Maul.JumpSpot23", "CTF-Maul.JumpSpot0");

            bot.getNavBuilder().removeEdgesBetween("CTF-Maul.JumpSpot22", "CTF-Maul.PathNode96");
            bot.getNavBuilder().removeEdgesBetween("CTF-Maul.JumpSpot22", "CTF-Maul.PathNode33");
            bot.getNavBuilder().removeEdgesBetween("CTF-Maul.JumpSpot22", "CTF-Maul.PathNode31");
            bot.getNavBuilder().removeEdgesBetween("CTF-Maul.JumpSpot22", "CTF-Maul.PathNode112");

            bot.getNavBuilder().removeEdge("CTF-Maul.PathNode48", "CTF-Maul.JumpSpot7");
            bot.getNavBuilder().removeEdge("CTF-Maul.PathNode6", "CTF-Maul.JumpSpot20");
            bot.getNavBuilder().removeEdge("CTF-Maul.PathNode12", "CTF-Maul.JumpSpot6");

            bot.getNavBuilder().removeEdgesBetween("CTF-Maul.JumpSpot25", "CTF-Maul.JumpSpot8");
            bot.getNavBuilder().removeEdgesBetween("CTF-Maul.JumpSpot25", "CTF-Maul.PathNode19");
            bot.getNavBuilder().removeEdgesBetween("CTF-Maul.JumpSpot25", "CTF-Maul.PathNode161");


            bot.getNavBuilder().removeEdgesBetween("CTF-Maul.JumpSpot24", "CTF-Maul.JumpSpot4");
            bot.getNavBuilder().removeEdgesBetween("CTF-Maul.JumpSpot25", "CTF-Maul.PathNode171");
            bot.getNavBuilder().removeEdgesBetween("CTF-Maul.JumpSpot25", "CTF-Maul.PathNode169");
            bot.getNavBuilder().removeEdgesBetween("CTF-Maul.JumpSpot25", "CTF-Maul.PathNode44");

            bot.getNavBuilder().removeEdge("CTF-Maul.PathNode143", "CTF-Maul.JumpSpot8");
            bot.getNavBuilder().removeEdge("CTF-Maul.PathNode6", "CTF-Maul.JumpSpot4");

            bot.getNavBuilder().removeEdge("CTF-Maul.PathNode95", "CTF-Maul.AIMarker147");
            bot.getNavBuilder().removeEdge("CTF-Maul.PathNode93", "CTF-Maul.JumpSpot0");


            bot.getNavBuilder().modifyNavPoint("CTF-Maul.UTJumppad1").addX(-20).apply();

            bot.getNavBuilder().modifyNavPoint("CTF-Maul.PathNode59").modifyEdgeTo("CTF-Maul.xRedFlagBase0").clearFlags();
            bot.getNavBuilder().modifyNavPoint("CTF-Maul.PathNode57").modifyEdgeTo("CTF-Maul.xBlueFlagBase0").clearFlags();

        }


    }

}
