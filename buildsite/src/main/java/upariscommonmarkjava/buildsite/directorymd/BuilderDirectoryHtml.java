package upariscommonmarkjava.buildsite.directorymd;

import upariscommonmarkjava.buildsite.directoryhtml.DirectoryHtml;
import upariscommonmarkjava.buildsite.theme.ITheme;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/** Interface fonctionnel de génération de directoryhtml */
@FunctionalInterface
interface BuilderDirectoryHtml{
    DirectoryHtml apply(final Path inputContentBasePath, final ITOMLFile tomlOptions,final List<Path> mdFilesPaths,final List<Path> staticFilesPaths, final List<Path> asciiFilesPaths, final List<Path> templatesPaths, final Optional<ITheme> theme);
}
