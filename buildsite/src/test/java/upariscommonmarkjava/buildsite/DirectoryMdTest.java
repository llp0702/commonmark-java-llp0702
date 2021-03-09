package upariscommonmarkjava.buildsite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import upariscommonmarkjava.buildsite.directoryhtml.DirectoryHtml;
import upariscommonmarkjava.buildsite.directoryhtml.IDirectoryHtml;
import upariscommonmarkjava.buildsite.directorymd.DirectoryMd;

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
            incorrect_site = DirectoryMd.open("src/test/resources/other");
        }
        catch(SiteFormatException e)
        {
            fail("Cannot open DirectoryMd");
        }
    }

    @Test
    void testIndexPresent()
    {
        SiteFormatException e = assertThrows(SiteFormatException.class,
                () -> DirectoryMd.open("src/test/resources/no_index"));

        assertEquals("No index.md found ! ", e.getMessage());
    }

    @Test
    void testTomlPresent()
    {
        SiteFormatException e = assertThrows(SiteFormatException.class,
                () -> DirectoryMd.open("src/test/resources/no_toml"));

        assertEquals("No Site.Toml found ! ", e.getMessage());
    }

    @Test
    void testContentPresent()
    {
        SiteFormatException e = assertThrows(SiteFormatException.class,
                () -> DirectoryMd.open("src/test/resources/no_content"));

        assertEquals("No content folder ! ", e.getMessage());
    }

    //vérifie le correcte chargement des fichiers md
    @Test
    void testOpen()
    {
        //Test2
        assertThrows(SiteFormatException.class,
                () -> DirectoryMd.open(""));
    }

    //Vérifie la bonne génération du site
    @Test
    void testGenerateHtml()
    {
        final IDirectoryHtml correct_html = correct_site.generateHtml();
        if(correct_html instanceof DirectoryHtml){
            assertTrue(DirectoryHtmlTest.isSimilare((DirectoryHtml) correct_html,correct_site));
            assertFalse(DirectoryHtmlTest.isSimilare((DirectoryHtml)correct_html,incorrect_site));
        }
    }
}