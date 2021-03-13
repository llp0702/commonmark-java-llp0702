package upariscommonmarkjava.buildsite.directoryhtml;

import java.io.IOException;
import java.nio.file.Path;

public interface IDirectoryHtml {
    void save(Path path) throws IOException;
    void saveAll(final Path targetBasePath) throws IOException;
}
