package upariscommonmarkjava.md2html.implementations.extensions.htmltemplate;

import lombok.experimental.SuperBuilder;
import org.tomlj.TomlArray;
import org.tomlj.TomlTable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuperBuilder
public class AdvancedHtmlTemplate extends HtmlTemplate {
    private static final String PATTERN_FOR = "\\{%[ ]*for[ ]+(.*?)[ ]+in[ ]+(.*?)[ ]*%\\}((\\r|\\n|.)*?)\\{%[ ]*endfor[ ]*%\\}";
    private static final String PATTERN_IF_ELSE = "\\{%[ ]*?if[ ]+(.*?)[ ]*?%\\}([^$]*?)(\\{%[ ]*else if[ ]+.*?[ ]*?%\\}[^$]*?)?(\\{%[ ]*else[ ]*%\\}((\\r|\\n|.)*?))?\\{%[ ]*endif[ ]*%\\}";
    private static final String PATTERN_ELSEIF = "\\{%[ ]*else if[ ]+(.*?)[ ]*?%\\}([^\\{]*)";

    @Override
    public String apply() {
        this.replace(PATTERN_FOR,this::replaceFor);
        this.replace(PATTERN_IF_ELSE,this::replaceIfElse);
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

    private boolean isTrue(final String variable) {
        final String tomlString = getMetadata(variable).toString();

        if(!tomlString.equals("true") && !tomlString.equals("false"))
            logger.warning("The value is not a boolean");

        return tomlString.equals("true");
    }

    private String replaceIfElse(final Matcher matcher) {
        //LE IF est Vrai
        if(isTrue(matcher.group(1).trim()))
            return matcher.group(2).trim();

        if(matcher.groupCount() >= 3 && matcher.group(3) != null) {
            final Matcher matchElseIf = Pattern.compile(PATTERN_ELSEIF).matcher(matcher.group(3));
            while(matchElseIf.find())
            {
                //ELSEIF Vrai
                if(isTrue(matchElseIf.group(1).trim()))
                    return matchElseIf.group(2).trim();
            }
        }

        //LE ELSE est Vrai
        if(matcher.groupCount() >= 4 && matcher.group(4) != null)   //ELSE
            return matcher.group(5).trim();

        //Cas aucun vrai et pas de else
        return "";
    }
}