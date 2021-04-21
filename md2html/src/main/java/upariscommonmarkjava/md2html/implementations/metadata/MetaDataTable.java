package upariscommonmarkjava.md2html.implementations.metadata;

import org.tomlj.TomlTable;
import upariscommonmarkjava.md2html.interfaces.metadata.IMetaData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MetaDataTable implements IMetaData {
    private final Map<String,Object> table;

    public MetaDataTable(final TomlTable tomlTable) {
        table = tomlTable.toMap();
    }

    public MetaDataTable(final Map<String,Object> table) {
        this.table = table;
    }

    @Override
    public String toHtml() {
        final StringBuilder result = new StringBuilder();

        result.append("<ul>");

        for(Map.Entry<String, Object> entry : table.entrySet()){
            final IMetaData meta = IMetaData.buildMetaData(entry.getValue());
            result.append("<li>").append(entry.getKey()).append(" : ").append(meta.toHtml()).append("</li>");
        }

        result.append("</ul>");
        return result.toString();
    }

    @Override
    public List<Object> toList() {
        return new ArrayList<>( this.table.values());
    }
}
