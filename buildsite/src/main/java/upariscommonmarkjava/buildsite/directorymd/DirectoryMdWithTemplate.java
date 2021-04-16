package upariscommonmarkjava.buildsite.directorymd;

import lombok.Getter;
import lombok.NonNull;
import upariscommonmarkjava.buildsite.directoryhtml.DirectoryHtml;
import upariscommonmarkjava.buildsite.directoryhtml.IDirectoryHtml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class DirectoryMdWithTemplate extends DirectoryMd {
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Getter
    protected final List<Path> templatesPaths;

    @Getter
    private final Optional<Path> templateBasePath;

    private DirectoryMdWithTemplate(@NonNull final Path toml, @NonNull final Path content, @NonNull final Optional<Path> templates) throws IOException
    {
        super(toml,content);
        this.templatesPaths = new ArrayList<>();
        this.templateBasePath = templates;
        parcoursTemplates();
    }

    protected DirectoryMdWithTemplate(@NonNull final Path toml, @NonNull final Path content, @NonNull final Path templates) throws IOException{
        this(toml,content,Optional.of(templates));
    }

    protected DirectoryMdWithTemplate(@NonNull final Path toml, @NonNull final Path content) throws IOException{
        this(toml,content,Optional.empty());
    }


    protected void parcoursTemplates() {
        this.templateBasePath.ifPresent(this::parcoursTemplates);
    }

    private void parcoursTemplates(@NonNull final Path templateBasePath){
        try(final Stream<Path> paths = Files.list(templateBasePath)){
            paths.forEach(currentPath -> {
                if(Files.isDirectory(currentPath)){
                    parcoursTemplates(currentPath);
                }else{
                    this.templatesPaths.add(currentPath);
                }
            });
        }catch (IOException e){
            logger.warning("IOException during parcoursThemes");
        }
    }

    @Override
    public IDirectoryHtml generateHtml() {
        return new DirectoryHtml(this.contentBasePath,this.tomlOptions,this.mdFilesPaths,this.staticFilesPaths,
                this.asciiFilesPaths, this.templatesPaths, Optional.empty());
    }

}