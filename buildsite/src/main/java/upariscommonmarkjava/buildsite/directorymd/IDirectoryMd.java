package upariscommonmarkjava.buildsite.directorymd;

import upariscommonmarkjava.buildsite.directoryhtml.IDirectoryHtml;
import upariscommonmarkjava.buildsite.theme.ITheme;

import java.nio.file.Path;
import java.util.List;

public interface IDirectoryMd  {
    Path getBasePath();
    List<Path> getMdFilesPaths();
    List<Path> getStaticFilesPaths();
    List<Path> getTemplatesPaths();
    public IDirectoryHtml generateHtml();
    List<ITheme> getThemes();
}
