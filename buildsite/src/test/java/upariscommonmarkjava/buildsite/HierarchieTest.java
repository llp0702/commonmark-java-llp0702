package upariscommonmarkjava.buildsite;

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
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import upariscommonmarkjava.buildsite.incremental.Hierarchie;

public class HierarchieTest {
    
    Hierarchie h;
    

    @BeforeEach
    public void createHierarchie(){
        h = new Hierarchie(createListPath());
    }
    
    public String[] createListPath(){
        String[] p = { "index.md","CommonMark.md","site.md","exemple.md"};
        return p;
    }


    @Test
    public void createHierarchieTest(){
        String[] p = createListPath();
        Hierarchie h = new Hierarchie(p);
        assertEquals(p.length,h.nombrePath());
    }

    @Test
    public void getCourantPathTest(){
        assertNotNull(h.getCourant("site.md"));
        assertTrue(h.existPath("site.md"));
        assertNotNull(h.getCourant("index.md"));
        assertTrue(h.existPath("index.md"));
        assertNotNull(h.getCourant("exemple.md"));
        assertTrue(h.existPath("exemple.md"));
    }

    @Test
    public void addDepTest(){
        h.addDep("site.md", "dependances.md");
        List<String> lp = h.getCourant("site.md");
        assertEquals(1, lp.size());
        assertEquals("dependances.md", lp.get(0).toString());
    }

    @Test
    public void addNewPathAndDepTest(){
        h.addDep("bonjour.md", "dependances.md");
        assertEquals(5,h.nombrePath());
        List<String> lp = h.getCourant("bonjour.md");
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
            assertEquals(1,p.getCourant("index.md").size());
            assertEquals("Salut.md",p.getCourant("index.md").get(0).toString());
            assertEquals(1,p.getCourant("CommonMark.md").size());
            assertEquals("Salue.md",p.getCourant("CommonMark.md").get(0).toString());
            assertEquals(1,p.getCourant("site.md").size());
            assertEquals("Salu.md",p.getCourant("site.md").get(0).toString());
            ois.close(); 
          } catch (java.io.IOException | ClassNotFoundException e) {
            fail();
          }
    }

}
