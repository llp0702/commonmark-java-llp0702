package upariscommonmarkjava.buildsite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DirectoryMdTest {

    DirectoryMd correct_site;
    DirectoryMd incorrect_site;

    @BeforeEach
    public void init()
    {
        try
        {
            correct_site = DirectoryMd.open("src/test/resources/minimal");
            incorrect_site = DirectoryMd.open("src/test/resources/minimal");
        }
        catch(SiteFormatException e)
        {
            fail("Cannot open DirectoryMd");
        }
    }

    @Test
    public void testIndexPresent()
    {
        SiteFormatException e = assertThrows(SiteFormatException.class,
                () -> DirectoryMd.open("src/test/resources/no_index"));

        assertEquals("No index.md founded ! ", e.getMessage());
    }

    @Test
    public void testTomlPresent()
    {
        SiteFormatException e = assertThrows(SiteFormatException.class,
                () -> DirectoryMd.open("src/test/resources/no_toml"));

        assertEquals("No Site.Toml founded ! ", e.getMessage());
    }

    @Test
    public void testContentPresent()
    {
        SiteFormatException e = assertThrows(SiteFormatException.class,
                () -> DirectoryMd.open("src/test/resources/no_content"));

        assertEquals("No content folder ! ", e.getMessage());
    }

    //vérifie le correcte chargement des fichiers md
    @Test
    public void testOpen()
    {
        //Test2
        assertThrows(SiteFormatException.class,
                () -> DirectoryMd.open(""));

        assertThrows(SiteFormatException.class,
                () -> DirectoryMd.open(""));

        assertThrows(SiteFormatException.class,
                () -> DirectoryMd.open(""));

        assertThrows(SiteFormatException.class,
                () -> DirectoryMd.open(""));
    }

    //Vérifie la bonne génération du site
    @Test
    public void testGenerateHtml()
    {
        final DirectoryHtml correct_html = correct_site.generateHtml();
        assertTrue(correct_html.isSimilare(correct_site));
        assertFalse(correct_html.isSimilare(incorrect_site));
    }
}