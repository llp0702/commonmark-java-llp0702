package upariscommonmarkjava.md2html.interfaces;

import org.commonmark.node.Node;
import upariscommonmarkjava.md2html.interfaces.ICMFile;

import java.io.IOException;
import java.nio.file.Path;

public interface IParserMd2Html {
    Node parseAndConvert2HtmlAndSave(ICMFile cmFile) throws IOException;
    String parseAndConvert2Html(ICMFile cmFile)throws IOException;
    public void parseAndConvert2HtmlAndSave(ICMFile cmFile, Path destination)throws IOException;
}
