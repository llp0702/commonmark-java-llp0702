package upariscommonmarkjava.buildsite.directorymd;

import lombok.NonNull;
import upariscommonmarkjava.buildsite.SiteFormatException;
import upariscommonmarkjava.buildsite.directoryhtml.DirectoryHtml;
import upariscommonmarkjava.buildsite.directoryhtml.DirectoryHtmlParallel;
import upariscommonmarkjava.buildsite.directoryhtml.IDirectoryHtml;
import upariscommonmarkjava.buildsite.theme.ITheme;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DirectoryMdParallel implements IDirectoryMd {
    private final DirectoryMd directoryMd;

    private final int nb_thread;

    public DirectoryMdParallel(final Path folderPath, int nb_thread) throws SiteFormatException{
        this.directoryMd = DirectoryMd.open(folderPath);
        this.nb_thread = nb_thread;
    }

    @Override
    public Path getContentBasePath() {
        return directoryMd.getContentBasePath();
    }

    @Override
    public List<Path> getMdFilesPaths() {
        return directoryMd.getMdFilesPaths();
    }

    @Override
    public List<Path> getStaticFilesPaths() {
        return directoryMd.getStaticFilesPaths();
    }

    @Override
    public List<Path> getTemplatesPaths() {
        return directoryMd.getTemplatesPaths();
    }

    @Override
    public Optional<Path> getTemplateBasePath() {
        return directoryMd.getTemplateBasePath();
    }

    @Override
    public IDirectoryHtml generateHtml() {
        final DirectoryHtmlParallel directoryHtmlParallel = (DirectoryHtmlParallel)directoryMd.generateHtml(DirectoryHtmlParallel::new);
        directoryHtmlParallel.setNbThread(this.nb_thread);
        return directoryHtmlParallel;
    }

    @Override
    public List<ITheme> getThemes() {
        return directoryMd.getThemes();
    }
}
