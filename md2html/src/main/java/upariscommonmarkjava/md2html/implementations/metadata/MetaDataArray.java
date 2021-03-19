package upariscommonmarkjava.md2html.implementations.metadata;

import org.tomlj.TomlArray;
import upariscommonmarkjava.md2html.interfaces.metadata.IMetaData;

import java.util.ArrayList;
import java.util.List;

public class MetaDataArray implements IMetaData {
    private final TomlArray tomlArray;
    public MetaDataArray(TomlArray tomlArray) {
        this.tomlArray = tomlArray;
    }

    @Override
    public String toHtml() {
        final StringBuilder result = new StringBuilder();
        result.append("<ul>");

        tomlArray.toList().forEach(x -> result.append("<li>").append(x.toString()).append("</li>"));

        result.append("</ul>");
        return result.toString();
    }

    @Override
    public List<Object> toList() {
        return new ArrayList<>(tomlArray.toList());
    }

}
