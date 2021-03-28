package upariscommonmarkjava.buildsite.directorymd;

import upariscommonmarkjava.buildsite.directoryhtml.IDirectoryHtml;

import java.nio.file.Path;
import java.util.List;

public interface IDirectoryMd  {
    Path getBasePath();
    List<Path> getMdFilesPaths();
    List<Path> getStaticFilesPaths();
    IDirectoryHtml generateHtml();
}
