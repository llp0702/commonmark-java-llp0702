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
import java.util.logging.Logger;
import java.util.stream.Stream;

public class DirectoryMdWithTemplate extends DirectoryMd {
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Getter
    protected final List<Path> templatesPaths;

    @Getter
    private final Path templateBasePath;

    protected DirectoryMdWithTemplate(@NonNull Path toml, @NonNull Path content, Path templates) throws IOException{
        super(toml,content);
        this.templatesPaths = new ArrayList<>();
        if(templates!=null) {
            this.templateBasePath = templates;
            parcoursTemplates(templates);
        }else{
            this.templateBasePath = null;
        }
    }
    protected void parcoursTemplates(Path templateBasePath){
        if(templateBasePath == null) return;
        try(final Stream<Path> paths = Files.list(templateBasePath)){
            paths.forEach(currentPath ->{
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
        return DirectoryHtml.create(this.contentBasePath,this.tomlOptions,this.mdFilesPaths,this.staticFilesPaths,
                this.templatesPaths, null);
    }

}