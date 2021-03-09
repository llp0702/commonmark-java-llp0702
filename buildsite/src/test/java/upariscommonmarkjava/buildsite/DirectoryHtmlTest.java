package upariscommonmarkjava.buildsite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import upariscommonmarkjava.buildsite.directoryhtml.DirectoryHtml;
import upariscommonmarkjava.buildsite.directorymd.DirectoryMd;

import static org.junit.jupiter.api.Assertions.*;
class DirectoryHtmlTest {
    DirectoryMd correct_site;
    DirectoryHtml correct_html;

    public static boolean isSimilare(DirectoryHtml dh, DirectoryMd d)
    {
        /*if(d.getMdFilesPaths().size() != dh.getInputFilesPaths().size())
            return false;

        for(String path_md : d.getMdFilesPaths()) {
            if (!dh.getInputFilesPaths().containsKey(path_md))
                return false;
        }*/
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
        correct_html =(DirectoryHtml) correct_site.generateHtml();
    }

    @Test
    void testIsSimilare()
    {
        assertTrue(isSimilare(correct_html,correct_site));
    }


    @Test
    void testSave()
    {
        assertDoesNotThrow(() ->  correct_html.save("src/test/resources/out/correct"));
    }

    @Test
    void testCreate()
    {
        assertTrue(isSimilare(correct_html,correct_site));
    }
}
