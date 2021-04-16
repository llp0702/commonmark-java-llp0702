package upariscommonmarkjava.parallel;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import upariscommonmarkjava.SimilaireDirectoryTest;
import upariscommonmarkjava.buildsite.SiteFormatException;
import upariscommonmarkjava.buildsite.directoryhtml.DirectoryHtmlParallel;
import upariscommonmarkjava.buildsite.directorymd.DirectoryMdParallel;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class DirectoryHtmlParallelTest {
    private DirectoryMdParallel correct_site;
    private DirectoryHtmlParallel correct_html;

    @BeforeEach
    public void initCorrectSite()
    {
        try
        {
            correct_site = new DirectoryMdParallel(Paths.get("src/test/resources/minimal"));
        }
        catch(SiteFormatException e)
        {
            fail("Cannot open DirectoryMd");
        }
        correct_html = (DirectoryHtmlParallel)correct_site.generateHtml();
    }

    @Test
    void testIsSimilaire(){
        assertTrue(SimilaireDirectoryTest.isSimilare(correct_html,correct_site));
    }

    @Test
    void testSaveAllParallel() {
        assertDoesNotThrow(() ->  correct_html.save(Paths.get("src/test/resources/hierarchie/correct/_output"),true));
        assertDoesNotThrow(() ->  correct_html.save(Paths.get("src/test/resources/hierarchie/correct/_output"),false));
    }

    @Test
    void setNbThreadTest(){
        assertThrows(Error.class, () -> correct_html.setNbThread(-1));
    }

    @Test
    void directoryMdTest(){
        Assertions.assertNotNull(correct_site.getContentBasePath());
        Assertions.assertNotNull(correct_site.getStaticFilesPaths());
        Assertions.assertNotNull(correct_site.getTemplatesPaths());
        Assertions.assertNotNull(correct_site.getTemplateBasePath());
        Assertions.assertNotNull(correct_site.getThemes());
    }
}
