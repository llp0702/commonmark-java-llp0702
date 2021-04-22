package upariscommonmarkjava.md2html.implementations.extensions.htmltemplate;

import org.tomlj.TomlTable;
import upariscommonmarkjava.md2html.interfaces.metadata.IMetaData;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/** Class de remplacement et template pour la fonctionnalité template++ */
public class AdvancedHtmlTemplate extends HtmlTemplate {
    protected static final String PATTERN_FOR = "\\{%[ ]*for[ ]+(.*?)[ ]+in[ ]+metadata\\.(.*?)[ ]*%\\}((\\r|\\n|.)*?)\\{%[ ]*endfor[ ]*%\\}";
    protected static final String PATTERN_IF_ELSE = "\\{%[ ]*?if[ ]+metadata\\.(.*?)[ ]*?%\\}([^$]*?)(\\{%[ ]*else if[ ]+metadata\\..*?[ ]*?%\\}[^$]*?)?(\\{%[ ]*else[ ]*%\\}((\\r|\\n|.)*?))?\\{%[ ]*endif[ ]*%\\}";
    protected static final String PATTERN_ELSEIF = "\\{%[ ]*else if[ ]+metadata\\.(.*?)[ ]*?%\\}([^\\{]*)";

    public AdvancedHtmlTemplate(String md2HtmlContent, ITOMLFile metadataGlobal, List<Map<String,Object>> tomlMetadata, List<Path> templates, String content) {
        super(md2HtmlContent, metadataGlobal, tomlMetadata, templates, content);
    }

    /**
     * Applique le remplacement pour les templates For et If
     * Applique aussi les pattern basique
     */
    @Override
    public String apply() {
        this.replace(PATTERN_FOR,this::replaceFor);
        this.replace(PATTERN_IF_ELSE,this::replaceIfElse);
        return super.apply();
    }

    /** Fonction remplacant le pattern For */
    protected final String replaceFor(final Matcher matcher) {
        final String element = matcher.group(1).trim();
        final String iterable = matcher.group(2).trim();
        final String innerContent = matcher.group(3).trim();

        final StringBuilder sb = new StringBuilder(innerContent.length());

        getMetadata(iterable).ifPresent(metaData -> {
            for (final Object tomlObject : metaData.toList()) {
                sb.append((tomlObject instanceof TomlTable) ?
                    matchForTomlTable(element, innerContent, (TomlTable) tomlObject) :
                    matchAndReplace("\\{\\{[ ]*" + element + "[ ]*\\}\\}", innerContent ,
                            m -> tomlObject.toString()));
            }
        });

        return sb.toString();
    }

    /** remplacement pour le cas spécifique des TomTable
     * @param element le nom de la variable du for
     * @param innerContent le contenu du for
     * @param table la TomlTable
     * @return le text auquel on a remplacé le for
     */
    protected final String matchForTomlTable(final String element, final String innerContent, final TomlTable table) {
        return new AdvancedHtmlTemplate(fileContent,metadataGlobal,buildMetaDataLocal(List.of(table)),this.templates,
                matchAndReplace("\\{\\{[ ]*" + element + "(\\.[^ ]+?)[ ]*\\}\\}", innerContent,
                        m -> "{{ metadata" + m.group(1).trim() + " }}")).apply();
    }

    /** Evalue la métadata en un boolean
     * @param variable le nom de la métadata
     */
    protected final boolean evalBoolean(final String variable) {
        final Optional<IMetaData> metaDataOptional = getMetadata(variable);
        if(metaDataOptional.isEmpty()) {
            logger.warning("The value is not a boolean");
            return false;
        }

        final String content = metaDataOptional.get().toHtml();
        if (!content.equals("true") && !content.equals("false"))
            logger.warning("The value is not a boolean");

        return content.equals("true");
    }

    /** Fonction remplacant le pattern If else */
    protected final String replaceIfElse(final Matcher matcher) {
        if(evalBoolean(matcher.group(1).trim()))
            return matcher.group(2).trim();

        if(matcher.groupCount() >= 3 && matcher.group(3) != null) {
            final Matcher matchElseIf = Pattern.compile(PATTERN_ELSEIF).matcher(matcher.group(3));
            while(matchElseIf.find())
            {
                if(evalBoolean(matchElseIf.group(1).trim()))
                    return matchElseIf.group(2).trim();
            }
        }

        if(matcher.groupCount() >= 4 && matcher.group(4) != null)   //ELSE
            return matcher.group(5).trim();

        return "";  //Cas aucun vrai et pas de else
    }
}