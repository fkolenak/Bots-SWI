package cz.zcu.swi.fkolenak.communication.events;

import cz.cuni.amis.pogamut.ut2004.teamcomm.mina.messages.TCMessageData;
import cz.cuni.amis.utils.token.IToken;
import cz.cuni.amis.utils.token.Tokens;
import cz.zcu.swi.fkolenak.communication.classes.RequestHelpDefense;
import cz.zcu.swi.fkolenak.communication.classes.TimeStampedObject;

/**
 * Created by japan on 12-May-17.
 */
public class TCRequestHelpDefense extends TCMessageData {

    public static final IToken MESSAGE_TYPE = Tokens.get("TCRequestHelpDefense");
    private static final long serialVersionUID = 192407484467130100L;

    private TimeStampedObject<RequestHelpDefense> helpDefense;

    public TCRequestHelpDefense(TimeStampedObject<RequestHelpDefense> helpDefense) {
        super(MESSAGE_TYPE);
        this.helpDefense = helpDefense;
    }


    public TimeStampedObject<RequestHelpDefense> getHelpDefense() {
        return helpDefense;
    }
}
