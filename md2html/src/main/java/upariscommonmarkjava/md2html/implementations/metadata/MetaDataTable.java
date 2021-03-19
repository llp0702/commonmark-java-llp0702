package upariscommonmarkjava.md2html.implementations.metadata;

import org.tomlj.TomlTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MetaDataTable implements IMetaData {
    private final TomlTable tomlTable;
    public MetaDataTable(TomlTable tomlTable) {
        this.tomlTable = tomlTable;
    }

    @Override
    public String toHtml() {
        final StringBuilder result = new StringBuilder();

        result.append("<ul>");

        for(Map.Entry<String, Object> entry : tomlTable.toMap().entrySet()){
            final IMetaData meta = IMetaData.buildMetaData(entry.getValue());
            result.append("<li>").append(entry.getKey()).append(" : ").append(meta.toHtml()).append("</li>");
        }

        result.append("</ul>");
        return result.toString();
    }

    @Override
    public List<Object> toList() {
        return new ArrayList<>(this.tomlTable.toMap().values());
    }
}
