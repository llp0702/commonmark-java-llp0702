package upariscommonmarkjava.buildsite.directoryhtml;

import lombok.Getter;
import lombok.NonNull;
import upariscommonmarkjava.buildsite.theme.ITheme;
import upariscommonmarkjava.md2html.implementations.CMFile;
import upariscommonmarkjava.md2html.implementations.ConverterMd2Html;
import upariscommonmarkjava.md2html.interfaces.ICMFile;
import upariscommonmarkjava.md2html.interfaces.IConverterMd2Html;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirectoryHtml implements IDirectoryHtml {
    public static final Logger logger = Logger.getLogger("Directory html logger");

    @Getter
    private final List<Path> inputFilesMdPaths;


    protected ITOMLFile tomlOptions;
    protected List<Path> staticFiles;
    protected List<Path> templatesFiles;
    protected ITheme theme;
    private final Path inputPathBase;

    public static DirectoryHtml create(@NonNull Path inputPath, ITOMLFile tomlOptions, @NonNull List<Path> mdFilesPaths,
                                       List<Path> staticFiles, List<Path> templatesFiles, ITheme theme) {
        return new DirectoryHtml(inputPath, tomlOptions, mdFilesPaths, staticFiles, templatesFiles, theme);
    }

    protected DirectoryHtml(@NonNull Path inputPathBase, ITOMLFile tomlOptions, @NonNull List<Path> mdFilesPaths, List<Path> staticFiles,
                            List<Path> templatesFiles, ITheme theme) {
        this.inputPathBase = inputPathBase;

        this.tomlOptions = tomlOptions;

        this.inputFilesMdPaths = mdFilesPaths;

        this.staticFiles = staticFiles;
        if(this.staticFiles==null)this.staticFiles = new ArrayList<>();

        this.templatesFiles = templatesFiles;
        if(this.templatesFiles==null)this.templatesFiles = new ArrayList<>();

        this.theme = theme;
    }



    @Override
    public void save(@NonNull final Path targetBasePath) throws IOException {

        File tmp = new File(targetBasePath.toString());
        if(!tmp.mkdirs()){
            logger.log(Level.INFO,"No dir was created");
        }

        final File[] tmpFiles = tmp.listFiles();
        if(tmp.exists() && tmpFiles != null && tmpFiles.length > 0){
            logger.warning(tmp.getName() + " is already existing, please choose another output");
            return;
        }

        //Copy static files
        copyStaticFiles(targetBasePath, this.staticFiles ,inputPathBase, true);
        //Convert Md to Html then Copy hrefs
        convertMd2HtmlAndCopyHrefs(targetBasePath);

        if(theme != null && theme.isValid()){
            //If there is template, we copy its static files
            copyStaticFiles(targetBasePath, theme.getStaticPaths() ,theme.getBasePath().resolve("static"), false);

        }

    }

    private void convertMd2HtmlAndCopyHrefs(@NonNull Path targetBasePath) throws IOException {
        for(Path inputMdFile: inputFilesMdPaths){

            Path outputPath = callMd2Html(targetBasePath, inputMdFile);

            //Save hrefs if not present
            copyHrefsIfAbsent(targetBasePath, outputPath);
        }
    }

    private Path callMd2Html(@NonNull Path targetBasePath, @NonNull Path inputMdFile) throws IOException {
        Path outputPath = extension2Html(targetBasePath.resolve(inputMdFile));
        Path inputPath = inputPathBase.resolve(inputMdFile);
        Files.createDirectories(outputPath.getParent());

        ICMFile cmFile = CMFile.fromPath(inputPath);
        IConverterMd2Html converterMd2Html = new ConverterMd2Html();
        converterMd2Html.parseAndConvert2HtmlAndSave(cmFile, tomlOptions, outputPath, templatesFiles);
        return outputPath;
    }

    private void copyHrefsIfAbsent(@NonNull Path targetBasePath, @NonNull Path html) throws IOException {
        List<String> hrefs = getHrefs(html);
        for(String href:hrefs){
            Path hrefShouldBe = Paths.get(targetBasePath.toString(), href);
            if(!Files.exists(hrefShouldBe) ){
                //In this case we search it in templates folder
                Path hrefRecuperationFrom = templatesFiles.stream()
                        .filter(x->x.getFileName().toString().equals(hrefShouldBe.getFileName().toString()))
                        .findAny()
                        .orElse(null);
                if(hrefRecuperationFrom!=null){
                    Files.createDirectories(hrefShouldBe);
                    Files.copy(hrefRecuperationFrom,hrefShouldBe,StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private void copyStaticFiles(@NonNull Path targetBasePath, @NonNull List<Path> staticFiles, @NonNull Path fromBasePath, boolean replaceIfExisting) throws IOException {
        for(Path staticPath : staticFiles) {
            Path staticPathRelative = fromBasePath.relativize(staticPath);
            Path output = targetBasePath.resolve(staticPathRelative);
            Files.createDirectories(output.getParent());
            if(replaceIfExisting){
                Files.copy(staticPath, output,StandardCopyOption.REPLACE_EXISTING);
            }else if(!Files.exists(output)){
                Files.copy(staticPath, output);
            }
        }
    }

    private List<String> getHrefs(@NonNull Path htmlPath){
        if(!Files.exists(htmlPath) || !Files.isRegularFile(htmlPath))return Collections.emptyList();
        String htmlContent="";
        try {
            htmlContent = Files.readString(htmlPath);
        } catch (IOException e) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        Matcher matcher  = Pattern.compile("href[ ]*=[ ]*['\"](.*?)['\"]").matcher(htmlContent);
        while (matcher.find()){
            result.add(matcher.group(1));
        }
        return result;
    }

    private Path extension2Html(@NonNull Path pathMd){
        final String name = pathMd.getFileName().toString();
        return pathMd.resolveSibling(name.substring(0, name.lastIndexOf('.')) + ".html");
    }

}
