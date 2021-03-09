package upariscommonmarkjava.buildsite.theme;

import java.nio.file.Path;
import java.util.List;

public interface ITheme {
    List<Path> getStaticPaths();
    List<Path> getTemplatePaths();
    String getName();
    void addStaticPath(Path staticFilePath);
    void addTemplatePath(Path templateFilePath);
}
