package upariscommonmarkjava.md2html.interfaces;

import lombok.NonNull;
import org.tomlj.TomlParseResult;
import java.util.List;

public interface ICMFile extends IFile{
    void setTomlMetadataLocal(@NonNull List<TomlParseResult> parseResult);
    boolean isDraft();
    List<TomlParseResult> getTomlMetadataLocal();
}
