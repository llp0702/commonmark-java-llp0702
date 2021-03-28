package upariscommonmarkjava.md2html;

import org.apache.commons.io.input.ReaderInputStream;
import org.junit.jupiter.api.Test;
import upariscommonmarkjava.md2html.implementations.CMFile;
import upariscommonmarkjava.md2html.implementations.ConverterMd2Html;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ParserMd2HtmlTest {

    public static final String CMEXAMPLE_1_MD = "cmexample1.md";

    @Test
    void testConvertCm() throws Exception {
        //Given
        CMFile cmFile = CMFile.fromString(RessourcesAccess.getResourceFile(CMEXAMPLE_1_MD));
        ConverterMd2Html converter = new ConverterMd2Html();

        //When
        String result = converter.parseAndConvert2Html(cmFile);

        //Then
        InputStream inputStreamResult = new ReaderInputStream(new StringReader(result));
        HTML5Validator validator = new HTML5Validator(inputStreamResult);
        assertTrue(validator.validate());
    }

    @Test
    void testParseAndConvert2HtmlAndSave() throws IOException {
        //Given
        CMFile cmFile = CMFile.fromString(RessourcesAccess.getResourceFile(CMEXAMPLE_1_MD));
        String dest = "/tmp/output.html";
        Path destPath = Paths.get(dest);
        ConverterMd2Html converter = new ConverterMd2Html();


        //When
        converter.parseAndConvert2HtmlAndSave(cmFile, destPath);

        //Then
        assertTrue(Files.exists(destPath));
        InputStream inputStreamResult = Files.newInputStream(destPath);
        HTML5Validator validator = new HTML5Validator(inputStreamResult);
        assertTrue(validator.validate());
    }
}
