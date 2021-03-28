package upariscommonmarkjava.md2html;

import org.junit.jupiter.api.Test;
import upariscommonmarkjava.md2html.implementations.TomlFile;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


class TomlTest {
    @Test
    void testTOMLparsing() throws IOException {

        TomlFile tomlFile = TomlFile.fromString(RessourcesAccess.getResourceFile("site.toml"));
        tomlFile.parse();

        assertEquals(4,tomlFile.getData().size());
    }
}