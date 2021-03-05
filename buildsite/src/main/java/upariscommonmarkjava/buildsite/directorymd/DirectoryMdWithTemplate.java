package upariscommonmarkjava.buildsite.directorymd;

import upariscommonmarkjava.buildsite.DirectoryHtml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DirectoryMdWithTemplate extends DirectoryMd {
    protected final ArrayList<Path> pathsTemplates;
    private final String pathsParentTemplate;

    protected DirectoryMdWithTemplate(File toml, File content, File templates) throws IOException{
        super(toml,content);
        pathsTemplates = new ArrayList<>();
        this.pathsParentTemplate = templates.getAbsolutePath();
        parcoursTemplates(templates, "");
    }

    protected void parcoursTemplates(File templates, String path){
        if(templates == null) return;

        File[] contentFiles = templates.listFiles();
        if(contentFiles==null) return;

        for(File file : contentFiles) {
            if(file == null) continue;

            if(file.isDirectory()) {
                parcours(file, path + "/" + file.getName());
            }else{
                pathsTemplates.add(Paths.get(pathsParentTemplate, path , file.getName()));
            }
        }
    }

    @Override
    public DirectoryHtml generateHtml()
    {
        return DirectoryHtml.create(this.inputPath,this.tomlOptions,this.pathsMd,this.pathsOther, this.pathsTemplates);
    }

}