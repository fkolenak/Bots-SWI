package cz.zcu.swi.fkolenak.communication.classes;

import java.io.Serializable;

/**
 * Created by japan on 12-May-17.
 */
public class TimeStampedObject<O> implements Serializable {

    private static final long serialVersionUID = 5032162919064830332L;
    public O object;
    public long timestamp;

    public TimeStampedObject(O object, long timestamp) {
        this.object = object;
        this.timestamp = timestamp;
    }
}
