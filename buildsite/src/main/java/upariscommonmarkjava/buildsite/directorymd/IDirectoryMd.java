package upariscommonmarkjava.buildsite.directorymd;

import upariscommonmarkjava.buildsite.directoryhtml.IDirectoryHtml;
import upariscommonmarkjava.buildsite.theme.ITheme;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface IDirectoryMd  {
    Path getContentBasePath();
    List<Path> getMdFilesPaths();
    List<Path> getStaticFilesPaths();
    List<Path> getTemplatesPaths();
    Optional<Path> getTemplateBasePath();
    IDirectoryHtml generateHtml();
    List<ITheme> getThemes();
}
