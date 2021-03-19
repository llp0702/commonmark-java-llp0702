package upariscommonmarkjava.md2html.implementations;

import lombok.NonNull;
import org.commonmark.Extension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.tomlj.TomlTable;
import upariscommonmarkjava.md2html.implementations.extensions.htmltemplate.AdvancedHtmlTemplate;
import upariscommonmarkjava.md2html.implementations.extensions.toml.TomlMetaParser;
import upariscommonmarkjava.md2html.implementations.extensions.toml.TomlVisitor;
import upariscommonmarkjava.md2html.interfaces.ICMFile;
import upariscommonmarkjava.md2html.interfaces.IConverterMd2Html;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class ConverterMd2Html implements IConverterMd2Html {
    public ConverterMd2Html() {
        final List<Extension> extensions = Collections.singletonList(TomlMetaParser.create());
        parser = Parser.builder().extensions(extensions).build();
        htmlRenderer = HtmlRenderer.builder().extensions(extensions).build();
    }

    private final Parser parser;
    private final HtmlRenderer htmlRenderer;

    public Node parse(@NonNull final ICMFile cmFile) throws IOException {
        return parser.parseReader(cmFile.getReader());
    }

    @Override
    public String parseAndConvert2Html(@NonNull final ICMFile cmFile, final ITOMLFile globalMetadata, final List<Path> templatesFiles) throws IOException {
        return convert2Html(cmFile, globalMetadata, templatesFiles);
    }

    @Override
    public void parseAndConvert2HtmlAndSave(@NonNull final ICMFile cmFile, final ITOMLFile globalMetadata, @NonNull final Path destination, final List<Path> templatesFiles) throws IOException {
        final String resString = parseAndConvert2Html(cmFile, globalMetadata, templatesFiles);

        if (!resString.isEmpty()) {
            Files.createDirectories(destination.getParent());
            Files.writeString(destination, resString, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE);
        }
    }

    private String wrapHtmlBody(final String body) {
        return "<!DOCTYPE HTML><html lang=\"en\"><head><title>title</title></head><body>" + body + "</body></html>";
    }

    private String convert2Html(@NonNull final ICMFile cmFile, final ITOMLFile globalMetadata, final List<Path> templateFiles) throws IOException {
        final Node resNode = parse(cmFile);
        final TomlVisitor t = new TomlVisitor();

        resNode.accept(t);
        cmFile.setTomlMetadataLocal(t.getData());

        if (cmFile.isDraft())
            return "";

        final String htmlContent = htmlRenderer.render(resNode);

        if (templateFiles == null || templateFiles.isEmpty())
            return wrapHtmlBody(htmlContent);

        return applyTemplateIfPresent(cmFile, globalMetadata, templateFiles, htmlContent);
    }

    private static Optional<Path> searchPathEqual(final List<Path> templateFiles, final String pattern) {
        return templateFiles.stream().filter(x -> pattern.equals(x.getFileName().toString())).findAny();
    }

    private static Optional<Path> searchPathEndsWith(final List<Path> templateFiles, final String pattern) {
        return templateFiles.stream().filter(x -> x.normalize().toString().endsWith(pattern)).findAny();
    }

    private String applyTemplateIfPresent(@NonNull final ICMFile cmFile, final ITOMLFile globalMetadata, final List<Path> templateFiles, final String htmlContent) throws IOException {
        final List<TomlTable> metaDataLocal = cmFile.getTomlMetadataLocal();
        Optional<Path> template = searchPathEqual(templateFiles,"default.html");

        for (TomlTable metaData : metaDataLocal) {
            if (metaData != null) {
                String curRes = metaData.getString("template");
                if (curRes != null && !curRes.isEmpty() && !curRes.isBlank()) {
                    template = searchPathEndsWith(templateFiles,curRes);

                    if (template.isEmpty())
                        break;
                }
            }
        }

        if (template.isEmpty()) {
            return wrapHtmlBody(htmlContent);
        } else {
            return new AdvancedHtmlTemplate(htmlContent,globalMetadata,metaDataLocal,templateFiles,Files.readString(template.get())).apply();
        }
    }
}
