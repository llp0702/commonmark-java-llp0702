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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;


public class ConverterMd2Html implements IConverterMd2Html {
    public static final Logger logger = Logger.getLogger("ConvertMd2Html logger");
    private static final List<Extension> extensions = Collections.singletonList(TomlMetaParser.create());
    private static final Parser parser = Parser.builder().extensions(extensions).build();
    private static final HtmlRenderer htmlRenderer = HtmlRenderer.builder().extensions(extensions).build();

    private final Optional<ITOMLFile> globalMetadata;
    private final List<Path> templateFiles;

    public ConverterMd2Html(final ITOMLFile globalMetadata, final List<Path> templateFiles){
        this.globalMetadata = Optional.of(globalMetadata);
        this.templateFiles = templateFiles;
    }

    public ConverterMd2Html(final ITOMLFile globalMetadata){
        this(globalMetadata,new ArrayList<>());
    }

    public ConverterMd2Html() {
        this.globalMetadata = Optional.empty();
        this.templateFiles = new ArrayList<>();
    }

    @Override
    public Node parse(@NonNull final ICMFile cmFile) throws IOException{
        return parser.parseReader(cmFile.getReader());
    }

    @Override
    public String parseAndConvert2Html(@NonNull final ICMFile cmFile) throws IOException {
        final Node resNode = parse(cmFile);
        final TomlVisitor tomlVisitor = new TomlVisitor();

        resNode.accept(tomlVisitor);
        cmFile.setTomlMetadataLocal(tomlVisitor.getData());

        if (cmFile.isDraft())
            return "";

        final String htmlContent = htmlRenderer.render(resNode);

        if (templateFiles.isEmpty())
            return wrapHtmlBody(htmlContent);

        return applyTemplateIfPresent(cmFile, htmlContent);
    }

    @Override
    public void parseAndConvert2HtmlAndSave(@NonNull final ICMFile cmFile, @NonNull final Path destination) throws IOException {
        final String resString = parseAndConvert2Html(cmFile);

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

    private String applyTemplateIfPresent(@NonNull final ICMFile cmFile, final String htmlContent){
        final List<TomlTable> metaDataLocal = cmFile.getTomlMetadataLocal();
        Optional<Path> template = searchPathEqual(templateFiles,"default.html");

        for (TomlTable metaData : metaDataLocal) {
            if (metaData != null) {
                String curRes = metaData.getString("template");
                if (curRes != null) {
                    template = searchPathEndsWith(templateFiles,curRes);

                    if (template.isEmpty())
                        break;
                }
            }
        }

        if (template.isEmpty()) {
            return wrapHtmlBody(htmlContent);
        }


        String file = "";
        try{
            file = Files.readString(template.get());
        }
        catch(IOException ioe){
            logger.warning(ioe.getMessage());
        }

        return new AdvancedHtmlTemplate(htmlContent,globalMetadata.get(),metaDataLocal,templateFiles,file).apply();
    }

    private static Optional<Path> searchPathEqual(final List<Path> templateFiles, final String pattern) {
        return templateFiles.stream().filter(x -> pattern.equals(x.getFileName().toString())).findAny();
    }

    private static Optional<Path> searchPathEndsWith(final List<Path> templateFiles, final String pattern) {
        return templateFiles.stream().filter(x -> x.normalize().toString().endsWith(pattern)).findAny();
    }

}
