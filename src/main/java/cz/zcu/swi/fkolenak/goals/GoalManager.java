package cz.zcu.swi.fkolenak.goals;

import cz.cuni.amis.utils.Cooldown;
import cz.zcu.swi.fkolenak.SmartHunterBot;
import cz.zcu.swi.fkolenak.goals.helpers.IGoal;
import cz.zcu.swi.fkolenak.helpers.LetKnow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author rohlik, fkolenak
 */
public class GoalManager {
    protected final List<IGoal> goals = new ArrayList<IGoal>();
    protected IGoal currentGoal = null;
    protected final SmartHunterBot bot;

    protected Cooldown cool = new Cooldown(500);

    public GoalManager(SmartHunterBot bot) {
        this.bot = bot;
    }

    public void addGoal(IGoal goal) {
        goals.add(goal);
        Collections.sort(goals);
    }

    public IGoal executeGoalWithHighestPriority() {
        for (int i = goals.size() - 1; i >= 0; --i) {
            if (goals.get(i).triggering()) {
                if (currentGoal == null) {
                    currentGoal = goals.get(i);
                } else {
                    if (currentGoal != goals.get(i)) {
                        currentGoal.abandon();
                        currentGoal = goals.get(i);
                    }
                }
                break;
            }
        }
        // mame vybrany korektni cil, ktery nasledujeme
        if (cool.isCool()) {
            cool.use();
            bot.getLog().info(
                    String.format("Chosen goal pri %.2f: %s",
                            currentGoal.getPriority(), currentGoal.getClass().getSimpleName()));
            LetKnow.debugGoal(bot, "Goal", "[" + currentGoal.getClass().getSimpleName() + "]");
        }
        currentGoal.perform();

        //setName("CTFBot [" + currentGoal.getClass().getSimpleName() + "]");

        return currentGoal;
    }

    public void reset() {
        currentGoal.abandon();
        currentGoal = null;
    }

    public IGoal getCurrentGoal() {
        return currentGoal;
    }
}
