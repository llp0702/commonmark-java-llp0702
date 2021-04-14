package upariscommonmarkjava.buildsite.directoryhtml;

import upariscommonmarkjava.buildsite.theme.ITheme;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@FunctionalInterface
interface BuilderDirectoryHtml{
    DirectoryHtml apply(Path inputContentBasePath, ITOMLFile tomlOptions,List<Path> mdFilesPaths, List<Path> staticFilesPaths, List<Path> templatesPaths, Optional<ITheme> theme);
}
