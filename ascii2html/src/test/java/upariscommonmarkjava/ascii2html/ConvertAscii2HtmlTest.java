package upariscommonmarkjava.ascii2html;

import org.apache.commons.io.FileUtils;
import org.jruby.embed.io.ReaderInputStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import upariscommonmarkjava.htmlvalidator.HTML5Validator;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConvertAscii2HtmlTest {

    private static final String FILE_EXAMPLE_1_ASCII = "ascii-files/example1.adoc";
    private static final String FILE_EXAMPLE_2_ASCII = "ascii-files/example2.adoc";
    private static final String FILE_SOLUTION_1_HTML = "html-files/solution1.html";
    private static final String FILE_SOLUTION_2_HTML = "html-files/solution2.html";
    private static String CURRENT_PATH;
    Ascii2HtmlMain converter;

    @BeforeAll
    public void init() throws Exception {
        File testFile = new File(".");
        try {
            CURRENT_PATH = testFile.getCanonicalPath() + "/src/test/resources/";
            converter = new Ascii2HtmlMain();
        } catch (IOException ioe) {
            throw new Exception(ioe);
        }
    }

    private String sanitiseHTML(String inputFile) {
        return inputFile.replace("\n", "")
                .replace("\r", "");
    }

    @Test
    void testConvertToString() throws Exception {
        String testContent = Files.readString(Paths.get(CURRENT_PATH + FILE_EXAMPLE_1_ASCII));
        String toHTML = new Ascii2HtmlMain().convertAsString(testContent);
        String solutionContent = Files.readString(Paths.get(CURRENT_PATH + FILE_SOLUTION_1_HTML));
        boolean validation = validateString(toHTML);
        assertEquals(sanitiseHTML(solutionContent), sanitiseHTML(toHTML));
        assert(validation);
    }

    @Test
    void testConvertToFile() throws Exception {
        Path pathToInput = Paths.get(CURRENT_PATH + FILE_EXAMPLE_1_ASCII);
        String pathToConvertedOutput = CURRENT_PATH + "ascii-files/example2.html";
        String testContent = Files.readString(pathToInput);
        converter.convertAs(testContent, pathToConvertedOutput);
        File convertedFile = new File(pathToConvertedOutput);
        assert(convertedFile.exists());
        assert(validateString(Files.readString(Paths.get(pathToConvertedOutput))));
    }

    @Test
    void testConvertFileToFile() throws Exception {
        File inputFile = new File(CURRENT_PATH + FILE_EXAMPLE_2_ASCII);
        String solutionOutput = Files.readString(Paths.get(CURRENT_PATH + FILE_SOLUTION_2_HTML));
        converter.convert(inputFile);
        File convertedFile = new File(CURRENT_PATH + "ascii-files/example2.html");
        assert(convertedFile.exists());
        String convertedOutput = Files.readString(Paths.get(CURRENT_PATH + "ascii-files/example2.html"));
        assertEquals(solutionOutput, convertedOutput);
        //assert(validateString(convertedOutput));
    }

    private boolean validateString(String result) {
        InputStream inputStreamResult = new ReaderInputStream(new StringReader(result));
        HTML5Validator validator = new HTML5Validator(inputStreamResult);
        return validator.validate();
    }

    @AfterAll
    public void teardown() {
        File firstConvertedFile = new File(CURRENT_PATH + "ascii-files/example2.html");
        firstConvertedFile.deleteOnExit();
    }

}