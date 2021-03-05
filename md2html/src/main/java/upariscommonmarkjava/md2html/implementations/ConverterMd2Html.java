package upariscommonmarkjava.md2html.implementations;

import org.commonmark.Extension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.tomlj.TomlParseResult;
import upariscommonmarkjava.md2html.implementations.extensions.htmltemplate.HtmlTemplate;
import upariscommonmarkjava.md2html.implementations.extensions.toml.TomlMetaParser;
import upariscommonmarkjava.md2html.implementations.extensions.toml.TomlVisitor;
import upariscommonmarkjava.md2html.interfaces.ICMFile;
import upariscommonmarkjava.md2html.interfaces.IConverterMd2Html;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;
import upariscommonmarkjava.md2html.interfaces.extensions.htmltemplate.IHtmlTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;


public class ConverterMd2Html implements IConverterMd2Html {
    public ConverterMd2Html(){
        final List<Extension> extensions = Collections.singletonList(TomlMetaParser.create());
        parser = Parser.builder().extensions(extensions).build();
        htmlRenderer = HtmlRenderer.builder().extensions(extensions).build();
    }

    private final Parser parser;
    private final HtmlRenderer htmlRenderer;

    public Node parse(ICMFile cmFile)throws IOException {
        return parser.parseReader(cmFile.getReader());
    }

    @Override
    public String parseAndConvert2Html(ICMFile cmFile, ITOMLFile globalMetadata,
                                       List<Path> templatesFiles) throws IOException {
        return convert2Html(cmFile, globalMetadata, templatesFiles);

    }

    @Override
        public void parseAndConvert2HtmlAndSave(ICMFile cmFile, ITOMLFile globalMetadata, Path destination,
                                            List<Path> templatesFiles) throws IOException {
        String resString = parseAndConvert2Html(cmFile, globalMetadata, templatesFiles);
        if(!resString.isEmpty()){
            Files.createDirectories(destination.getParent());

            Files.writeString(destination, resString, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE);
        }
    }

    private String wrapHtmlBody(String body){
        return "<!DOCTYPE HTML><html lang=\"en\"><head><title>title</title></head><body>"+body+"</body></html>";
    }
    private String convert2Html(ICMFile cmFile, ITOMLFile globalMetadata, List<Path> templateFiles) throws IOException {
        Node resNode = parse(cmFile);
        TomlVisitor t = new TomlVisitor();
        resNode.accept(t);
        cmFile.setTomlMetadataLocal(t.getData());
        if(cmFile.isDraft())return "";
        String htmlContent = htmlRenderer.render(resNode);
        if(templateFiles==null || templateFiles.isEmpty()){
            return wrapHtmlBody(htmlContent);
        }else{
            return applyTemplateIfPresent(cmFile, globalMetadata, templateFiles, htmlContent);
        }
    }

    private String applyTemplateIfPresent(ICMFile cmFile, ITOMLFile globalMetadata, List<Path> templateFiles, String htmlContent) throws IOException {
        List<TomlParseResult> metaDataLocal = cmFile.getTomlMetadataLocal();
        Path template = templateFiles.stream()
                .filter(x->"default.html".equals(x.getFileName().toString()))
                .findAny().orElse(null);
        for(TomlParseResult metaData:metaDataLocal){
            if(metaData!=null){
                String curRes = metaData.getString("template");
                if(curRes != null && !curRes.isEmpty() && !curRes.isBlank() ){
                    template = templateFiles.stream().filter(x->x.normalize().toString().endsWith(curRes))
                            .findAny().orElse(null);
                    if(template!=null)break;
                }
            }
        }
        if(template==null){
            htmlContent =wrapHtmlBody(htmlContent);
            return htmlContent;
        }else{
            IHtmlTemplate htmlTemplate = HtmlTemplate.builder().md2HtmlContent(htmlContent)
                    .metadataGlobal(globalMetadata).tomlMetadata(metaDataLocal)
                    .templateContent(Files.readString(template))
                    .templates(templateFiles).build();
            return htmlTemplate.apply();
        }
    }
}
