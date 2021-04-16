package upariscommonmarkjava.buildsite.directorymd;

import lombok.Getter;
import upariscommonmarkjava.buildsite.SiteFormatException;
import upariscommonmarkjava.buildsite.directoryhtml.DirectoryHtml;
import upariscommonmarkjava.buildsite.directoryhtml.IDirectoryHtml;
import upariscommonmarkjava.buildsite.theme.ITheme;
import upariscommonmarkjava.md2html.implementations.TomlFile;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class DirectoryMd implements IDirectoryMd{
    @Getter
    protected final List<Path> mdFilesPaths;

    @Getter
    protected final List<Path> asciiFilesPaths;

    @Getter
    protected final List<Path> staticFilesPaths;

    protected final ITOMLFile tomlOptions;

    @Getter
    protected final Path contentBasePath;

    private final Logger logger = Logger.getLogger(getClass().getName());

    public static Optional<File> findOptFileOrDirectory(final String name, final File[] files) {
        return Arrays.stream(files).filter(x -> x.getName().equals(name)).findAny();
    }

    public static File findFileOrDirectory(final String name, final File[] files, final String errorMessage) throws  SiteFormatException {
        return findOptFileOrDirectory(name,files).orElseThrow(() -> new SiteFormatException(errorMessage));
    }

    public static File[] findFiles(final File folder,final String errorMessage) throws  SiteFormatException {
        final File[] files = folder.listFiles();
        if(files == null)
            throw new SiteFormatException(errorMessage);
        return files;
    }

    public static void isDirectory(final File folder, final String errorMessage) throws  SiteFormatException {
        if(!folder.isDirectory())
            throw new SiteFormatException(errorMessage);
    }

    public static DirectoryMd open(final Path folderPath) throws SiteFormatException {

        isDirectory(folderPath.toFile(), "The file is not a folder");

        final File[] files = findFiles(folderPath.toFile(), "No files found");

        final File toml = findFileOrDirectory("site.toml",files,"No Site.Toml found ! ");
        final File content = findFileOrDirectory("content",files,"No content folder ! ");

        isDirectory(content, "Content is not a folder ! ");

        final File[] contentFiles = findFiles(content, "Content is empty");

        findFileOrDirectory("index.md",contentFiles,"No index.md found ! ");

        final Optional<File> optTemplatesDir = findOptFileOrDirectory("templates",files);
        final Optional<File> optThemesDir = findOptFileOrDirectory("themes",files);

        try {
            if(optThemesDir.isPresent()){

                if(optTemplatesDir.isPresent())
                    return new DirectoryMdWithTemplateAndTheme(toml.toPath(), content.toPath(),
                        optTemplatesDir.get().toPath(), optThemesDir.get().toPath());

                return new DirectoryMdWithTemplateAndTheme(toml.toPath(), content.toPath(), optThemesDir.get().toPath());
            }else if(optTemplatesDir.isPresent()){
                return new DirectoryMdWithTemplate(toml.toPath(), content.toPath(), optTemplatesDir.get().toPath());
            }else{
                return new DirectoryMd(toml.toPath(), content.toPath());
            }
        }
        catch (IOException ioe){
            throw new SiteFormatException("Error IOException : " + ioe.getMessage());
        }
    }

    private ITOMLFile initOption(final Path toml) throws IOException {
        return TomlFile.fromPath(toml);
    }

    protected void parcoursContent(final Path contentBasePath){
        if(contentBasePath == null) return;
        try(final Stream<Path> paths = Files.list(contentBasePath)){
            paths.forEach(currentPath ->{
                if(Files.isDirectory(currentPath)){
                    parcoursContent(currentPath);
                }else if (currentPath.getFileName().toString().endsWith(".md")) {
                    mdFilesPaths.add(currentPath);
                } else if (currentPath.getFileName().toString().endsWith(".adoc")) {
                    asciiFilesPaths.add(currentPath);
                } else{
                    staticFilesPaths.add(currentPath);
                }
            });
        }catch (IOException e){
            logger.warning("IOException during parcoursThemes");
        }
    }

    protected DirectoryMd(final Path toml,final Path content) throws IOException {
        this.contentBasePath = content;
        this.tomlOptions = initOption(toml);
        mdFilesPaths = new ArrayList<>();
        staticFilesPaths = new ArrayList<>();
        asciiFilesPaths = new ArrayList<>();
        parcoursContent(content);
    }


    protected IDirectoryHtml generateHtml(BuilderDirectoryHtml builder) {
        return builder.apply(this.contentBasePath,this.tomlOptions,this.mdFilesPaths,this.staticFilesPaths,
                this.asciiFilesPaths,  Collections.emptyList(), Optional.empty());
    }

    public IDirectoryHtml generateHtml() {
        return generateHtml(DirectoryHtml::new);
    }

    @Override
    public List<ITheme> getThemes() {
        return new ArrayList<>();
    }

    @Override
    public List<Path> getTemplatesPaths() {
        return new ArrayList<>();
    }

    @Override
    public Optional<Path> getTemplateBasePath() {
        return Optional.empty();
    }
}
