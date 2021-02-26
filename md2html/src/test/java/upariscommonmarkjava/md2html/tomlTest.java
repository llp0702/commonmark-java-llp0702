package upariscommonmarkjava.md2html;

import org.junit.jupiter.api.Test;
import org.tomlj.TomlTable;
import upariscommonmarkjava.md2html.implementations.CMFile;
import upariscommonmarkjava.md2html.implementations.TomlFile;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


class tomlTest {

    public TomlFile getRessource(String filename) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStreamInput = classLoader.getResourceAsStream(filename);
        if(inputStreamInput==null) fail(filename+" not found");

        return TomlFile.fromString(new String(inputStreamInput.readAllBytes()));
    }
    @Test
    void testTOMLparsing() throws IOException {

        TomlFile tomlFile = getRessource("site.toml");
        tomlFile.parse();

        assertEquals(4,tomlFile.getData().size());


    }
}