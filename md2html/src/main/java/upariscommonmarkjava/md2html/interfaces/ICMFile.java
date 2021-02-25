package upariscommonmarkjava.md2html.interfaces;

import org.tomlj.TomlParseResult;

import java.io.Reader;

public interface ICMFile {
    Reader getReader();
    void setTomlMetadata(TomlParseResult parseResult);
}
