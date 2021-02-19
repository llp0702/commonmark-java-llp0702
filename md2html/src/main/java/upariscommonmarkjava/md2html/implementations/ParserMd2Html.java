package upariscommonmarkjava.md2html.implementations;

import lombok.Builder;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import upariscommonmarkjava.md2html.interfaces.ICMFile;
import upariscommonmarkjava.md2html.interfaces.IParserMd2Html;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Builder
public class ParserMd2Html implements IParserMd2Html {
    final private Parser parser = Parser.builder().build();

    public Node parse(ICMFile cmFile)throws IOException {
        return parser.parseReader(cmFile.getReader());
    }

    @Override
    public String parseAndRenderToString(ICMFile cmFile) throws IOException {
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String res = renderer.render(parse(cmFile));

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
