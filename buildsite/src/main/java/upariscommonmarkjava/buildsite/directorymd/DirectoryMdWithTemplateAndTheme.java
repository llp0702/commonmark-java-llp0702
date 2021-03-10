package upariscommonmarkjava.buildsite.directorymd;

import lombok.Getter;
import upariscommonmarkjava.buildsite.theme.ITheme;
import upariscommonmarkjava.buildsite.theme.Theme;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class DirectoryMdWithTemplateAndTheme  extends DirectoryMdWithTemplate{
    private final Path themesBasePath;
    private final Logger logger = Logger.getLogger(getClass().getName());
    @Getter
    private final List<ITheme> themes;

    protected DirectoryMdWithTemplateAndTheme(Path toml, File content, Path templates, Path themesBasePath) throws IOException {
        super(toml, content, templates);
        this.themesBasePath = themesBasePath;
        this.themes = new ArrayList<>();
        parcoursThemes();
    }

    private void parcoursThemes() {
        try(final Stream<Path> themesPaths = Files.list(themesBasePath)){
            themesPaths.forEach(themePath ->{
                if(Files.isDirectory(themePath)){
                    ITheme currentTheme = new Theme(themePath);
                    if(currentTheme.isValid()){
                        this.themes.add(currentTheme);
                    }
                }
            });
        }catch (IOException e){
            logger.warning("IOException during parcoursThemes");
        }
    }




}
