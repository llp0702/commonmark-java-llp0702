package upariscommonmarkjava.md2html.interfaces;

import lombok.NonNull;
import org.commonmark.node.Node;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface IConverterMd2Html {
    Node parse(@NonNull ICMFile cmFile) throws IOException;
    String parseAndConvert2Html(@NonNull ICMFile cmFile, ITOMLFile globalMetadata, List<Path> templatesFiles)throws IOException;
    void parseAndConvert2HtmlAndSave(@NonNull ICMFile cmFile, ITOMLFile globalMetadata, @NonNull Path destination,
                                     List<Path> templatesFiles)throws IOException;
}
