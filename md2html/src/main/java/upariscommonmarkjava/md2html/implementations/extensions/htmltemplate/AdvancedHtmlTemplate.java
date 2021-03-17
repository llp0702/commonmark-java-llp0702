package upariscommonmarkjava.md2html.implementations.extensions.htmltemplate;

import lombok.experimental.SuperBuilder;
import org.tomlj.TomlArray;
import org.tomlj.TomlTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

@SuperBuilder
public class AdvancedHtmlTemplate extends HtmlTemplate {

    @Override
    public String apply() throws IOException {
        StringBuilder sb = matchAndReplace("\\{%[ ]*for[ ]+(.*?)[ ]+in[ ]+(.*?)[ ]*%\\}((\\r|\\n|.)*?)\\{% endfor %\\}", templateContent,
                this::matchFor);

        this.templateContent = sb.toString();
        return super.apply();
    }

    private void matchFor(Matcher matcher, StringBuilder result) {
        String element = matcher.group(1).trim();
        String array = matcher.group(2).trim();
        String innerContent = matcher.group(3).trim();

        Object elementObject = super.getMetadata(array);
        StringBuilder sb = new StringBuilder(innerContent.length());

        List<Object> l;
        if (elementObject instanceof TomlArray) {
            l = ((TomlArray) elementObject).toList();
        }
        else if(elementObject instanceof TomlTable) {
            l = new ArrayList<>(((TomlTable) elementObject).toMap().values());
        }
        else {
            logger.warning("Element non iterable");
            l = List.of();
        }

        for (Object o : l) {
            if(o instanceof TomlTable)
            {
                matchForTomlTable(element, innerContent, sb, (TomlTable) o);
            }
            else
            {
                sb.append(matchAndReplace("\\{\\{[ ]*" + element + "[ ]*\\}\\}", innerContent , (m,tmp) ->
                        m.appendReplacement(tmp, Matcher.quoteReplacement(o.toString()))));
            }
        }

        matcher.appendReplacement(result, Matcher.quoteReplacement(sb.toString()));
    }

    private void matchForTomlTable(String element, String innerContent, StringBuilder sb, TomlTable table) {
        try {
            sb.append(HtmlTemplate.builder()
                    .md2HtmlContent(this.md2HtmlContent)
                    .metadataGlobal(this.metadataGlobal)
                    .tomlMetadata(List.of(table))
                    .templateContent(
                            matchAndReplace("\\{\\{[ ]*" + element + "(\\.[^ ]+?)[ ]*\\}\\}", innerContent,
                                    (m,tmp) -> m.appendReplacement(
                                            tmp, Matcher.quoteReplacement(
                                                    "{{ metadata" + m.group(1).trim() + " }}"
                                            )
                                    )).toString())
                    .templates(this.templates).build().apply());
        }catch(IOException ioe)
        {
            logger.warning(ioe.getMessage());
        }
    }
}
