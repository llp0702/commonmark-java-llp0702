package upariscommonmarkjava.buildsite.directorymd;

import lombok.Getter;
import upariscommonmarkjava.buildsite.directoryhtml.DirectoryHtml;
import upariscommonmarkjava.buildsite.directoryhtml.IDirectoryHtml;
import upariscommonmarkjava.buildsite.theme.ITheme;
import upariscommonmarkjava.buildsite.theme.Theme;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class DirectoryMdWithTemplateAndTheme  extends DirectoryMdWithTemplate{
    private final Path themesBasePath;
    private final Logger logger = Logger.getLogger(getClass().getName());
    @Getter
    private final List<ITheme> themes;

    protected DirectoryMdWithTemplateAndTheme(Path toml, Path content, Path templates, Path themesBasePath,Optional<File> optHier) throws IOException {
        super(toml, content, templates, optHier);
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


    @Override
    public IDirectoryHtml generateHtml() {
        try{
            return DirectoryHtml.create(this.contentBasePath,this.tomlOptions,this.mdFilesPaths,this.staticFilesPaths,
            this.templatesPaths,  getThemeIfPresent(),getHierarchie() );  
        }catch(ClassNotFoundException | IOException e){
            return DirectoryHtml.create(this.contentBasePath,this.tomlOptions,this.mdFilesPaths,this.staticFilesPaths,
            this.templatesPaths,  getThemeIfPresent(),Optional.empty() );  
        }
    }

    private ITheme getThemeIfPresent(){
        if(tomlOptions.getData() != null){
            final String themeName = tomlOptions.getData().getString("general.theme");
            if(themeName != null && !themeName.isBlank()){
                return themes.stream().
                        filter(theme->themeName.equals(theme.getName()))
                        .findFirst().orElse(null);
            }
        }
        return null;
    }

}
