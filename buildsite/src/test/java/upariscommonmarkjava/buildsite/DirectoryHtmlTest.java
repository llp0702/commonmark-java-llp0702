package upariscommonmarkjava.buildsite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;

import static org.junit.jupiter.api.Assertions.*;

public class DirectoryHtmlTest {
    DirectoryMd correct_site;
    DirectoryHtml correct_html;

    public static boolean isSimilare(DirectoryHtml dh,DirectoryMd d)
    {
        if(d.getPaths().size() != dh.files.size())
            return false;

        for(String path_md : d.getPaths()) {
            if (!dh.files.containsKey(path_md))
                return false;
        }
        return true;
    }

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
        assertTrue(isSimilare(correct_html,correct_site));
    }



    @Test
    public void testSave()
    {
        assertDoesNotThrow(() ->  correct_html.save("src/test/resources/out/correct"));
    }

    @Test
    public void testCreate()
    {
        assertTrue(isSimilare(correct_html,correct_site));
    }
}
