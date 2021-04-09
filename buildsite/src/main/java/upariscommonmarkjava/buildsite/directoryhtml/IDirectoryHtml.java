package upariscommonmarkjava.buildsite.directoryhtml;

import java.io.IOException;
import java.nio.file.Path;

public interface IDirectoryHtml {
    void save(Path path,boolean all) throws IOException;
}
