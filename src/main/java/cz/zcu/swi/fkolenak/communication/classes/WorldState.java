package cz.zcu.swi.fkolenak.communication.classes;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;

import java.io.Serializable;


/**
 * Created by japan on 11-May-17.
 */
public class WorldState implements Serializable {
    private static final long serialVersionUID = 7889253649490330851L;

    private TimeStampedObject<Location> enemyFlag;
    private TimeStampedObject<Location> ourFlag;


    public WorldState() {
        this.ourFlag = new TimeStampedObject(Location.NONE, Long.MIN_VALUE);
        this.enemyFlag = new TimeStampedObject(Location.NONE, Long.MIN_VALUE);
    }

    public void updateWorldState(WorldState kn) {
        if (kn == null) {
            return;
        }
        this.updateOurFlag(kn.getOurFlag());
        this.updateEnemyFlag(kn.getEnemyFlag());
    }

    public TimeStampedObject<Location> getEnemyFlag() {
        return enemyFlag;
    }

    public void updateEnemyFlag(TimeStampedObject<Location> enemyFlag) {
        if (this.enemyFlag.timestamp < enemyFlag.timestamp) {
            this.enemyFlag = enemyFlag;
        }
    }

    public void updateEnemyFlag(Location newLocation, long timestamp) {
        if (this.enemyFlag.timestamp < timestamp) {
            this.enemyFlag.object = newLocation;
            this.enemyFlag.timestamp = timestamp;
        }
    }

    public TimeStampedObject<Location> getOurFlag() {
        return ourFlag;
    }

    public void updateOurFlag(TimeStampedObject<Location> ourFlag) {
        if (this.ourFlag.timestamp < ourFlag.timestamp) {
            this.ourFlag = enemyFlag;
        }
    }

    public void updateOurFlag(Location newLocation, long timestamp) {
        if (this.ourFlag.timestamp < timestamp) {
            this.ourFlag.object = newLocation;
            this.ourFlag.timestamp = timestamp;
        }
    }

}
