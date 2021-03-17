package upariscommonmarkjava.md2html.implementations.extensions.htmltemplate;

import lombok.experimental.SuperBuilder;
import org.tomlj.TomlArray;
import org.tomlj.TomlTable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

@SuperBuilder
public class AdvancedHtmlTemplate extends HtmlTemplate {
    protected static final String PATTERN_FOR = "\\{%[ ]*for[ ]+(.*?)[ ]+in[ ]+(.*?)[ ]*%\\}((\\r|\\n|.)*?)\\{% endfor %\\}";

    @Override
    public String apply() {
        this.templateContent = matchAndReplace(PATTERN_FOR, templateContent, this::replaceFor);
        return super.apply();
    }

    private List<Object> getIterableTomlList(Object elementObject)
    {
        final List<Object> l = new ArrayList<>();

        if (elementObject instanceof TomlArray)
            l.addAll(((TomlArray) elementObject).toList());
        else if(elementObject instanceof TomlTable)
            l.addAll(((TomlTable) elementObject).toMap().values());
        else
            logger.warning("Not iterable ");

        return l;
    }
    
    private String replaceFor(Matcher matcher) {
        final String element = matcher.group(1).trim();
        final String array = matcher.group(2).trim();
        final String innerContent = matcher.group(3).trim();

        final StringBuilder sb = new StringBuilder(innerContent.length());

        for (final Object tomlObject : getIterableTomlList(super.getMetadata(array))) {
            sb.append(tomlObject instanceof TomlTable ?
                    matchForTomlTable(element, innerContent, (TomlTable) tomlObject) :
                    matchAndReplace("\\{\\{[ ]*" + element + "[ ]*\\}\\}", innerContent ,
                            m -> tomlObject.toString()));
        }

        return sb.toString();
    }

    private String matchForTomlTable(String element, String innerContent, TomlTable table) {
            return matchTemplate(md2HtmlContent,metadataGlobal,List.of(table),this.templates,
                    matchAndReplace("\\{\\{[ ]*" + element + "(\\.[^ ]+?)[ ]*\\}\\}", innerContent,
                        m -> "{{ metadata" + m.group(1).trim() + " }}"));
    }
}