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

    /**
     * Map qui contient le nom du fichier et ce qu'il faut recompiler si
     * il vient à être modifié
     */

    private Map<String,Pair<Integer,List<String>>> dependances;

    /**
     * le hashCode global de la hierarchie (du site)
     */
    private int globalHash;
    
    /**
     * Construit une hierarchie en y ajoutant les membres de liste
     * @param contient les éléments pour la hierarchie
     */
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

    /**
     * ajoute la dépendance newDep au fichier de manière
     * transitif
     * @param courant nom du fichier à ajouter la dépendance
     * @param newDep la nouvelle dependance à ajouter
     */
    public void addDep(String courant,String newDep){
        if(!existPath(courant)){
            addNewPath(courant);
        }
        addTransitive(courant, newDep);
        updateTransitivity(courant);
    }

    /**
     * ajoute les dépendances d'un fichier A à B si B dépend de A
     * @param courant fichier à qui ajouter les dépendances d'un autre
     */
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

    /**
     * ajoute simplement newDep dans courant
     * 
     */
    private void addSimpleDep(String courant, String newDep){
        if(!existDepInCour(courant, newDep)){
            getDepCourant(courant).add(newDep);
        }
    }

    /**
     * supprime tous les éléments de e dans la liste de dépendances de courant
     * 
     */
    public void supprAllFromCollection(String courant, List<Path> e){
        for(Path p : e){
            supprDepFromCourant(courant, p.toString());
        }
    }

    /**
     * ajoute tous les éléments de e dans la liste de dépendances de courant
     * 
     */
    public void addFromCollection(String courant, List<Path> e){
        for(Path p : e){
            addDep(courant, p.toString());
        }
    }

    /**
     * supprime toutes les apparitions de courant dans la liste e de fichier
     * 
     */
    public void supprInstanceOfCourant(String courant, List<Path> e){
        for(Path p : e){
            supprDepFromCourant(p.toString(), courant);
        }
    }

    /**
     * supprime la dépendance dep de courant
     * @return true si la suppression a marché
     */
    public boolean supprDepFromCourant(String courant, String dep){
        List<String> liste = getDepCourant(courant);
        return liste.remove(dep);
    }

    /**
     * ajoute un nouveau noeud dans la hierarchie
     * 
     */
    public void addNewPath(String newPath){
        dependances.put(newPath, new Pair<Integer,List<String>>(0,new ArrayList<String>()));
    }

    /**
     * vérifie si le chemin courant existe
     * 
     */
    public boolean existPath(String courant){
        return dependances.containsKey(courant);
    }

    /**
     * vérifie si la dépendance dep existe dans celles de courant
     * 
     */
    public boolean existDepInCour(String courant,String dep){
        return getDepCourant(courant).contains(dep);
    }

    /**
     * retourne les dépendances de courant sinon vide
     * 
     */
    public List<String> getDepCourant(String courant){
        if(existPath(courant)){
            return dependances.get(courant).getSecond();
        }
        return Collections.emptyList();
    }

    /**
     * récupère le hashCode du noeud courant dans la hierarchie
     * @return le hash
     */
    public int getHashCourant(String courant){
        if(existPath(courant)){
            return dependances.get(courant).getFrst();
        }
        return 0;
    }

    /**
     * set le hashCode du noeud courant dans la hierarchie
     * 
     */
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

    public void setGlobalHash(int hash){
        globalHash = hash;
    }

    @Override
    public int hashCode(){
        return globalHash;
    }

    /**
     * récupère ce dont a besoin le noeud courant avant d'être compilé
     * @return la liste des besoins de courant
     */
    public List<String> getNeeds(String courant){
        if(!existPath(courant)){
            return Collections.emptyList();
        }
        List<String> res = new ArrayList<>();
        Iterator<Entry<String, Pair<Integer,List<String>>>> itr = dependances.entrySet().iterator();
        while(itr.hasNext())
        {
            Map.Entry<String, Pair<Integer,List<String>>> entry = itr.next();
            for(String dep : entry.getValue().getSecond()){
                if(dep.equals(courant)){
                    res.add(entry.getKey());
                }
            }
        }
        return res;
    }


    @Override
    public String toString(){
        StringBuffer b = new StringBuffer();
        Iterator<Entry<String, Pair<Integer,List<String>>>> itr = dependances.entrySet().iterator();
        while(itr.hasNext())
        {
            Map.Entry<String, Pair<Integer,List<String>>> entry = itr.next();
            b.append(entry.getKey() + ": \n");
            for(String g : entry.getValue().getSecond()){
                b.append("\t" +g + "\n");
            }
            b.append("///////////////\n");
        }
        return b.toString();
    }
    
}
