package upariscommonmarkjava.buildsite;

import org.junit.jupiter.api.Test;
import upariscommonmarkjava.SimilaireDirectoryTest;
import upariscommonmarkjava.buildsite.directoryhtml.DirectoryHtml;
import upariscommonmarkjava.buildsite.directorymd.DirectoryMd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
class DirectoryHtmlTest {
    public void testDirectoryHtml(final String in, final String out)
    {
        try
        {
            final DirectoryMd correct_site = DirectoryMd.open(Paths.get(in));
            final DirectoryHtml correct_html = (DirectoryHtml)correct_site.generateHtml();

            assertNotNull(correct_html);
            assertTrue(SimilaireDirectoryTest.isSimilare(correct_html,correct_site));
            assertDoesNotThrow(() ->  correct_html.save(Paths.get(out),true));
            assertDoesNotThrow(() ->  correct_html.save(Paths.get(out),false));
            assertTrue(SimilaireDirectoryTest.isSimilare(correct_html,correct_site));
        }
        catch(SiteFormatException e)
        {
            fail("Cannot open DirectoryMd");
        }
    }

    @Test
    public void minimalTest(){
        testDirectoryHtml("src/test/resources/minimal","src/test/resources/hierarchie/correct/_output");
        try{ teardown(); } catch (IOException ignore){}
    }

    @Test
    public void minimalTemplateTest(){
        testDirectoryHtml("src/test/resources/mini-templates","src/test/resources/hierarchie/correct/_output2");
    }

    @Test
    public void minimalThemeTest(){
        testDirectoryHtml("src/test/resources/mini-theme","src/test/resources/hierarchie/correct/_output3");
    }

    @Test
    public void miniTemplateTest(){
        testDirectoryHtml("src/test/resources/minimal_template++","src/test/resources/hierarchie/correct/_output4");
    }

    @Test
    public void miniPlusTest(){
        testDirectoryHtml("src/test/resources/minimalPlus","src/test/resources/hierarchie/correct/_output5");
    }


    static void teardown() throws IOException {
        File testFile = new File(".");
        String CURRENT_PATH = testFile.getCanonicalPath() + "/src/test/resources/";
        File firstConvertedFile = new File(CURRENT_PATH + "minimal/content/random.html");
        firstConvertedFile.deleteOnExit();
    }
}

