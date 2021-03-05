package upariscommonmarkjava.md2html.interfaces;

import org.tomlj.TomlParseResult;

public interface ITOMLFile extends IFile{

     void setData(TomlParseResult parseResult);
     TomlParseResult getData();

}
