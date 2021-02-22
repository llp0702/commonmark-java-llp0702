package upariscommonmarkjava.md2html.interfaces;

import java.io.Reader;
import java.util.List;
import java.util.Map;

public interface ICMFile {
    Reader getReader();
    void setMetadata(Map<String, List<String>> metadata);
}
