package upariscommonmarkjava.md2html;

import upariscommonmarkjava.md2html.implementations.CMFile;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.fail;

public class RessourcesAccess {
    public static String getResourceFile(final String filename) throws IOException {
        ClassLoader classLoader = RessourcesAccess.class.getClassLoader();
        InputStream inputStreamInput = classLoader.getResourceAsStream(filename);

        if(inputStreamInput == null)
            fail(filename+" not found");

        return new String(inputStreamInput.readAllBytes());
    }
}
