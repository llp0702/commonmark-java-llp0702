package upariscommonmarkjava.md2html.implementations;

import lombok.NonNull;
import org.commonmark.Extension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.eclipse.jetty.util.IO;
import org.tomlj.TomlTable;
import upariscommonmarkjava.md2html.implementations.extensions.htmltemplate.AdvancedHtmlTemplate;
import upariscommonmarkjava.md2html.implementations.extensions.htmltemplate.HtmlTemplate;
import upariscommonmarkjava.md2html.implementations.extensions.htmltemplate.MysteryTemplate;
import upariscommonmarkjava.md2html.implementations.extensions.toml.TomlMetaParser;
import upariscommonmarkjava.md2html.implementations.extensions.toml.TomlVisitor;
import upariscommonmarkjava.md2html.implementations.incremental.Hierarchie;
import upariscommonmarkjava.md2html.interfaces.ICMFile;
import upariscommonmarkjava.md2html.interfaces.IConverterMd2Html;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Logger;


public class ConverterMd2Html implements IConverterMd2Html {
    public static final Logger logger = Logger.getLogger("ConvertMd2Html logger");
    private static final List<Extension> extensions = Collections.singletonList(TomlMetaParser.create());
    private static final Parser parser = Parser.builder().extensions(extensions).build();
    private static final HtmlRenderer htmlRenderer = HtmlRenderer.builder().extensions(extensions).build();

    private final Optional<ITOMLFile> globalMetadata;
    private final List<Path> templateFiles;

    private Optional<Hierarchie> actualHierarchie;

    public ConverterMd2Html(final ITOMLFile globalMetadata, final List<Path> templateFiles, final Hierarchie hier){
        this.globalMetadata = Optional.of(globalMetadata);
        this.templateFiles = templateFiles;
        this.actualHierarchie = Optional.of(hier);
    }

    public ConverterMd2Html(final ITOMLFile globalMetadata, final List<Path> templateFiles){
        this.globalMetadata = Optional.of(globalMetadata);
        this.templateFiles = templateFiles;
        this.actualHierarchie = Optional.empty();
    }

    public ConverterMd2Html(final ITOMLFile globalMetadata){
        this(globalMetadata,new ArrayList<>());
    }

    public ConverterMd2Html() {
        this.globalMetadata = Optional.empty();
        this.templateFiles = new ArrayList<>();
        this.actualHierarchie = Optional.empty();
    }

    @Override
    /** récupère l'ensemble du fichier common mark structuré par la librairie */
    public Node parse(@NonNull final ICMFile cmFile) throws IOException{
        return parser.parseReader(cmFile.getReader());
    }

    /** traduit les métadata du fichier toml */
    private Node parseTomlMetadata(@NonNull final ICMFile cmFile) throws IOException {
        final Node resNode = parse(cmFile);
        final TomlVisitor tomlVisitor = new TomlVisitor();

        resNode.accept(tomlVisitor);
        cmFile.setTomlMetadataLocal(tomlVisitor.getData());
        return resNode;
    }

    @Override
    /** traduit le fichier common mark */
    public String parseAndConvert2Html(@NonNull final ICMFile cmFile) throws IOException {
        final Node resNode = parseTomlMetadata(cmFile);

        if (cmFile.isDraft())
            return "";

        final String htmlContent = htmlRenderer.render(resNode);

        if (templateFiles.isEmpty())
            return wrapHtmlBody(htmlContent);

        return applyTemplateIfPresent(cmFile, htmlContent);
    }

    @Override
    /** traduit le fichier common mark et le sauvegarde */
    public void parseAndConvert2HtmlAndSave(@NonNull final ICMFile cmFile, @NonNull final Path destination) throws IOException {
        final String resString = parseAndConvert2Html(cmFile);

        if (!resString.isEmpty()) {
            Files.createDirectories(destination.getParent());
            Files.writeString(destination, resString, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE);
        }
    }

    /** renvoie la page avec les balises nécessaires à une page html */
    private String wrapHtmlBody(final String body) {
        return "<!DOCTYPE HTML><html lang=\"en\"><head><title>title</title></head><body>" + body + "</body></html>";
    }

    /** Récupère la hierarchie actuel si elle existe */
    public Optional<Hierarchie> getActualHierarchie(@NonNull final ICMFile cmFile) throws IOException{
        parseTomlMetadata(cmFile);
        final List<TomlTable> metaDataLocal = cmFile.getTomlMetadataLocal();
        final Optional<Path> template = searchTemplate(metaDataLocal);
        refreshHierarchie(cmFile,template);
        return this.actualHierarchie;
    }

    /** Mets à jour la hierarchie */
    private void refreshHierarchie(@NonNull final ICMFile cmFile, final Optional<Path> template){
        if(template.isEmpty())
            return;

        try {
            if (actualHierarchie.isPresent()) {
                //retire de toute la liste des templates le cmFile afin de mettre le potentiel nouveau
                actualHierarchie.get().supprInstanceOfCourant(cmFile.getStringPath(), templateFiles);
                actualHierarchie.get().addDep(template.get().toString(), cmFile.getStringPath());
            }
        } catch (IOException ignored) {}
    }

    /** Récupère le template spécifié par les métadatas  */
    private Optional<Path> searchTemplate(final List<TomlTable> metaDataLocal){
        Optional<Path> template = searchPathEqual(templateFiles,"default.html");
        for (TomlTable metaData : metaDataLocal) {
            if (metaData != null) {
                String curRes = metaData.getString("template");
                if (curRes != null) {
                    template = searchPathEndsWith(templateFiles, curRes);
                    if (template.isEmpty())
                        break;
                }
            }
        }
        return template;
    }

    /** Applique un template si il est trouvé */
    private String applyTemplateIfPresent(@NonNull final ICMFile cmFile, final String htmlContent){
        final List<TomlTable> metaDataLocal = cmFile.getTomlMetadataLocal();
        final Optional<Path> template = searchTemplate(metaDataLocal);

        if (template.isEmpty()) {
            return wrapHtmlBody(htmlContent);
        }

        refreshHierarchie(cmFile,template);

        String file = "";
        try{
            file = Files.readString(template.get());
        }
        catch(IOException ioe){
            logger.warning(ioe.getMessage());
        }

        return new MysteryTemplate(htmlContent,globalMetadata.get(), HtmlTemplate.buildMetaDataLocal(metaDataLocal),templateFiles,file).apply();
    }

    private static Optional<Path> searchPathEqual(final List<Path> templateFiles, final String pattern) {
        return templateFiles.stream().filter(x -> pattern.equals(x.getFileName().toString())).findAny();
    }

    private static Optional<Path> searchPathEndsWith(final List<Path> templateFiles, final String pattern) {
        return templateFiles.stream().filter(x -> x.normalize().toString().endsWith(pattern)).findAny();
    }
}
