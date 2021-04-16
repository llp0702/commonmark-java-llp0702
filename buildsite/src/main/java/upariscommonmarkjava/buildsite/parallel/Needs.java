package upariscommonmarkjava.buildsite.parallel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class Needs<Type> {
    private final Type value;
    private final List<Type> valusNeeded;

    public static <Type> List<Type> toArrayList(Type[] array){
        final ArrayList<Type> elements = new ArrayList<>();
        Collections.addAll(elements, array);
        return elements;
    }

    public Needs(final Type value, final List<Type> valusNeeded){
        this.value = value;
        this.valusNeeded = valusNeeded;
    }

    public Needs(final Type value, final Type... constraints){
        this(value,toArrayList(constraints));
    }

    public final void addNeededValue(Type value){
        this.valusNeeded.add(value);
    }

    public final Type getValue(){
        return this.value;
    }

    public final boolean noConstraint(){
        return this.valusNeeded.isEmpty();
    }

    public final Iterator<Type> constraintIterator(){
        return this.valusNeeded.iterator();
    }

    public final void removeConstraint(final Type c){
        this.valusNeeded.remove(c);
    }
}
