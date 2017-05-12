package cz.zcu.swi.fkolenak.goals.helpers;

/**
 * @author rohlik
 */
public interface IGoal extends Comparable<IGoal> {

    /**
     * Rekne, jestli dany cil ma smysl, "pursue" ... jestli ma
     * smysl vykonavat tuto cinnost.
     *
     * @return
     */
    boolean triggering();

    double getPriority();

    void abandon();

    void perform();
}
