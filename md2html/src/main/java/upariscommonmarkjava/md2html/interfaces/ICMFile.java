package upariscommonmarkjava.md2html.interfaces;

import org.tomlj.TomlParseResult;

public interface ICMFile extends IFile{
    void setTomlMetadata(TomlParseResult parseResult);
}
