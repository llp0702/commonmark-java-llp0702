package upariscommonmarkjava.md2html.interfaces;

import org.tomlj.TomlParseResult;
import java.util.List;

public interface ICMFile extends IFile{
    void setTomlMetadataLocal(List<TomlParseResult> parseResult);
    boolean isDraft();
    List<TomlParseResult> getTomlMetadataLocal();
}
