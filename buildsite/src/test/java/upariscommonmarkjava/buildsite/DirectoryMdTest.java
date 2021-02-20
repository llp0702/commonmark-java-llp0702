package upariscommonmarkjava.buildsite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DirectoryMdTest {

    DirectoryMd correct_site;
    DirectoryMd incorrect_site;

    @BeforeEach
    public void init()
    {
        try
        {
            DirectoryMd correct_site = DirectoryMd.open("");
            DirectoryMd incorrect_site = DirectoryMd.open("");
        }
        catch(SiteFormatException e)
        {
            fail("Cannot open DirectoryMd");
        }
    }

    //verifie la validité du test de répertoire
    @Test
    public void testValid()
    {
        assertTrue(correct_site.valid());
        assertFalse(incorrect_site.valid());
    }

    @Test
    public void testIndexPresent()
    {
        try
        {
            final DirectoryMd correct_index = DirectoryMd.open("");
            assertTrue(correct_index.valid());
            assertThrows(SiteFormatException.class,() ->
            { final DirectoryMd incorrect_index = DirectoryMd.open("");});
        }
        catch(SiteFormatException e)
        {
            fail("Cannot open DirectoryMd");
        }
    }

    @Test
    public void testTomlPresent()
    {
        try
        {
            final DirectoryMd correct_toml = DirectoryMd.open("");
            assertTrue(correct_toml.valid());

            assertThrows(SiteFormatException.class,() ->
            { final DirectoryMd incorrect_toml = DirectoryMd.open("");});
        }
        catch(SiteFormatException e)
        {
            fail("Cannot open DirectoryMd");
        }
    }

    @Test
    public void testContentPresent()
    {
        try {
            final DirectoryMd correct_content = DirectoryMd.open("");
            assertTrue(correct_content.valid());

            assertThrows(SiteFormatException.class,() ->
            { final DirectoryMd incorrect_content = DirectoryMd.open("");});
        }
        catch(SiteFormatException e)
        {
            fail("Cannot open DirectoryMd");
        }
    }

    //vérifie le correcte chargement des fichiers md
    @Test
    public void testOpen()
    {
        //Test1
        assertTrue(correct_site.valid());

        //Test2
        assertThrows(SiteFormatException.class,() ->
            {DirectoryMd incorrect_site_toml = DirectoryMd.open("");});

        assertThrows(SiteFormatException.class,() ->
            {DirectoryMd incorrect_site_index = DirectoryMd.open("");});

        assertThrows(SiteFormatException.class,() ->
            {DirectoryMd incorrect_site_mdformat = DirectoryMd.open("");});

        assertThrows(SiteFormatException.class,() ->
            {DirectoryMd incorrect_site_content = DirectoryMd.open("");});
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