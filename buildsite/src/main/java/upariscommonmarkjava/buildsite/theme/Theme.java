package upariscommonmarkjava.buildsite.theme;

import lombok.Getter;
import lombok.NonNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class Theme implements ITheme {
    private static final Logger logger = Logger.getLogger("Theme");
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

    /** récupère les fichiers statiques et templates du thèmes si ils sont présents */
    private boolean parseTheme(){
        final Path templatesPathsBase = this.basePath.resolve("templates");
        final Path staticPathsBase = this.basePath.resolve("static");
        final boolean isTemplatesExistant = Files.isDirectory(templatesPathsBase);
        final boolean isStaticExistant = Files.isDirectory(staticPathsBase);
        if(isTemplatesExistant){
            parcoursArbo(templatesPathsBase, templatePaths);
        }
        if(isStaticExistant){
            parcoursArbo(staticPathsBase, staticPaths);
        }
        return isTemplatesExistant || isStaticExistant;
    }

    @Override
    public String getName(){
        return basePath.getFileName().toString();
    }

    /** Parcours l'arborescence des fichiers et prenant en compte les thèmes */
    private void parcoursArbo(@NonNull final Path basePath, @NonNull final List<Path> targetList){
        try(final Stream<Path> pathsStream = Files.list(basePath)){
            pathsStream.forEach(curPath->{
                if(Files.isDirectory(curPath)){
                    parcoursArbo(curPath,targetList);
                }else if(Files.isRegularFile(curPath)){
                    targetList.add(curPath);
                }
            });
        }catch (Exception e){
            logger.warning("Exception in parcoursArbo "+e.getMessage());
        }
    }
}
