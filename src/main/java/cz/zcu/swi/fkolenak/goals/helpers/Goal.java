package cz.zcu.swi.fkolenak.goals.helpers;

import cz.zcu.swi.fkolenak.SmartHunterBot;
import cz.zcu.swi.fkolenak.helpers.BotReference;
import cz.zcu.swi.fkolenak.helpers.NavigateFunctions;
import cz.zcu.swi.fkolenak.helpers.Paths;
import cz.zcu.swi.fkolenak.helpers.State;

/**
 * @author rohlik
 */
public abstract class Goal extends BotReference implements IGoal {
    protected NavigateFunctions fNavigate;
    protected Paths paths;
    protected State state;

    public Goal(SmartHunterBot bot, NavigateFunctions fNavigate, Paths paths, State state) {
        super(bot);
        this.fNavigate = fNavigate;
        this.paths = paths;
        this.state = state;
    }


    @Override
    public int compareTo(IGoal o) {
        if (o == null)
            return 0;
        return Double.compare(getPriority(), o.getPriority());
    }


}
