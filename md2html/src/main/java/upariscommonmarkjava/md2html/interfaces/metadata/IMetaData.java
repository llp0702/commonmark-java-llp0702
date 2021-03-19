package upariscommonmarkjava.md2html.interfaces.metadata;

import org.tomlj.TomlArray;
import org.tomlj.TomlTable;
import upariscommonmarkjava.md2html.implementations.metadata.MetaDataArray;
import upariscommonmarkjava.md2html.implementations.metadata.MetaDataTable;
import upariscommonmarkjava.md2html.implementations.metadata.MetaDataValue;

import java.util.List;

public interface IMetaData {
    String toHtml();
    List<Object> toList();

    static IMetaData buildMetaData(final Object obj){
        if(obj instanceof TomlTable)
            return new MetaDataTable((TomlTable)obj);

        if(obj instanceof TomlArray)
            return new MetaDataArray((TomlArray)obj);

        return new MetaDataValue(obj);
    }
}
