package upariscommonmarkjava.buildsite.parallel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/** Cette classe premet de structurer des constraintes et fonctionne de pair avec IndependantQueue
 *      value représente l'éléments qui possède des contraintes
 *      valusNeeded représente l'ensemble des éléments qui sont des contraintes pour value
 */
public final class Needs<Type> {
    private final Type value;
    private final List<Type> valusNeeded;

    /** Transforme un tableau en une liste dynamique */
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

    /** Ajout d'une containte */
    public final void addNeededValue(Type value){
        this.valusNeeded.add(value);
    }

    /** Récupération d'une clef */
    public final Type getValue(){
        return this.value;
    }

    /** Renvoie vrai si l'élément ne possède aucune contrainte */
    public final boolean noConstraint(){
        return this.valusNeeded.isEmpty();
    }

    /** Renvoie un itérateur pour la liste d'élements contraignants */
    public final Iterator<Type> constraintIterator(){
        return this.valusNeeded.iterator();
    }

    /** Enlève une contrainte */
    public final void removeConstraint(final Type c){
        this.valusNeeded.remove(c);
    }
}
