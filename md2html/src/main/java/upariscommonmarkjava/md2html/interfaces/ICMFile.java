package upariscommonmarkjava.md2html.interfaces;

import org.tomlj.TomlParseResult;

import java.io.Reader;

public interface ICMFile extends IFile{
    void setTomlMetadata(TomlParseResult parseResult);
}
