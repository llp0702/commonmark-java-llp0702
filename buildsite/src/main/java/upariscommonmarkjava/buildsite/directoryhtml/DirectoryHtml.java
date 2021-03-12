package upariscommonmarkjava.buildsite.directoryhtml;

import lombok.Getter;
import upariscommonmarkjava.md2html.implementations.CMFile;
import upariscommonmarkjava.md2html.implementations.ConverterMd2Html;
import upariscommonmarkjava.md2html.interfaces.ICMFile;
import upariscommonmarkjava.md2html.interfaces.IConverterMd2Html;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
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
    private final Path inputPathBase;

    public static DirectoryHtml create(Path inputPath, ITOMLFile tomlOptions, List<Path> mdFilesPaths,
                                       List<Path> staticFiles, List<Path> templatesFiles) {
        return new DirectoryHtml(inputPath, tomlOptions, mdFilesPaths, staticFiles, templatesFiles);
    }

    protected DirectoryHtml(Path inputPathBase, ITOMLFile tomlOptions, List<Path> mdFilesPaths, List<Path> staticFiles,
                            List<Path> templatesFiles) {
        this.inputPathBase = inputPathBase;
        this.tomlOptions = tomlOptions;

        this.inputFilesMdPaths = mdFilesPaths;

        this.staticFiles = staticFiles;
        this.templatesFiles = templatesFiles;
    }



    @Override
    public void save(final Path targetBasePath) throws IOException {

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
        copyStaticFiles(targetBasePath);

        //Convert Md to Html then Copy hrefs
        convertMd2HtmlAndCopyHrefs(targetBasePath);

    }

    private void convertMd2HtmlAndCopyHrefs(Path targetBasePath) throws IOException {
        for(Path inputMdFile: inputFilesMdPaths){

            Path outputPath = callMd2Html(targetBasePath, inputMdFile);

            //Save hrefs if not present
            copyHrefsIfAbsent(targetBasePath, outputPath);
        }
    }

    private Path callMd2Html(Path targetBasePath, Path inputMdFile) throws IOException {
        Path outputPath = extension2Html(targetBasePath.resolve(inputMdFile));
        Path inputPath = inputPathBase.resolve(inputMdFile);
        Files.createDirectories(outputPath.getParent());

        ICMFile cmFile = CMFile.fromPath(inputPath);
        IConverterMd2Html converterMd2Html = new ConverterMd2Html();
        converterMd2Html.parseAndConvert2HtmlAndSave(cmFile, tomlOptions, outputPath, templatesFiles);
        return outputPath;
    }

    public static boolean isUrl(String url)
    {
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

    private void copyHrefsIfAbsent(Path targetBasePath, Path html) throws IOException {
        List<String> hrefs = getHrefs(html);
        for(String href:hrefs) {
            if (!isUrl(href) && !Paths.get(href).isAbsolute()) {
                Path hrefShouldBe = Paths.get(targetBasePath.toString(), href);
                if (!Files.exists(hrefShouldBe)) {
                    //In this case we search it in templates folder
                    Path hrefRecuperationFrom = templatesFiles.stream()
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

    private void copyStaticFiles(Path targetBasePath) throws IOException {
        for(Path staticPath : this.staticFiles) {
            Path output = targetBasePath.resolve(staticPath);

            Files.createDirectories(output.getParent());
            Files.copy(inputPathBase.resolve(staticPath), output,StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private List<String> getHrefs(Path htmlPath){
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

    private Path extension2Html(Path pathMd){
        final String name = pathMd.getFileName().toString();
        return pathMd.resolveSibling(name.substring(0, name.lastIndexOf('.')) + ".html");
    }

}
