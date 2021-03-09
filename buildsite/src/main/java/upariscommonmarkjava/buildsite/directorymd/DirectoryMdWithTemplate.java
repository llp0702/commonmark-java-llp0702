package upariscommonmarkjava.buildsite.directorymd;

import upariscommonmarkjava.buildsite.directoryhtml.DirectoryHtml;
import upariscommonmarkjava.buildsite.directoryhtml.IDirectoryHtml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DirectoryMdWithTemplate extends DirectoryMd {
    protected final ArrayList<Path> pathsTemplates;
    private final String pathsParentTemplate;

    protected DirectoryMdWithTemplate(File toml, File content, Path templates) throws IOException{
        super(toml,content);
        pathsTemplates = new ArrayList<>();
        this.pathsParentTemplate = templates.toString();
        parcoursTemplates(templates.toFile(),"");
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
                pathsTemplates.add(Paths.get(pathsParentTemplate, basePath , file.getName()));
            }
        }
    }

    @Override
    public IDirectoryHtml generateHtml()
    {
        return DirectoryHtml.create(this.basePath,this.tomlOptions,this.mdFilesPaths,this.staticFilesPaths, this.pathsTemplates);
    }

}