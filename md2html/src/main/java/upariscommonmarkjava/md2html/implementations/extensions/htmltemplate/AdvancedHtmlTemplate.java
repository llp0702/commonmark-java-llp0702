package upariscommonmarkjava.md2html.implementations.extensions.htmltemplate;

import org.tomlj.TomlTable;
import upariscommonmarkjava.md2html.implementations.metadata.IMetaData;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdvancedHtmlTemplate extends HtmlTemplate {
    private static final String PATTERN_FOR = "\\{%[ ]*for[ ]+(.*?)[ ]+in[ ]+(.*?)[ ]*%\\}((\\r|\\n|.)*?)\\{%[ ]*endfor[ ]*%\\}";
    private static final String PATTERN_IF_ELSE = "\\{%[ ]*?if[ ]+(.*?)[ ]*?%\\}([^$]*?)(\\{%[ ]*else if[ ]+.*?[ ]*?%\\}[^$]*?)?(\\{%[ ]*else[ ]*%\\}((\\r|\\n|.)*?))?\\{%[ ]*endif[ ]*%\\}";
    private static final String PATTERN_ELSEIF = "\\{%[ ]*else if[ ]+(.*?)[ ]*?%\\}([^\\{]*)";

    protected AdvancedHtmlTemplate(String md2HtmlContent, ITOMLFile metadataGlobal, List<TomlTable> tomlMetadata, List<Path> templates, String content) {
        super(md2HtmlContent, metadataGlobal, tomlMetadata, templates, content);
    }

    public static String buildTemplate(String md2HtmlContent, ITOMLFile metadataGlobal, List<TomlTable> tomlMetadata, List<Path> templates, String content) {
        return new AdvancedHtmlTemplate(md2HtmlContent,metadataGlobal,tomlMetadata,templates,content).apply();
    }

    @Override
    public String apply() {
        this.replace(PATTERN_FOR,this::replaceFor);
        this.replace(PATTERN_IF_ELSE,this::replaceIfElse);
        return super.apply();
    }

    private String replaceFor(final Matcher matcher) {
        final String element = matcher.group(1).trim();
        final String iterable = matcher.group(2).trim();
        final String innerContent = matcher.group(3).trim();

        final StringBuilder sb = new StringBuilder(innerContent.length());

        getMetadata(iterable).ifPresent(metaData -> {
            for (final Object tomlObject : metaData.toList()) {
                sb.append(tomlObject instanceof TomlTable ?
                    matchForTomlTable(element, innerContent, (TomlTable) tomlObject) :
                    matchAndReplace("\\{\\{[ ]*" + element + "[ ]*\\}\\}", innerContent ,
                            m -> tomlObject.toString()));
            }
        });

        return sb.toString();
    }

    private String matchForTomlTable(final String element, final String innerContent, final TomlTable table) {
        return buildTemplate(md2HtmlContent,metadataGlobal,List.of(table),this.templates,
                matchAndReplace("\\{\\{[ ]*" + element + "(\\.[^ ]+?)[ ]*\\}\\}", innerContent,
                        m -> "{{ metadata" + m.group(1).trim() + " }}"));
    }

    private boolean evalBoolean(final String variable) {
        Optional<IMetaData> metaDataOptional = getMetadata(variable);
        if(metaDataOptional.isEmpty()) {
            logger.warning("The value is not a boolean");
            return false;
        }

        final String content = metaDataOptional.get().toHtml();
        if (!content.equals("true") && !content.equals("false"))
            logger.warning("The value is not a boolean");

        return content.equals("true");
    }

    private String replaceIfElse(final Matcher matcher) {
        //LE IF est Vrai
        if(evalBoolean(matcher.group(1).trim()))
            return matcher.group(2).trim();

        if(matcher.groupCount() >= 3 && matcher.group(3) != null) {
            final Matcher matchElseIf = Pattern.compile(PATTERN_ELSEIF).matcher(matcher.group(3));
            while(matchElseIf.find())
            {
                //ELSEIF Vrai
                if(evalBoolean(matchElseIf.group(1).trim()))
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