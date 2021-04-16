package upariscommonmarkjava;

import upariscommonmarkjava.buildsite.directoryhtml.DirectoryHtml;
import upariscommonmarkjava.buildsite.directorymd.DirectoryMd;
import upariscommonmarkjava.buildsite.directorymd.IDirectoryMd;

import java.nio.file.Path;

public class SimilaireDirectoryTest {
    public static boolean isSimilare(final DirectoryHtml dh, final IDirectoryMd d)
    {
        if(d.getMdFilesPaths().size() != dh.getInputFilesMdPaths().size())
            return false;

        for(final Path path_md : d.getMdFilesPaths()) {
            if (!dh.getInputFilesMdPaths().contains(path_md))
                return false;
        }
        return true;
    }
}
