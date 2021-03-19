package upariscommonmarkjava.md2html.implementations.metadata;

import org.tomlj.TomlArray;
import org.tomlj.TomlTable;

import java.util.List;

public interface IMetaData {
    String toHtml();
    List<Object> toList();

    static IMetaData buildMetaData(Object obj){
        if(obj instanceof TomlTable)
            return new MetaDataTable((TomlTable)obj);

        if(obj instanceof TomlArray)
            return new MetaDataArray((TomlArray)obj);

        return new MetaDataValue(obj);
    }
}
