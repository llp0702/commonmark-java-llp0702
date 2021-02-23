package upariscommonmarkjava.md2html.implementations;

import org.commonmark.Extension;
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import upariscommonmarkjava.md2html.implementations.extensions.yaml.YamlMeta;
import upariscommonmarkjava.md2html.interfaces.ICMFile;
import upariscommonmarkjava.md2html.interfaces.IConverterMd2Html;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;


public class ConverterMd2Html implements IConverterMd2Html {
    public ConverterMd2Html(){
        final List<Extension> extensions = Collections.singletonList(YamlMeta.create());
        parser = Parser.builder().extensions(extensions).build();
        htmlRenderer = HtmlRenderer.builder().extensions(extensions).build();
    }

    final private Parser parser;
    final private HtmlRenderer htmlRenderer;
    public Node parseAndConvert2HtmlAndSave(ICMFile cmFile)throws IOException {
        return parser.parseReader(cmFile.getReader());
    }

    @Override
    public String parseAndConvert2Html(ICMFile cmFile) throws IOException {
        Node document = parseAndConvert2HtmlAndSave(cmFile);
        String res = htmlRenderer.render(document);
        YamlFrontMatterVisitor t = new YamlFrontMatterVisitor();
        document.accept(t);
        cmFile.setMetadata(t.getData());
        return wrapHtmlBody(res);
    }

    @Override
    public void parseAndConvert2HtmlAndSave(ICMFile cmFile, Path destination) throws IOException {
        Node res = parseAndConvert2HtmlAndSave(cmFile);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String resString = wrapHtmlBody(renderer.render(res));
        Files.createDirectories(destination.getParent());

        Files.writeString(destination, resString, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }

    private String wrapHtmlBody(String body){
        return "<!DOCTYPE HTML><html lang=\"en\"><head><title>title</title></head><body>"+body+"</body></html>";
    }
}
