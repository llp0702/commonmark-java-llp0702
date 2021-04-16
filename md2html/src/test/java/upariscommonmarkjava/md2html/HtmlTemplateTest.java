package upariscommonmarkjava.md2html;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import upariscommonmarkjava.md2html.implementations.CMFile;
import upariscommonmarkjava.md2html.implementations.ConverterMd2Html;
import upariscommonmarkjava.md2html.implementations.TomlFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class HtmlTemplateTest {

    final String directory = System.getProperty("user.dir") + "/src/test/resources/template/";
    CMFile cmFile;
    TomlFile tomlFile;

    @BeforeEach
    public void init()
    {
        try {
            cmFile = CMFile.fromString(RessourcesAccess.getResourceFile("template/cmTemplate1.md"));
            tomlFile = TomlFile.fromString(RessourcesAccess.getResourceFile("template/site.toml"));
        }catch (IOException ioe)
        {
            fail("Cannot open cm and toml");
        }
    }

    private String getResult(final String testName)
    {
        final ArrayList<Path> lp = new ArrayList<>();
        lp.add(Paths.get(directory, testName + "/default.html"));
        return result(lp);
    }

    private String getResultWith(final String testName,final String templateTest)
    {
        final ArrayList<Path> lp = new ArrayList<>();
        lp.add(Paths.get(directory, testName + "/default.html"));
        lp.add(Paths.get(directory, testName + "/" + templateTest + ".html"));
        return result(lp);
    }

    private String result(ArrayList<Path> lp)
    {
        try {
            cmFile.reset();
            return new ConverterMd2Html(tomlFile,lp).parseAndConvert2Html(cmFile);
        }
        catch (IOException ioe)
        {
            fail("Cannot open default");
            return "";
        }
    }

    @Test
    void testHtmlTemplate() throws IOException
    {
        assertEquals("test include",getResultWith("testInclude", "about"));
        assertEquals("<ul><li>val : test_1</li><li>test2 : false</li><li>test : true</li><li>tab : <ul><li>1</li><li>2</li><li>3</li></ul></li></ul>",getResult("testAll"));

        assertEquals("test_1",getResult("testVariable"));
        assertEquals("test_2",getResult("testGlobalVariable"));
    }

    @Test
    void testAdvancedHtmlTemplate() throws IOException
    {
        assertEquals("test_3",getResult("testIf"));
        assertEquals("test_4",getResult("testNotIf"));
        assertEquals("test_5",getResult("testNotGlobalIf"));
        assertEquals("test_6",getResult("testElse"));
        assertEquals("test_7",getResult("testElseIf"));
        assertEquals("test_8",getResult("testNotElseIf"));
        assertEquals("testtesttest",getResult("testFor"));
        assertEquals("test0test1test2test3",getResult("testForVar"));
        assertEquals("v1,v2,",getResult("testForMap"));
        assertEquals("1,2;0,7;",getResult("testForMatrice"));
    }
}