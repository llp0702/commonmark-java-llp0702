package upariscommonmarkjava.md2html.implementations;

import org.commonmark.Extension;
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import upariscommonmarkjava.md2html.implementations.extensions.YamlMeta;
import upariscommonmarkjava.md2html.interfaces.ICMFile;
import upariscommonmarkjava.md2html.interfaces.IParserMd2Html;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;


public class ConverterMd2Html implements IParserMd2Html {
    public ConverterMd2Html(){
        final List<Extension> extensions = Collections.singletonList(YamlMeta.create());
        parser = Parser.builder().extensions(extensions).build();
        htmlRenderer = HtmlRenderer.builder().extensions(extensions).build();
    }

    final private Parser parser;
    final private HtmlRenderer htmlRenderer;
    public Node parse(ICMFile cmFile)throws IOException {
        return parser.parseReader(cmFile.getReader());
    }

    @Override
    public String parseAndRenderToString(ICMFile cmFile) throws IOException {
        Node document = parse(cmFile);
        String res = htmlRenderer.render(document);
        YamlFrontMatterVisitor t = new YamlFrontMatterVisitor();
        document.accept(t);
        cmFile.setMetadata(t.getData());
        return "<!DOCTYPE HTML><html lang=\"en\"><head><title>title</title></head><body>"+res+"</body></html>";
    }

    @Override
    public void parse(ICMFile cmFile, Path destination) throws IOException {
        Node res = parse(cmFile);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String resString = renderer.render(res);
        Files.deleteIfExists(destination);Files.createFile(destination);
        Files.writeString(destination, resString, StandardOpenOption.APPEND);

    }

}