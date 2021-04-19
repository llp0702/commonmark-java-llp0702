package upariscommonmarkjava.buildsite.parallel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class IndependantQueue {
    private IndependantQueue(){}

    /** Vérifie que l'ensemble des contraintes sont bien déclaré et qu'aucune contraintes n'est bloquante
     * @param values L'ensemble des containtes
     * @throws NeedsException si la liste de contrainte n'est pas valide
     */
    private static <TYPE> void valid(final List<Needs<TYPE>> values) throws NeedsException {
        final ArrayList<TYPE> keys = new ArrayList<>();
        values.stream().map(Needs::getValue).forEach(keys::add);

        for(final Needs<TYPE> need : values)
            for (Iterator<TYPE> it = need.constraintIterator(); it.hasNext(); )
                if(!keys.contains(it.next()))
                    throw new NeedsException("Incorrect Key");

    }

    /** Fabrique une liste d'éléments indépendants */
    private static <TYPE> List<TYPE> getStep(final List<Needs<TYPE>> values) {
        final List<TYPE> step = new ArrayList<>();
        for(int i = 0; i < values.size(); i++) {
            final Needs<TYPE> e = values.get(i);
            if (e.noConstraint())
            {
                step.add(e.getValue());
                if(values.remove(e))
                    i--;
            }
        }

        for(final TYPE t : step) {
            for(final Needs<TYPE> need : values) {
                need.removeConstraint(t);
            }
        }

        return step;
    }

    /** fabrique l'ensemble des listes indépendantes */
    public static <TYPE> List<List<TYPE>> generate(final List<Needs<TYPE>> constraints) throws NeedsException {
        valid(constraints);

        final List<List<TYPE>> steps = new ArrayList<>();

        while (!constraints.isEmpty()) {
            steps.add(getStep(constraints));
        }

        return steps;
    }
}
