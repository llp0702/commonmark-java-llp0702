package upariscommonmarkjava.md2html.interfaces;

import lombok.NonNull;
import org.commonmark.node.Node;

import java.io.IOException;
import java.nio.file.Path;

public interface IConverterMd2Html {
    Node parse(@NonNull ICMFile cmFile) throws IOException;
    String parseAndConvert2Html(@NonNull ICMFile cmFile) throws IOException;
    void parseAndConvert2HtmlAndSave(@NonNull ICMFile cmFile,@NonNull Path destination)throws IOException;
}
