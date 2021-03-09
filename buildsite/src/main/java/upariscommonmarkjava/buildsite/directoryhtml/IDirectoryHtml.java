package upariscommonmarkjava.buildsite.directoryhtml;

import java.io.IOException;

public interface IDirectoryHtml {
    void save(String path, String dir) throws IOException;
    void save(String path) throws IOException;
}
