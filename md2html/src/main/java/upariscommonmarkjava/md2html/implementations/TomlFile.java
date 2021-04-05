package upariscommonmarkjava.md2html.implementations;

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
import java.util.Optional;

public class TomlFile implements ITOMLFile {
    
    @Getter
    private final Reader reader;

    @Setter
    private TomlParseResult data;

    private Optional<Path> path;

    protected TomlFile(Reader reader,Optional<Path> p) throws IOException
    {
        this.reader = reader;
        data = Toml.parse(reader);
        path = p;
    }

    public TomlParseResult getData()
    {
        return data;
    }

    public static TomlFile fromString(@NonNull final String cmString) throws IOException {
        return new TomlFile(new StringReader(cmString),Optional.empty());
    }

    public static TomlFile fromPath(@NonNull final Path path) throws IOException {
        return new TomlFile(Files.newBufferedReader(path),Optional.of(path));
    }

    @Override
    public String getStringPath() throws IOException{
        if(path.isPresent()){
            return path.get().toString();
        }
        throw new IOException("The TomlFile is from a String");
    }


}
