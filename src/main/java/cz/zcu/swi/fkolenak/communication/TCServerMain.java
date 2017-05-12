package cz.zcu.swi.fkolenak.communication;

import cz.cuni.amis.pogamut.ut2004.teamcomm.server.UT2004TCServer;

/**
 * Created by japan on 12-May-17.
 */
public class TCServerMain {
    public static void main(String[] args) {
        // Start TC (~ TeamCommunication) Server first...
        UT2004TCServer tcServer = UT2004TCServer.startTCServer();
    }
}