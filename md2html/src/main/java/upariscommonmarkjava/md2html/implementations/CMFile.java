package upariscommonmarkjava.md2html.implementations;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.tomlj.TomlTable;
import upariscommonmarkjava.md2html.interfaces.ICMFile;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Builder
public class CMFile implements ICMFile {

    public static CMFile fromString(@NonNull final String cmString) throws IOException{
        return CMFile.builder().reader(new StringReader(cmString)).build();
    }

    public static CMFile fromPath(@NonNull final Path path) throws IOException {
        return CMFile.builder().reader(Files.newBufferedReader(path)).build();
    }

    Reader reader;


    @Getter @Setter
    List<TomlTable> tomlMetadataLocal;

    @Override
    public Reader getReader(){
        return reader;
    }

    @Override
    public boolean isDraft() {
        for(TomlTable metadataSet:tomlMetadataLocal){
            if(metadataSet==null)continue;
            Boolean isDraft = metadataSet.getBoolean("draft");
            if(isDraft != null && isDraft ){
                return true;
            }
        }
        return false;
    }

}
