package upariscommonmarkjava.buildsite;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DirectoryMdTest {

    //verifie la validité du test de répertoire
    @Test
    public void testValid() throws IOException
    {
        final DirectoryMd correct_site = DirectoryMd.open("");
        assertTrue(correct_site.valid());

        final DirectoryMd incorrect_site = DirectoryMd.open("");
        assertFalse(correct_site.valid());
    }

    @Test
    public void testIndexPresent() throws IOException
    {
        final DirectoryMd correct_index = DirectoryMd.open("");
        assertTrue(correct_index.valid());
        assertThrows(SiteFormatException.class,() ->
            { final DirectoryMd incorrect_index = DirectoryMd.open("");});
    }

    @Test
    public void testTomlPresent() throws IOException
    {
        final DirectoryMd correct_toml = DirectoryMd.open("");
        assertTrue(correct_toml.valid());

        assertThrows(SiteFormatException.class,() ->
        { final DirectoryMd incorrect_toml = DirectoryMd.open("");});
    }

    @Test
    public void testContentPresent() throws IOException
    {
        final DirectoryMd correct_content = DirectoryMd.open("");
        assertTrue(correct_content.valid());

        assertThrows(SiteFormatException.class,() ->
        { final DirectoryMd incorrect_content = DirectoryMd.open("");});
    }

    //vérifie le correcte chargement des fichiers md
    @Test
    public void testOpen() throws IOException
    {
        //Test1
        final DirectoryMd correct_site = DirectoryMd.open("");
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
    public void testGenerateHtml() throws IOException
    {
        final DirectoryMd correct_site = DirectoryMd.open("");
        final DirectoryHtml correct_html = correct_site.generateHtml();

        assertTrue(correct_html.isSimilare(correct_site));

        final DirectoryMd incorrect_site = DirectoryMd.open("");

        assertFalse(correct_html.isSimilare(incorrect_site));
    }
}