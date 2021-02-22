package upariscommonmarkjava.buildsite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class DirectoryHtmlTest {
    DirectoryMd correct_site;
    DirectoryHtml correct_html;

    @BeforeEach
    public void initCorrectSite()
    {
        try
        {
            correct_site = DirectoryMd.open("src/test/resources/minimal");
        }
        catch(SiteFormatException e)
        {
            fail("Cannot open DirectoryMd");
        }
        correct_html = correct_site.generateHtml();
    }

    @Test
    public void testIsSimilare()
    {
        assertTrue(correct_html.isSimilare(correct_site));

        final File folder = new File("");

        assertTrue(correct_html.isSimilare(folder));
    }

    @Test
    public void testSave()
    {
        String path = "";

        correct_html.save(path);
        final File folder = new File(path);

        //TODO
    }

    @Test
    public void testCreate()
    {
        assertTrue(correct_html.isSimilare(correct_site));
    }
}
