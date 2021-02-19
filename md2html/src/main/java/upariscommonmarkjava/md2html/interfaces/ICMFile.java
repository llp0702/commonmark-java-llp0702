package upariscommonmarkjava.md2html.interfaces;

import upariscommonmarkjava.md2html.implementations.CMFile;

import java.io.Reader;
import java.nio.file.Path;

public interface ICMFile {
    Reader getReader();
}
