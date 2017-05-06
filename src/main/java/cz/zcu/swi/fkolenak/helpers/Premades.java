package cz.zcu.swi.fkolenak.helpers;

import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by japan on 04-May-17.
 */
public class Premades {
    private Collection<UT2004ItemType> requiredWeapons = new ArrayList<UT2004ItemType>();
    private HashMap<UT2004ItemType,Boolean> generalPref = new LinkedHashMap();
    public Premades(){
        setRequiredWeapons();
        fillGeneralPrefs();
    }

    private void fillGeneralPrefs() {
        generalPref.put(UT2004ItemType.LIGHTNING_GUN,true);
        generalPref.put(UT2004ItemType.MINIGUN,true);
        generalPref.put(UT2004ItemType.SHOCK_RIFLE,true);
        generalPref.put(UT2004ItemType.LINK_GUN,true);
        generalPref.put(UT2004ItemType.FLAK_CANNON,true);
        generalPref.put(UT2004ItemType.ROCKET_LAUNCHER,true);
        generalPref.put(UT2004ItemType.ASSAULT_RIFLE,true);
        generalPref.put(UT2004ItemType.SHIELD_GUN,false);
        generalPref.put(UT2004ItemType.BIO_RIFLE,true);
    }

    public HashMap<UT2004ItemType,Boolean> getGeneralPref(){
        return generalPref;
    }

    private void setRequiredWeapons(){
        if(requiredWeapons.size() == 0){
            UT2004ItemType[] list = {UT2004ItemType.MINIGUN,UT2004ItemType.FLAK_CANNON,UT2004ItemType.SHOCK_RIFLE,UT2004ItemType.LINK_GUN};
            for(UT2004ItemType weapon :  list){
                requiredWeapons.add(weapon);
            }
        }
    }

    public Collection<UT2004ItemType> getRequiredWeapons(){
        return requiredWeapons;
    }


}
