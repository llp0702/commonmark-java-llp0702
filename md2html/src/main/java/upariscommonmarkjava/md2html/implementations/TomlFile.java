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

public class TomlFile implements ITOMLFile {
    @Getter
    private final Reader reader;

    @Setter
    private TomlParseResult data;

    protected TomlFile(Reader reader) throws IOException
    {
        this.reader = reader;
        data = Toml.parse(reader);
    }

    public TomlParseResult getData()
    {
        return data;
    }

    public static TomlFile fromString(@NonNull final String cmString) throws IOException {
        return new TomlFile(new StringReader(cmString));
    }

    public static TomlFile fromPath(@NonNull final Path path) throws IOException {
        return new TomlFile(Files.newBufferedReader(path));
    }
}
