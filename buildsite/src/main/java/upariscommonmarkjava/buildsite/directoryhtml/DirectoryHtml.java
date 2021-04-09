package upariscommonmarkjava.buildsite.directoryhtml;

import lombok.Getter;
import lombok.NonNull;
import upariscommonmarkjava.buildsite.theme.ITheme;
import upariscommonmarkjava.buildsite.theme.Theme;
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
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirectoryHtml implements IDirectoryHtml {
    public static final Logger logger = Logger.getLogger("Directory html logger");

    @Getter
    private final List<Path> inputFilesMdPaths;

    protected final ITOMLFile tomlOptions;
    protected final List<Path> staticFilesPaths;
    protected final List<Path> templatesPaths;
    protected final Optional<ITheme> optTheme;
    private final Path inputContentBasePath;

    public DirectoryHtml(@NonNull final Path inputContentBasePath, @NonNull final ITOMLFile tomlOptions,
                            @NonNull final List<Path> mdFilesPaths, @NonNull final List<Path> staticFilesPaths,
                            @NonNull final List<Path> templatesPaths, @NonNull final Optional<ITheme> theme) {

        this.inputContentBasePath = inputContentBasePath;
        this.tomlOptions = tomlOptions;
        this.inputFilesMdPaths = mdFilesPaths;
        this.staticFilesPaths = staticFilesPaths;
        this.templatesPaths = templatesPaths;
        this.optTheme = theme;
    }

    @Override
    public void save(@NonNull final Path targetBasePath) throws IOException {

        final File tmp = new File(targetBasePath.toString());
        if(!tmp.mkdirs()){
            logger.log(Level.INFO,"No dir was created");
        }

        //Copy static files
        copyStaticFiles(targetBasePath, this.staticFilesPaths, inputContentBasePath, true);
        //Convert Md to Html then Copy hrefs
        convertMd2HtmlAndCopyHrefs(targetBasePath);
        applyToValid(optTheme, theme -> {
            try {
                copyStaticFiles(targetBasePath, theme.getStaticPaths() ,theme.getBasePath().resolve("static"), false);
            } catch (IOException e) {
                logger.warning("Exception when trying to copy static files");
                e.printStackTrace();
            }
        });
    }

    private void applyToValid(final Optional<ITheme> optTheme, final Consumer<ITheme> fun) {
        optTheme.ifPresent(theme -> {
            if (theme.isValid())
                fun.accept(theme);
        });
    }

    private void convertMd2HtmlAndCopyHrefs(@NonNull final Path targetBasePath) throws IOException {
        for(Path inputMdFile: inputFilesMdPaths){
            copyHrefsIfAbsent(targetBasePath, callMd2Html(targetBasePath, inputMdFile));
        }
    }

    private Path callMd2Html(@NonNull final Path targetBasePath, @NonNull final Path inputMdFile) throws IOException {
        final Path outputPath = extension2Html(targetBasePath.resolve(inputContentBasePath.relativize(inputMdFile)));
        Files.createDirectories(outputPath.getParent());

        final ICMFile cmFile = CMFile.fromPath(inputMdFile);
        final IConverterMd2Html converterMd2Html = new ConverterMd2Html(this.tomlOptions,templatesPaths);

        applyToValid(optTheme, theme -> {
            for(Path themeTemplate : theme.getTemplatePaths())
            {
                if(this.templatesPaths.stream().noneMatch(templatePath->templatePath.getFileName()
                        .equals(themeTemplate.getFileName()))){
                    templatesPaths.add(themeTemplate);
                }
            }
        });

        converterMd2Html.parseAndConvert2HtmlAndSave(cmFile, outputPath);
        return outputPath;
    }

    public static boolean isUrl(final String url) {
        try {
            new URL(url).toURI();
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    private void copyHrefsIfAbsent(@NonNull final Path targetBasePath, @NonNull final Path html) throws IOException {
        final List<String> hrefs = getHrefs(html);
        for(String href : hrefs) {
            if (!isUrl(href) && !Paths.get(href).isAbsolute()) {
                final Path hrefShouldBe = Paths.get(targetBasePath.toString(), href);
                if (!Files.exists(hrefShouldBe)) {
                    //In this case we search it in templates folder
                    final Optional<Path> hrefRecuperationFrom = templatesPaths.stream()
                            .filter(x -> x.getFileName().toString().equals(hrefShouldBe.getFileName().toString()))
                            .findAny();

                    if (hrefRecuperationFrom.isPresent()) {
                        Files.createDirectories(hrefShouldBe.getParent());
                        Files.copy(hrefRecuperationFrom.get(), hrefShouldBe, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }

    private void copyStaticFiles(@NonNull final Path targetBasePath, @NonNull final List<Path> staticFiles, @NonNull final Path fromBasePath, final boolean replaceIfExisting) throws IOException {
        for(final Path staticPath : staticFiles) {
            final Path output = targetBasePath.resolve(fromBasePath.relativize(staticPath));
            Files.createDirectories(output.getParent());
            if(replaceIfExisting){
                Files.copy(staticPath, output,StandardCopyOption.REPLACE_EXISTING);
            }else if(!Files.exists(output)){
                Files.copy(staticPath, output);
            }
        }
    }

    private List<String> getHrefs(@NonNull final Path htmlPath){
        if(!Files.exists(htmlPath) || !Files.isRegularFile(htmlPath))
            return Collections.emptyList();

        try {
            final String htmlContent = Files.readString(htmlPath);
            final List<String> result = new ArrayList<>();
            final Matcher matcher = Pattern.compile("href[ ]*=[ ]*['\"](.*?)['\"]").matcher(htmlContent);
            while (matcher.find()){
                result.add(matcher.group(1));
            }
            return result;

        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private Path extension2Html(@NonNull final Path pathMd){
        final String name = pathMd.getFileName().toString();
        return pathMd.resolveSibling(name.substring(0, name.lastIndexOf('.')) + ".html");
    }

}
