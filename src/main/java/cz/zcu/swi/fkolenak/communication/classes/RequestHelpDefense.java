package cz.zcu.swi.fkolenak.communication.classes;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;

import java.io.Serializable;

/**
 * Created by japan on 12-May-17.
 */
public class RequestHelpDefense implements Serializable {
    private static final long serialVersionUID = 6059967110541394610L;

    private Player enemy;
    private Location myLocation;
    private boolean urgent;

    public RequestHelpDefense(Player enemy, Location myLocation, boolean urgent) {
        this.enemy = enemy;
        this.myLocation = myLocation;
        this.urgent = urgent;
    }


    public Player getEnemy() {
        return enemy;
    }

    public void setEnemy(Player enemy) {
        this.enemy = enemy;
    }

    public Location getMyLocation() {
        return myLocation;
    }

    public void setMyLocation(Location myLocation) {
        this.myLocation = myLocation;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

}
