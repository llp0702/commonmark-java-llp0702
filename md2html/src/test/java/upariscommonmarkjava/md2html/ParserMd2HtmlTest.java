package upariscommonmarkjava.md2html;

import lombok.AllArgsConstructor;
import nu.validator.validation.SimpleDocumentValidator;
import org.apache.commons.io.input.ReaderInputStream;
import org.junit.jupiter.api.Test;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
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

public class ParserMd2HtmlTest {

    public static final String CMEXAMPLE_1_MD = "cmexample1.md";

    @Test
    public void testConvertCm() throws Exception {
        //Given
        CMFile cmFile = getCMResourceFile(CMEXAMPLE_1_MD);
        ConverterMd2Html converter = new ConverterMd2Html();

        //When
        String result = converter.parseAndConvert2Html(cmFile);

        //Then
        InputStream inputStreamResult = new ReaderInputStream(new StringReader(result));
        HTML5Validator validator = new HTML5Validator(inputStreamResult);
        assertTrue(validator.validate());
    }

    @Test
    public void testParseAndConvert2HtmlAndSave() throws IOException {
        //Given
        CMFile cmFile = getCMResourceFile(CMEXAMPLE_1_MD);
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

    private CMFile getCMResourceFile(final String filename) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStreamInput = classLoader.getResourceAsStream(filename);
        if(inputStreamInput==null) fail(filename+" not found");
        return CMFile.fromString(new String(inputStreamInput.readAllBytes()));
    }

    @AllArgsConstructor
    public static class HTML5Validator {
        private final InputStream document;

        private final MyHtml5ValidatorErrorHandler errorHandler = new MyHtml5ValidatorErrorHandler();

        public boolean validate() {

            SimpleDocumentValidator validator = new SimpleDocumentValidator();
            String schemaUrl = "http://s.validator.nu/html5-all.rnc";

            InputSource source = new InputSource(document);
            try {
                validator.setUpMainSchema(schemaUrl, errorHandler);
                validator.setUpValidatorAndParsers(errorHandler, false, false);
                validator.checkHtmlInputSource(source);
            } catch (Exception e) {
                return false;
            }
            return !errorHandler.hasErrors();
        }

        private static class MyHtml5ValidatorErrorHandler implements ErrorHandler {
            int err = 0;

            @Override
            public void warning(SAXParseException exception) throws SAXException {
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                err++;
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                err++;
            }

            public boolean hasErrors() {
                return err > 0;
            }
        }
    }

}
