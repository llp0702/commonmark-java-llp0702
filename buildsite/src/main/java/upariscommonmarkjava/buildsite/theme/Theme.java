package upariscommonmarkjava.buildsite.theme;

import lombok.Getter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Theme implements ITheme {
    @Getter
    private final List<Path> staticPaths;

    @Getter
    private final List<Path> templatePaths;

    @Getter
    private final String name;

    public Theme(final String name){
        this.name = name;
        staticPaths = new ArrayList<>();
        templatePaths = new ArrayList<>();
    }

    @Override
    public void addStaticPath(Path staticFilePath) {
        if(staticFilePath != null){
            staticPaths.add(staticFilePath);
        }
    }

    @Override
    public void addTemplatePath(Path templateFilePath) {
        if(templateFilePath != null){
            templatePaths.add(templateFilePath);
        }
    }
}
