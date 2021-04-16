package upariscommonmarkjava.buildsite;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import upariscommonmarkjava.buildsite.directoryhtml.DirectoryHtml;
import upariscommonmarkjava.buildsite.directorymd.DirectoryMd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
class DirectoryHtmlTest {
    DirectoryMd correct_site;
    DirectoryHtml correct_html;



    public static boolean isSimilar(DirectoryHtml dh, DirectoryMd d)
    {
        if(d.getMdFilesPaths().size() != dh.getInputFilesMdPaths().size())
            return false;
        if(d.getAsciiFilesPaths().size() != dh.getAsciiFilesPaths().size())
            return false;

        for(Path path_md : d.getMdFilesPaths()) {
            if (!dh.getInputFilesMdPaths().contains(path_md))
                return false;
        }
        for(Path path_ascii : d.getAsciiFilesPaths())
            if(!dh.getAsciiFilesPaths().contains(path_ascii))
                return false;

        return true;
    }

    @BeforeEach
    public void initCorrectSite()
    {
        try
        {
            correct_site = DirectoryMd.open(Paths.get("src/test/resources/minimal"));
        }
        catch(SiteFormatException e)
        {
            fail("Cannot open DirectoryMd");
        }
        correct_html = (DirectoryHtml) correct_site.generateHtml();
    }

    @Test
    void testIsSimilar()
    {
        assertTrue(isSimilar(correct_html,correct_site));
    }


    @Test
    void testSave()
    {
        assertDoesNotThrow(() ->  correct_html.save(Paths.get("src/test/resources/hierarchie/correct/_output"),false));
    }

    @Test
    void testCreate()
    {
        assertTrue(isSimilar(correct_html,correct_site));
    }

    @AfterAll
    static void teardown() throws IOException {
        File testFile = new File(".");
        String CURRENT_PATH = testFile.getCanonicalPath() + "/src/test/resources/";
        File firstConvertedFile = new File(CURRENT_PATH + "minimal/content/random.html");
        firstConvertedFile.deleteOnExit();
    }
}

