package upariscommonmarkjava.buildsite.directorymd;

import lombok.Getter;
import lombok.NonNull;
import upariscommonmarkjava.buildsite.directoryhtml.DirectoryHtml;
import upariscommonmarkjava.buildsite.directoryhtml.IDirectoryHtml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;

public class DirectoryMdWithTemplate extends DirectoryMd {
    @Getter
    protected final List<Path> templatesPaths;

    private final String basePathTemplates;

    protected DirectoryMdWithTemplate(@NonNull Path toml, @NonNull File content, Path templates) throws IOException{
        super(toml,content);
        if(templates!=null) {
            this.templatesPaths = new ArrayList<>();
            this.basePathTemplates = templates.toString();
            parcoursTemplates(templates.toFile(), "");
        }else{
            this.templatesPaths = Collections.<Path>emptyList();
            this.basePathTemplates = "";
        }
    }

    protected void parcoursTemplates(File templates, String basePath){
        if(templates == null) return;

        File[] contentFiles = templates.listFiles();
        if(contentFiles==null) return;

        for(File file : contentFiles) {
            if(file == null) continue;

            if(file.isDirectory()) {
                parcoursTemplates(file, basePath + "/" + file.getName());
            }else{
                templatesPaths.add(Paths.get(basePathTemplates, basePath , file.getName()));
            }
        }
    }

    @Override
    public IDirectoryHtml generateHtml() {
        return DirectoryHtml.create(this.basePath,this.tomlOptions,this.mdFilesPaths,this.staticFilesPaths,
                this.templatesPaths, null);
    }

}