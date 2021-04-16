package upariscommonmarkjava.md2html;

import lombok.AllArgsConstructor;
import nu.validator.validation.SimpleDocumentValidator;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.InputStream;
import java.util.logging.Logger;

@AllArgsConstructor
public class HTML5Validator {
    private final InputStream document;

    private final MyHtml5ValidatorErrorHandler errorHandler = new MyHtml5ValidatorErrorHandler();

    public boolean validate() {
        Logger logger = Logger.getLogger(HTML5Validator.class.getName());
        SimpleDocumentValidator validator = new SimpleDocumentValidator();
        String schemaUrl = "http://s.validator.nu/html5-all.rnc";

        InputSource source = new InputSource(document);
        try {
            validator.setUpMainSchema(schemaUrl, errorHandler);
            validator.setUpValidatorAndParsers(errorHandler, false, false);
            validator.checkHtmlInputSource(source);
        } catch (Exception e) {
            logger.info("An error occurred señor");
            return false;
        }
        logger.info("An error did not occur señor");
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

