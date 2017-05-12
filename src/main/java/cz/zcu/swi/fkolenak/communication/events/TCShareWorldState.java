package cz.zcu.swi.fkolenak.communication.events;

import cz.cuni.amis.pogamut.ut2004.teamcomm.mina.messages.TCMessageData;
import cz.cuni.amis.utils.token.IToken;
import cz.cuni.amis.utils.token.Tokens;
import cz.zcu.swi.fkolenak.communication.classes.WorldState;

/**
 * Created by japan on 11-May-17.
 */
public class TCShareWorldState extends TCMessageData {
    private static final long serialVersionUID = 518962250148387481L;
    private WorldState worldState;


    public static final IToken MESSAGE_TYPE = Tokens.get("TCShareWorldState");


    public TCShareWorldState(WorldState worldState) {
        super(MESSAGE_TYPE);
        this.worldState = worldState;
    }

    public WorldState getWorldState() {
        return worldState;
    }

    public void setWorldState(WorldState worldState) {
        this.worldState = worldState;
    }
}
