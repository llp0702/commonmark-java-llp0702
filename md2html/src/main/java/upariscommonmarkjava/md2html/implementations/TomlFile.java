package upariscommonmarkjava.md2html.implementations;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
@Builder
public class TomlFile implements ITOMLFile {
    @Getter
    private Reader reader;
    @Getter
    @Setter
    private TomlParseResult data;



    public static TomlFile fromString(@NonNull final String cmString) throws IOException {
        return TomlFile.builder().reader(new StringReader(cmString)).build();
    }

    public static TomlFile fromPath(@NonNull final Path path) throws IOException {
        return TomlFile.builder().reader(Files.newBufferedReader(path)).build();
    }

    public void parse() throws IOException {
        data = Toml.parse(reader);
    }


}
