import nu.validator.validation.SimpleDocumentValidator;
import org.iso_relax.ant.ErrorHandlerImpl;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class HTML5Validator {
    public void validate() throws FileNotFoundException {
        SimpleDocumentValidator validator = new SimpleDocumentValidator();
        String schemaUrl = "http://s.validator.nu/html5-all.rnc";
        InputSource source = new InputSource(new FileReader("/tmp/testfile"));
        ErrorHandler errorHandler = new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {

            }

            @Override
            public void error(SAXParseException exception) throws SAXException {

            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {

            }
        };
        try {
            validator.setUpMainSchema(schemaUrl, errorHandler);
            validator.setUpValidatorAndParsers(errorHandler, false, false);
            validator.checkHtmlInputSource(source);
        } catch (SAXException e) {
            // Ignore - Let XMLErrorHandler handle it
        } catch (SimpleDocumentValidator.SchemaReadException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
