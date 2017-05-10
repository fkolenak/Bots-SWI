package cz.zcu.swi.fkolenak.states;

import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.zcu.swi.fkolenak.SmartHunterBot;
import cz.zcu.swi.fkolenak.helpers.BotReference;
import cz.zcu.swi.fkolenak.helpers.NavigateFunctions;
import cz.zcu.swi.fkolenak.helpers.Paths;
import cz.zcu.swi.fkolenak.helpers.State;

import java.util.List;

/**
 * Created by japan on 08-May-17.
 */
public class StateReady extends BotReference {
    private NavigateFunctions fNavigate;
    private Paths paths;

    public StateReady(SmartHunterBot bot, NavigateFunctions fNavigate, Paths paths) {
        super(bot);
        this.fNavigate = fNavigate;
        this.paths = paths;
    }

    private State state;

    public boolean decide(State state) {
        this.state = state;
        // TODO change state
        if (getCTF().isEnemyFlagHome()) {
            if (state != State.FLAG_STEAL) {
                this.state = State.FLAG_STEAL;
                // TODO form a group
                if (!getNavigation().isNavigating()) {
                    double distance = getInfo().getNearestNavPoint().getLocation().getDistance(getCTF().getOurBase().getLocation());
                    if (getInfo().getNearestNavPoint().equals(getCTF().getOurBase()) || distance < 100) {
                        List<List<NavPoint>> paths = this.paths.getEnemyBasePaths();
                        fNavigate.navigateTo(paths.get(getBot().getRandom().nextInt(paths.size())));
                    } else {
                        // TODO pokud prestreli je na tom bode ale nejblizsi muze byt neco jineho
                        fNavigate.navigateTo(getCTF().getOurBase());
                    }
                    return true;
                }
            }
            return true;
        }
        if (getCTF().isBotCarryingEnemyFlag()) {
            if (!getNavigation().isNavigating()) {
                List<List<NavPoint>> paths = this.paths.getOurBasePaths();
                fNavigate.navigateTo(paths.get(getBot().getRandom().nextInt(paths.size())));
                // TODO cover me
                return true;
            } else if (isEnemyFlagAtOurHome() && !getCTF().canBotScore()) {
                // TODO get our flag

                return true;
            }
        }
        if (getCTF().isEnemyFlagDropped()) {
            fNavigate.navigateTo(getCTF().getEnemyFlag().getLocation());
            return true;
        }
        if (getCTF().isEnemyFlagHeld()) {
            // TODO get info about flag location
            //navigation.navigate(navPoints.getNearestNavPoint(ctf.getEnemyFlag().getLocation()));
           /* if (isEnemyFlagAtOurHome()) {
                // TODO
            }*/
        }

        return false;
    }

    /**
     * Is enemy flag at our base
     *
     * @return is enemy flag at home
     */
    private boolean isEnemyFlagAtOurHome() {
        return true;
        // return navPoints.getNearestNavPoint(ctf.getEnemyFlag().getLocation()).equals(ctf.getOurBase());
    }
}
