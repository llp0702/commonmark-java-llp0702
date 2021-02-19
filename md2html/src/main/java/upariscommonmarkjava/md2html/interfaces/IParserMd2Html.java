package upariscommonmarkjava.md2html.interfaces;

import org.commonmark.node.Node;
import upariscommonmarkjava.md2html.interfaces.ICMFile;

import java.io.IOException;
import java.nio.file.Path;

public interface IParserMd2Html {
    Node parse(ICMFile cmFile) throws IOException;
    String parseAndRenderToString(ICMFile cmFile)throws IOException;
    public void parse(ICMFile cmFile, Path destination)throws IOException;
}
