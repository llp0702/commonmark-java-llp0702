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
    private final Path basePath;

    @Getter
    private final boolean valid;

    public Theme(final Path basePath){
        this.basePath = basePath;
        staticPaths = new ArrayList<>();
        templatePaths = new ArrayList<>();
        valid = parseTheme();
    }

    private boolean parseTheme(){
        //TODO
        return true;
    }

    @Override
    public String getName(){
        return basePath.getFileName().toString();
    }

}
