package upariscommonmarkjava.md2html.implementations.incremental;

import java.io.Serializable;


public class Pair<K,V> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5297164008405459491L;
    
    private K frst;

    private V scnd;

    public Pair(K object, V value){
        frst = object;
        scnd = value;
    }

    public V getSecond(){
        return scnd;
    }

    public K getFrst(){
        return frst;
    }

    public void setFrst(K one){
        frst = one;
    }

    public void setScnd(V two){
        scnd = two;
    }
}
