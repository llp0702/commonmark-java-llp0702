package upariscommonmarkjava.md2html.implementations.metadata;

import upariscommonmarkjava.md2html.interfaces.metadata.IMetaData;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MetaDataValue implements IMetaData {
    public static final Logger logger = Logger.getLogger("MetaDataValue logger");
    final Object value;

    public MetaDataValue(Object value) {
        this.value = value;
    }

    @Override
    public String toHtml() {
        return value.toString();
    }

    @Override
    public List<Object> toList() {
        if(value instanceof List)
            return (List<Object>) value;

        logger.warning("Not iterable " + value.getClass());
        return new ArrayList<>();
    }
}
