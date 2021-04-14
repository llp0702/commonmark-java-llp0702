package upariscommonmarkjava.md2html.implementations;

import lombok.NonNull;
import org.tomlj.TomlTable;
import upariscommonmarkjava.md2html.interfaces.ICMFile;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class CMFile implements ICMFile {
    public static final Logger logger = Logger.getLogger("CMFile logger");

    protected CMFile(Reader reader,Optional<Path> p)
    {
        this.reader = reader;
        path = p;
    }

    public static CMFile fromString(@NonNull final String cmString) {
        return new CMFile(new StringReader(cmString),Optional.empty());
    }

    public static CMFile fromPath(@NonNull final Path path) throws IOException {
        return new CMFile(Files.newBufferedReader(path),Optional.of(path));
    }

    public void reset()
    {
        try {
            reader.reset();
        }
        catch (IOException ioe){
            logger.warning(ioe.getMessage());
        }
    }

    final Reader reader;

    private Optional<Path> path;

    List<TomlTable> tomlMetadataLocal;

    public void setTomlMetadataLocal(@NonNull List<TomlTable> parseResult){
        this.tomlMetadataLocal = parseResult;
    }

    public List<TomlTable> getTomlMetadataLocal(){
        return tomlMetadataLocal;
    }

    @Override
    public Reader getReader(){
        return reader;
    }

    @Override
    public boolean isDraft() {
        for(TomlTable metadataSet : tomlMetadataLocal){
            if(metadataSet == null)
                continue;

            Boolean isDraft = metadataSet.getBoolean("draft");
            if(isDraft != null && isDraft ){
                return true;
            }
        }
        return false;
    }

    @Override
    public String getStringPath() throws IOException{
        if(path.isPresent()){
            return path.get().toString();
        }
        throw new IOException("The Common Mark File is from a String");
    }

}
