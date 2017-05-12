package cz.zcu.swi.fkolenak.goals;

import cz.zcu.swi.fkolenak.SmartHunterBot;
import cz.zcu.swi.fkolenak.goals.low.GoalStealEnemyFlag;
import cz.zcu.swi.fkolenak.helpers.BotReference;
import cz.zcu.swi.fkolenak.helpers.NavigateFunctions;
import cz.zcu.swi.fkolenak.helpers.Paths;
import cz.zcu.swi.fkolenak.helpers.State;

/**
 * Created by japan on 08-May-17.
 */
public class StateReady extends BotReference {
    private NavigateFunctions fNavigate;
    private Paths paths;

    private GoalStealEnemyFlag stealFlag;

    public StateReady(SmartHunterBot bot, NavigateFunctions fNavigate, Paths paths) {
        super(bot);
        this.fNavigate = fNavigate;
        this.paths = paths;

        stealFlag = new GoalStealEnemyFlag(bot, fNavigate, paths, state);
    }

    private State state;

    public void perform() {
        this.state = state;
        // TODO change state

        if (this.state.getCurrentStateHigh() != State.HIGH.READY) {
            return;
        }

        // If our team doesn't have enemy flag, go for it
       /*
        if (getCTF().isEnemyFlagHome()) {
            stealFlag.decide();
        }*/
        if (getCTF().isEnemyFlagHeld() && getCTF().isBotCarryingEnemyFlag()) {
          /*  if (!getNavigation().isNavigating()) {
                List<List<NavPoint>> paths = this.paths.getOurBasePaths();
                fNavigate.navigateTo(paths.get(getBot().getRandom().nextInt(paths.size())));
                state.setCurrentStateLow(State.LOW.FLAG_STEALING);
                // TODO cover me
                return ;
            } else if (!getCTF().canBotScore()) {
                // TODO get our flag
                // This bot just waits until teammates returns our flag
                return ;
            }*/
        }
        if (getCTF().isEnemyFlagDropped()) {
            // TODO get position
            fNavigate.navigateTo(getCTF().getEnemyFlag().getLocation());
            return;
        }
        if (getCTF().isEnemyFlagHeld()) {
            // TODO go to location
            // TODO get info about flag location
            //navigation.navigate(navPoints.getNearestNavPoint(ctf.getEnemyFlag().getLocation()));
           /* if (isEnemyFlagAtOurHome()) {
                // TODO
            }*/
        }

        return;
    }
}
