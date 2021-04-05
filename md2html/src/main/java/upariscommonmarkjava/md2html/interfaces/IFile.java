package upariscommonmarkjava.md2html.interfaces;

import java.io.IOException;
import java.io.Reader;

public interface IFile {
    Reader getReader();
    String getStringPath() throws IOException;
}
