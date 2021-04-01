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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirectoryHtml implements IDirectoryHtml {
    public static final Logger logger = Logger.getLogger("Directory html logger");

    @Getter
    private final List<Path> inputFilesMdPaths;


    protected ITOMLFile tomlOptions;
    protected List<Path> staticFilesPaths;
    protected List<Path> templatesPaths;
    protected Optional<ITheme> theme;
    private final Path inputContentBasePath;

    public static DirectoryHtml create(@NonNull Path inputPath, ITOMLFile tomlOptions, @NonNull List<Path> mdFilesPaths,
                                       List<Path> staticFiles, List<Path> templatesFiles, ITheme theme) {
        return new DirectoryHtml(inputPath, tomlOptions, mdFilesPaths, staticFiles, templatesFiles, theme);
    }

    protected DirectoryHtml(@NonNull Path inputContentBasePath, ITOMLFile tomlOptions, @NonNull List<Path> mdFilesPaths, List<Path> staticFilesPaths,
                            List<Path> templatesPaths, ITheme theme) {
        this.inputContentBasePath = inputContentBasePath;

        this.tomlOptions = tomlOptions;

        this.inputFilesMdPaths = mdFilesPaths;

        this.staticFilesPaths = staticFilesPaths;
        if(this.staticFilesPaths ==null)this.staticFilesPaths = new ArrayList<>();

        this.templatesPaths = templatesPaths;
        if(this.templatesPaths ==null)this.templatesPaths = new ArrayList<>();

        this.theme = Optional.of(theme);
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
        copyStaticFiles(targetBasePath, this.staticFilesPaths, inputContentBasePath, true);
        //Convert Md to Html then Copy hrefs
        convertMd2HtmlAndCopyHrefs(targetBasePath);
        theme.ifPresent(t ->{
            if(t.isValid()) {
                try {
                    copyStaticFiles(targetBasePath, t.getStaticPaths() ,t.getBasePath().resolve("static"), false);
                } catch (IOException e) {
                    logger.warning("Exception when trying to copy static files");
                    e.printStackTrace();
                }
            }
        });

    }

    private void convertMd2HtmlAndCopyHrefs(@NonNull Path targetBasePath) throws IOException {
        for(Path inputMdFile: inputFilesMdPaths){

            Path outputPath = callMd2Html(targetBasePath, inputMdFile);

            //Save hrefs if not present
            copyHrefsIfAbsent(targetBasePath, outputPath);
        }
    }

    private Path callMd2Html(@NonNull Path targetBasePath, @NonNull Path inputMdFile) throws IOException {
        Path outputPath = extension2Html(targetBasePath.resolve(inputContentBasePath.relativize(inputMdFile)));
        Files.createDirectories(outputPath.getParent());

        ICMFile cmFile = CMFile.fromPath(inputMdFile);
        IConverterMd2Html converterMd2Html = new ConverterMd2Html(this.tomlOptions,templatesPaths);
        theme.ifPresent(t->{
            if(t.isValid()){
                for(Path themeTemplate: t.getTemplatePaths()){
                    if(themeTemplate==null)continue;
                    if(this.templatesPaths.stream().noneMatch(templatePath->templatePath.getFileName()
                            .equals(themeTemplate.getFileName()))){
                        templatesPaths.add(themeTemplate);
                    }
                }
            }
        });
        converterMd2Html.parseAndConvert2HtmlAndSave(cmFile, outputPath);
        return outputPath;
    }

    public static boolean isUrl(String url) {
        /* Try creating a valid URL */
        try {
            new URL(url).toURI();
            return true;
        }

        // If there was an Exception
        // while creating URL object
        catch (Exception e) {
            return false;
        }
    }

    private void copyHrefsIfAbsent(@NonNull Path targetBasePath, @NonNull Path html) throws IOException {
        List<String> hrefs = getHrefs(html);
        for(String href:hrefs) {
            if (!isUrl(href) && !Paths.get(href).isAbsolute()) {
                Path hrefShouldBe = Paths.get(targetBasePath.toString(), href);
                if (!Files.exists(hrefShouldBe)) {
                    //In this case we search it in templates folder
                    Path hrefRecuperationFrom = templatesPaths.stream()
                            .filter(x -> x.getFileName().toString().equals(hrefShouldBe.getFileName().toString()))
                            .findAny()
                            .orElse(null);
                    if (hrefRecuperationFrom != null) {
                        Files.createDirectories(hrefShouldBe.getParent());
                        Files.copy(hrefRecuperationFrom, hrefShouldBe, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }

    private void copyStaticFiles(@NonNull Path targetBasePath, @NonNull List<Path> staticFiles, @NonNull Path fromBasePath, boolean replaceIfExisting) throws IOException {
        for(Path staticPath : staticFiles) {
            Path output = targetBasePath.resolve(fromBasePath.relativize(staticPath));
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
