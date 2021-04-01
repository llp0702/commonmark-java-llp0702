package upariscommonmarkjava.buildsite.parallel;

import java.util.ArrayList;
import java.util.Set;

public class IndependantQueue {
    private static <Type> ArrayList<Type> getStep(final Set<Needs<Type>> values) {
        final ArrayList<Type> step = new ArrayList<>();
        for(final Needs<Type> need : values) {
            if (need.noConstraint()) {
                values.remove(need);
                step.add(need.getValue());
            }
        }

        for(final Type t : step) {
            for(final Needs<Type> need : values) {
                need.removeConstraint(t);
            }
        }

        return step;
    }

    public static <Type> ArrayList<ArrayList<Type>> generate(final Set<Needs<Type>> constraints) {
        final ArrayList<ArrayList<Type>> steps = new ArrayList<>();

        while (constraints.size() > 0)
            steps.add(getStep(constraints));

        return steps;
    }
}
