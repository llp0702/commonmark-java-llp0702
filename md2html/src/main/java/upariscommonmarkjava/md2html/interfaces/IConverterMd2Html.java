package upariscommonmarkjava.md2html.interfaces;

import org.commonmark.node.Node;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface IConverterMd2Html {
    Node parse(ICMFile cmFile) throws IOException;
    String parseAndConvert2Html(ICMFile cmFile, ITOMLFile globalMetadata, List<Path> templatesFiles)throws IOException;
    void parseAndConvert2HtmlAndSave(ICMFile cmFile, ITOMLFile globalMetadata, Path destination,
                                     List<Path> templatesFiles)throws IOException;
}
