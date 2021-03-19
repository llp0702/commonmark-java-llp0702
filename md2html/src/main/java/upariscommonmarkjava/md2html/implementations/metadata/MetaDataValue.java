package upariscommonmarkjava.md2html.implementations.metadata;

import java.util.ArrayList;
import java.util.List;

public class MetaDataValue implements IMetaData {

    Object value;

    public MetaDataValue(Object value) {
        this.value = value;
    }

    @Override
    public String toHtml() {
        return value.toString();
    }

    @Override
    public List<Object> toList() {
        System.err.println("Not iterable ");
        return new ArrayList<>();
    }
}
