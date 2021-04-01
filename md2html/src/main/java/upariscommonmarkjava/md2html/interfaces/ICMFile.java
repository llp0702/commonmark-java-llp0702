package upariscommonmarkjava.md2html.interfaces;

import lombok.NonNull;
import org.tomlj.TomlTable;

import java.util.List;

public interface ICMFile extends IFile{
    void setTomlMetadataLocal(@NonNull List<TomlTable> parseResult);
    boolean isDraft();
    List<TomlTable> getTomlMetadataLocal();
}
