package upariscommonmarkjava.buildsite.directorymd;

import lombok.Getter;
import lombok.NonNull;
import upariscommonmarkjava.buildsite.directoryhtml.DirectoryHtml;
import upariscommonmarkjava.buildsite.directoryhtml.IDirectoryHtml;
import upariscommonmarkjava.buildsite.theme.ITheme;
import upariscommonmarkjava.buildsite.theme.Theme;

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
    private final List<ITheme> themes=new ArrayList<>();

    public DirectoryMdWithTemplateAndTheme(@NonNull Path toml, @NonNull Path content,@NonNull Path templates,@NonNull Path themesBasePath) throws IOException {
        super(toml, content, templates);
        this.themesBasePath = themesBasePath;
        parcoursThemes();
    }
    public DirectoryMdWithTemplateAndTheme(@NonNull Path toml, @NonNull Path content, @NonNull Path themesBasePath) throws IOException {
        super(toml, content);
        this.themesBasePath = themesBasePath;
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
        return new DirectoryHtml(this.contentBasePath,this.tomlOptions,this.mdFilesPaths,this.staticFilesPaths,
                this.templatesPaths,  getTheme());
    }

    private Optional<ITheme> getTheme(){
        if(tomlOptions.getData() != null){
            final String themeName = tomlOptions.getData().getString("general.theme");
            if(themeName != null && !themeName.isBlank()){
                return themes.stream().
                        filter(theme->themeName.equals(theme.getName()))
                        .findFirst();
            }
        }
        return Optional.empty();
    }

}