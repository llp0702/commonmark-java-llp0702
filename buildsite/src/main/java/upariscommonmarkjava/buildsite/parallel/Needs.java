package upariscommonmarkjava.buildsite.parallel;

import java.util.Set;

public final class Needs<Type> {
    private final Type value;
    private final Set<Type> valusNeeded;

    public Needs(final Type value, final Set<Type> valusNeeded){
        this.value = value;
        this.valusNeeded = valusNeeded;
    }

    public final Type getValue(){
        return this.value;
    }

    public final boolean noConstraint(){
        return this.valusNeeded.isEmpty();
    }

    public final void removeConstraint(final Type c){
        this.valusNeeded.remove(c);
    }

}
