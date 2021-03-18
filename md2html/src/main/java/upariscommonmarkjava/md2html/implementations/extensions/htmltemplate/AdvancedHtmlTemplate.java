package upariscommonmarkjava.md2html.implementations.extensions.htmltemplate;

import lombok.experimental.SuperBuilder;
import org.tomlj.TomlArray;
import org.tomlj.TomlTable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

@SuperBuilder
public class AdvancedHtmlTemplate extends HtmlTemplate {
    private static final String PATTERN_FOR = "\\{%[ ]*for[ ]+(.*?)[ ]+in[ ]+(.*?)[ ]*%\\}((\\r|\\n|.)*?)\\{%[ ]*endfor[ ]*%\\}";
    private static final String PATTERN_IF_ELSE = "\\{%[ ]*if[ ]+(.*?)[ ]*%\\}((\\r|\\n|.)*?)\\{%[ ]*else[ ]*%\\}((\\r|\\n|.)*?)\\{%[ ]*endif[ ]*%\\}";
    private static final String PATTERN_IF = "\\{%[ ]*if[ ]+(.*?)[ ]*%\\}((\\r|\\n|.)*?)\\{%[ ]*endif[ ]*%\\}";

    @Override
    public String apply() {
        this.replace(PATTERN_FOR,this::replaceFor);
        this.replace(PATTERN_IF_ELSE,this::replaceIfElse);
        this.replace(PATTERN_IF,this::replaceIf);
        return super.apply();
    }

    private List<Object> getIterableTomlList(Object elementObject) {
        final List<Object> l = new ArrayList<>();

        if (elementObject instanceof TomlArray)
            l.addAll(((TomlArray) elementObject).toList());
        else if(elementObject instanceof TomlTable)
            l.addAll(((TomlTable) elementObject).toMap().values());
        else
            logger.warning("Not iterable ");

        return l;
    }
    
    private String replaceFor(final Matcher matcher) {
        final String element = matcher.group(1).trim();
        final String array = matcher.group(2).trim();
        final String innerContent = matcher.group(3).trim();

        final StringBuilder sb = new StringBuilder(innerContent.length());

        for (final Object tomlObject : getIterableTomlList(getMetadata(array))) {
            sb.append(tomlObject instanceof TomlTable ?
                    matchForTomlTable(element, innerContent, (TomlTable) tomlObject) :
                    matchAndReplace("\\{\\{[ ]*" + element + "[ ]*\\}\\}", innerContent ,
                            m -> tomlObject.toString()));
        }

        return sb.toString();
    }

    private String matchForTomlTable(final String element, final String innerContent, final TomlTable table) {
        return matchTemplate(md2HtmlContent,metadataGlobal,List.of(table),this.templates,
                matchAndReplace("\\{\\{[ ]*" + element + "(\\.[^ ]+?)[ ]*\\}\\}", innerContent,
                        m -> "{{ metadata" + m.group(1).trim() + " }}"));
    }

    private boolean isA(final String variable, final String value) {
        return getMetadata(variable).toString().equals(value);
    }

    private boolean isBoolean(final String cond) {
        return isA(cond,"true") || isA(cond,"false");
    }

    private String replaceIfStatement(final String statement, final String cond) {
        return isA(cond, "true") ? statement : "";
    }

    private String replaceElseStatement(final String statement, final String cond) {
        return isA(cond, "false") ? statement : "";
    }

    private String replaceIf(final Matcher matcher) {
        final String cond = matcher.group(1).trim();
        final String ifStatement = matcher.group(2).trim();

        if(!isBoolean(cond))
            logger.warning("The value is not a boolean");

        return replaceIfStatement(ifStatement,cond);
    }

    private String replaceIfElse(final Matcher matcher) {
        final String cond = matcher.group(1).trim();
        final String elseStatement = matcher.group(4).trim();

        return replaceIf(matcher) + replaceElseStatement(elseStatement,cond);
    }
}