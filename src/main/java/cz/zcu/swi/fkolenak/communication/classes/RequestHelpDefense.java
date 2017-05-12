package cz.zcu.swi.fkolenak.communication.classes;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;

import java.io.Serializable;

/**
 * Created by japan on 12-May-17.
 */
public class RequestHelpDefense implements Serializable {
    private static final long serialVersionUID = 6059967110541394610L;

    private Location enemyLocation;
    private Location myLocation;
    private boolean urgent;

    public RequestHelpDefense(Location enemyLocation, Location myLocation, boolean urgent) {
        this.enemyLocation = enemyLocation;
        this.myLocation = myLocation;
        this.urgent = urgent;
    }


    public Location getEnemyLocation() {
        return enemyLocation;
    }

    public void setEnemyLocation(Location enemyLocation) {
        this.enemyLocation = enemyLocation;
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
