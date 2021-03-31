package upariscommonmarkjava.md2html;

import org.apache.commons.io.input.ReaderInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import upariscommonmarkjava.md2html.implementations.CMFile;
import upariscommonmarkjava.md2html.implementations.ConverterMd2Html;
import upariscommonmarkjava.md2html.implementations.TomlFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ConvertMd2HtmlTest {

    public static final String CMEXAMPLE_1_MD = "cmexample1.md";

    ConverterMd2Html converter;
    CMFile cmFile;

    @BeforeEach
    public void init()
    {
        try {
            cmFile = CMFile.fromString(RessourcesAccess.getResourceFile(CMEXAMPLE_1_MD));
            converter = new ConverterMd2Html();
        }catch (IOException ioe)
        {
            fail("Cannot open cm and toml");
        }
    }

    @Test
    void testConvertCm() throws Exception {
        //When
        final String result = converter.parseAndConvert2Html(cmFile);

        //Then
        final InputStream inputStreamResult = new ReaderInputStream(new StringReader(result));
        final HTML5Validator validator = new HTML5Validator(inputStreamResult);
        assertTrue(validator.validate());
    }

    @Test
    void testParseAndConvert2HtmlAndSave() throws IOException {
        //Given
        final String dest = "/tmp/output.html";
        final Path destPath = Paths.get(dest);

        //When
        converter.parseAndConvert2HtmlAndSave(cmFile, destPath);

        //Then
        assertTrue(Files.exists(destPath));
        final InputStream inputStreamResult = Files.newInputStream(destPath);
        final HTML5Validator validator = new HTML5Validator(inputStreamResult);
        assertTrue(validator.validate());
    }
}
