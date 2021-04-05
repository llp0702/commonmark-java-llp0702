package upariscommonmarkjava.md2html;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import upariscommonmarkjava.md2html.implementations.incremental.Hierarchie;


public class HierarchieTest {
    
    Hierarchie h;
    

    @BeforeEach
    public void createHierarchie(){
        h = new Hierarchie(createListPath());
    }
    
    public String[] createTabPath(){
        String[] p = { "index.md","CommonMark.md","site.md","exemple.md"};
        return p;
    }

    public List<Path> createListPath(){
        Path[] p = { Paths.get("index.md"),Paths.get("CommonMark.md"),Paths.get("site.md"),Paths.get("exemple.md")};
        return Arrays.asList(p);
    }


    @Test
    public void createHierarchieFromTabTest(){
        String[] p = createTabPath();
        Hierarchie h = new Hierarchie(p);
        assertEquals(p.length,h.nombrePath());
    }

    @Test
    public void createHierarchieFromListTest(){
        List<Path> p = createListPath();
        Hierarchie h = new Hierarchie(p);
        assertEquals(p.size(),h.nombrePath());
    }

    @Test
    public void getCourantPathTest(){
        assertNotNull(h.getDepCourant("site.md"));
        assertTrue(h.existPath("site.md"));
        assertNotNull(h.getDepCourant("index.md"));
        assertTrue(h.existPath("index.md"));
        assertNotNull(h.getDepCourant("exemple.md"));
        assertTrue(h.existPath("exemple.md"));
    }

    @Test
    public void addDepTest(){
        h.addDep("site.md", "dependances.md");
        List<String> lp = h.getDepCourant("site.md");
        assertEquals(1, lp.size());
        assertEquals("dependances.md", lp.get(0).toString());
    }

    @Test
    public void addNewPathAndDepTest(){
        h.addDep("bonjour.md", "dependances.md");
        assertEquals(5,h.nombrePath());
        List<String> lp = h.getDepCourant("bonjour.md");
        assertEquals(1, lp.size());
        assertEquals("dependances.md", lp.get(0).toString());
    }

    @Test
    public void addNewPathTest(){
        h.addNewPath("Bonjour.md");
        assertEquals(5,h.nombrePath());
        h.addNewPath("Monjour.md");
        h.addNewPath("Lonjour.md");
        assertEquals(7,h.nombrePath());
        assertTrue(h.existPath("Monjour.md"));
        assertTrue(h.existPath("Bonjour.md"));
        assertTrue(h.existPath("Lonjour.md"));
    }


    @Test
    public void clearPathTest(){
        h.addDep("index.md", "Salut.md");
        h.clearPath();
        assertEquals(1, h.nombrePath());
        assertFalse(h.existPath("site.md"));
        assertFalse(h.existPath("CommonMark.md"));
        assertFalse(h.existPath("exemple.md"));
    }

    @Test
    public void serializationTest(){
        h.addDep("index.md", "Salut.md");
        h.addDep("CommonMark.md", "Salue.md");
        h.addDep("site.md", "Salu.md");
        try {
            FileOutputStream out = new FileOutputStream("src/test/resources/hierarchie.ser");
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(h);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            fail();
        }
        try {
            FileInputStream fichier = new FileInputStream("src/test/resources/hierarchie.ser");
            ObjectInputStream ois = new ObjectInputStream(fichier);
            Hierarchie p = (Hierarchie) ois.readObject();
            assertEquals(4, p.nombrePath());
            assertEquals(1,p.getDepCourant("index.md").size());
            assertEquals("Salut.md",p.getDepCourant("index.md").get(0).toString());
            assertEquals(1,p.getDepCourant("CommonMark.md").size());
            assertEquals("Salue.md",p.getDepCourant("CommonMark.md").get(0).toString());
            assertEquals(1,p.getDepCourant("site.md").size());
            assertEquals("Salu.md",p.getDepCourant("site.md").get(0).toString());
            ois.close(); 
          } catch (java.io.IOException | ClassNotFoundException e) {
            fail();
          }
    }

    @Test
    public void addTransitiveTest(){
        h.addNewPath("dependances.md");
        h.addDep("dependances.md", "moi.md");
        h.addDep("index.md", "dependances.md");
        h.addDep("index.md", "moi.md");
        assertEquals(5,h.nombrePath());
        h.addDep("CommonMark.md", "index.md");
        h.addDep("CommonMark.md", "moi.md");
        List<String> cm = h.getDepCourant("CommonMark.md");
        assertEquals(3,cm.size());
        assertTrue(cm.contains("index.md"));
        assertTrue(cm.contains("dependances.md"));
        assertTrue(cm.contains("moi.md"));
        List<String> ind = h.getDepCourant("index.md");
        assertEquals(2,ind.size());
        assertTrue(ind.contains("dependances.md"));
        assertTrue(ind.contains("moi.md"));
        List<String> moi = h.getDepCourant("dependances.md");
        assertEquals(1,moi.size());
        assertTrue(moi.contains("moi.md"));
        h.addDep("dependances.md", "moi2.md");
        h.addDep("moi.md", "moi3.md");
        List<String> cm2 = h.getDepCourant("CommonMark.md");
        assertEquals(5,cm2.size());
        assertEquals(4,ind.size());
    }

}
