package upariscommonmarkjava.md2html.implementations;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import upariscommonmarkjava.md2html.interfaces.ICMFile;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

@Builder
@Slf4j
public class CMFile implements ICMFile {

    public static CMFile fromString(final String cmString) throws IOException{
        return CMFile.builder().reader(new StringReader(cmString)).build();
    }

    public static CMFile fromPath(final Path path) throws IOException {
        return CMFile.builder().reader(Files.newBufferedReader(path)).build();
    }

    Reader reader;

    @Override
    public Reader getReader(){
        return reader;
    }

}
