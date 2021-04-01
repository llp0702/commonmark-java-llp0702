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
    protected final List<Path> staticFilesPaths;

    protected final ITOMLFile tomlOptions;

    @Getter
    protected final Path contentBasePath;

    private final Logger logger = Logger.getLogger(getClass().getName());

    public static DirectoryMd open(final String path) throws SiteFormatException {
        File folder = new File(path);
        if(!folder.isDirectory())throw new SiteFormatException("The file is not a folder");

        File[] files = folder.listFiles();

        if(files==null)throw new SiteFormatException("No files found");

        Optional<File> optToml = Arrays.stream(files)
                .filter(x -> x.getName().equals("site.toml")).findAny();
        if(optToml.isEmpty())throw new SiteFormatException("No Site.Toml found ! ");

        Optional<File> optContent = Arrays.stream(files)
                .filter(x -> x.getName().equals("content")).findAny();
        if(optContent.isEmpty())throw new SiteFormatException("No content folder ! ");
        if(!optContent.get().isDirectory())throw new SiteFormatException("Content is not a folder ! ");
        File content = optContent.get();
        File[] contentFiles = content.listFiles();
        if(contentFiles==null)throw new SiteFormatException("Content is empty");
        Optional<File> optIndex = Arrays.stream(contentFiles)
                .filter(x -> x.getName().equals("index.md")).findAny();
        if (optIndex.isEmpty())throw new SiteFormatException("No index.md found ! ");

        Optional<File> optTemplatesDir = Arrays.stream(files)
                .filter(x->"templates".equals(x.getName())).findAny();

        Optional<File> optThemesDir = Arrays.stream(files).filter(x->"themes".equals(x.getName()))
                .findAny();

        try {
            if(optThemesDir.isPresent()){
                return new DirectoryMdWithTemplateAndTheme(optToml.get().toPath(), content.toPath(),
                        optTemplatesDir.map(File::toPath).orElse(null), optThemesDir.get().toPath());
            }else if(optTemplatesDir.isPresent()){
                return new DirectoryMdWithTemplate(optToml.get().toPath(), content.toPath(), optTemplatesDir.get().toPath());
            }else{
                return new DirectoryMd(optToml.get().toPath(), content.toPath());
            }
        }
        catch (IOException ioe){
            throw new SiteFormatException("Error IOException : " + ioe.getMessage());
        }
    }



    private ITOMLFile initOption(Path toml) throws IOException {
        return TomlFile.fromPath(toml);
    }

    protected void parcoursContent(Path contentBasePath){
        if(contentBasePath == null) return;
        try(final Stream<Path> paths = Files.list(contentBasePath)){
            paths.forEach(currentPath ->{
                if(Files.isDirectory(currentPath)){
                    parcoursContent(currentPath);
                }else if (currentPath.getFileName().toString().endsWith(".md")) {
                    mdFilesPaths.add(currentPath);
                }else{
                    staticFilesPaths.add(currentPath);
                }
            });
        }catch (IOException e){
            logger.warning("IOException during parcoursThemes");
        }

    }

    protected DirectoryMd(Path toml, Path content) throws IOException {
        this.contentBasePath = content;
        this.tomlOptions = initOption(toml);
        mdFilesPaths = new ArrayList<>();
        staticFilesPaths = new ArrayList<>();
        parcoursContent(content);
    }


    public IDirectoryHtml generateHtml() {
        return DirectoryHtml.create(this.contentBasePath,this.tomlOptions,this.mdFilesPaths,this.staticFilesPaths,
                Collections.emptyList(), null );
    }

    @Override
    public List<ITheme> getThemes() {
        return Collections.emptyList();
    }

    @Override
    public List<Path> getTemplatesPaths() {
        return Collections.emptyList();
    }

    @Override
    public Path getTemplateBasePath() {
        return null;
    }
}
