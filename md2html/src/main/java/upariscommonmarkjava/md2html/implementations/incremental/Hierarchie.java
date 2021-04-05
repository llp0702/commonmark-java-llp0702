package upariscommonmarkjava.md2html.implementations.incremental;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;



public class Hierarchie implements Serializable{

    /**
     *
     */
    private static final long serialVersionUID = 591874954233939136L;

    private Map<String,Pair<Integer,List<String>>> dependances;

    
    public Hierarchie(String[] liste){
        dependances = new HashMap<>();
        for(String e : liste){
            dependances.put(e, new Pair<Integer,List<String>>(0,new ArrayList<String>()));
        }
    }

    public Hierarchie(List<Path> liste){
        dependances = new HashMap<>();
        for(Path e : liste){
            dependances.put(e.toString(), new Pair<Integer,List<String>>(0,new ArrayList<String>()));
        }
    }

    
    public Hierarchie(){
        dependances = new HashMap<>();
    }

    public void addDep(String courant,String newDep){
        if(!existPath(courant)){
            addNewPath(courant);
        }
        addTransitive(courant, newDep);
        updateTransitivity(courant);
    }

    private void updateTransitivity(String courant) {
        Iterator<Entry<String, Pair<Integer,List<String>>>> itr = dependances.entrySet().iterator(); 
        while(itr.hasNext()) 
        { 
             Map.Entry<String, Pair<Integer,List<String>>> entry = itr.next(); 
             if(existDepInCour(entry.getKey(), courant)){
                 addDep(entry.getKey(), courant);
             }
        }
    }

    private void addTransitive(String courant,String newDep){
        List<String> transitivity = getDepCourant(newDep);
        if(!transitivity.isEmpty()){
            for(String s : transitivity){
                if(!existDepInCour(courant, s)){
                    addDep(courant, s);
                }
            }
        }
        addSimpleDep(courant, newDep);
    }

    private void addSimpleDep(String courant, String newDep){
        if(!existDepInCour(courant, newDep)){
            getDepCourant(courant).add(newDep);
        }
    }

    public void addNewPath(String newPath){
        dependances.put(newPath, new Pair<Integer,List<String>>(0,new ArrayList<String>()));
    }

    public boolean existPath(String courant){
        return dependances.containsKey(courant);
    }

    public boolean existDepInCour(String courant,String dep){
        return getDepCourant(courant).contains(dep);
    }

    public List<String> getDepCourant(String courant){
        if(existPath(courant)){
            return dependances.get(courant).getSecond();
        }
        return Collections.emptyList();
    }

    public int getHashCourant(String courant){
        if(existPath(courant)){
            return dependances.get(courant).getFrst();
        }
        return 0;
    }

    public void setHashCourant(String courant,int newHash){
        if(!existPath(courant)){
           addNewPath(courant);
        }
        dependances.get(courant).setFrst(newHash);
    }

    public int nombrePath(){
        return dependances.size();
    }

    public void supprPath(String courant){
        dependances.remove(courant);
    }

    public void clearPath(){
        Iterator<Entry<String, Pair<Integer,List<String>>>> itr = dependances.entrySet().iterator(); 
        while(itr.hasNext()) 
        { 
             Map.Entry<String, Pair<Integer,List<String>>> entry = itr.next(); 
             if(entry.getValue().getSecond().size() == 0){
                 itr.remove();
             }
        }
    }
}
