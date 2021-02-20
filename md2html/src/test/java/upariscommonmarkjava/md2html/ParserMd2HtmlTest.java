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

import java.io.InputStream;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ParserMd2HtmlTest {

    @Test
    public void testParseCm() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStreamInput = classLoader.getResourceAsStream("cmexample1.md");
        if(inputStreamInput==null) fail("cmexample1.md not found");
        CMFile cmFile = CMFile.fromString(new String(inputStreamInput.readAllBytes()));
        ConverterMd2Html parser = new ConverterMd2Html();
        String result = parser.parseAndRenderToString(cmFile);
        InputStream inputStreamResult = new ReaderInputStream(new StringReader(result));
        HTML5Validator validator = new HTML5Validator(inputStreamResult);
        assertTrue(validator.validate());
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
