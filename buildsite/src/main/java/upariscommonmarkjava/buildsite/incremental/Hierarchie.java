package upariscommonmarkjava.buildsite.incremental;

import java.io.Serializable;
import java.util.ArrayList;
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

    private Map<String,List<String>> dependances;

    public Hierarchie(String[] liste){
        dependances = new HashMap<>();
        for(String e : liste){
            dependances.put(e, new ArrayList<String>());
        }
    }

    public void addDep(String courant,String newDep){
        if(!existPath(courant)){
            addNewPath(courant);
        }
        dependances.get(courant).add(newDep);
    }

    public void addNewPath(String newPath){
        dependances.put(newPath, new ArrayList<String>());
    }

    public boolean existPath(String courant){
        return dependances.containsKey(courant);
    }

    public List<String> getCourant(String courant){
        return dependances.get(courant);
    }

    public int nombrePath(){
        return dependances.size();
    }

    public void clearPath(){
        Iterator<Entry<String, List<String>>> itr = dependances.entrySet().iterator(); 
        while(itr.hasNext()) 
        { 
             Map.Entry<String, List<String>> entry = itr.next(); 
             if(entry.getValue().size() == 0){
                 itr.remove();
             }
        }
    }
}
