package upariscommonmarkjava.md2html.implementations.extensions.htmltemplate;

import lombok.experimental.SuperBuilder;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuperBuilder
public class AdvancedHtmlTemplate extends HtmlTemplate {

    @Override
    public String apply() throws IOException {
        Matcher matcherFor = Pattern.compile("\\{%[ ]*for[ ]+(.*?)[ ]+in[ ]+(.*?)[ ]*%\\}((\\r|\\n|.)*?)\\{% endfor %\\}").matcher(templateContent);
        StringBuffer result = new StringBuffer(templateContent.length());

        while (matcherFor.find()) {
            matchFor(matcherFor, result);
        }

        matcherFor.appendTail(result);
        this.templateContent = result.toString();
        return super.apply();
    }

    private void matchFor(Matcher matcher, StringBuffer result) throws IOException {
        String element = matcher.group(1).trim();
        String array = matcher.group(2).trim();
        String innerContent = matcher.group(3).trim();

        Object elementObject = super.getMetadata(array);

        StringBuilder sb = new StringBuilder();

        if (elementObject instanceof TomlArray) {
            for (Object o : ((TomlArray) elementObject).toList()) {

                Matcher matchElement = Pattern.compile("\\{\\{[ ]*" + element + "(\\.[^ ]+?)\\}\\}").matcher(innerContent);
                StringBuffer resultElement = new StringBuffer(innerContent.length());

                while (matchElement.find()) {
                    String currentMatch = matcher.group(1).trim();
                    currentMatch = "{{ metadata" + currentMatch + " }}";
                    matchElement.appendReplacement(sb, Matcher.quoteReplacement(currentMatch));
                }

                matchElement.appendTail(resultElement);

                if(o instanceof TomlTable)
                {
                    sb.append(HtmlTemplate.builder()
                            .md2HtmlContent(this.md2HtmlContent)
                            .metadataGlobal(this.metadataGlobal)
                            .tomlMetadata(List.of((TomlParseResult) o))
                            .templateContent(resultElement.toString())
                            .templates(this.templates).build().apply());
                } else {

                    Matcher replaceElement = Pattern.compile("\\{\\{[ ]*" + element + "[ ]*\\}\\}").matcher(innerContent);

                    StringBuilder tmpReplace = new StringBuilder(innerContent.length());

                    while(replaceElement.find())
                    {
                        replaceElement.appendReplacement(tmpReplace, Matcher.quoteReplacement(o.toString()));
                    }

                    replaceElement.appendTail(tmpReplace);
                    sb.append(tmpReplace);
                }
            }
        } else {
            logger.warning("Element non iterable");
        }

        matcher.appendReplacement(result, Matcher.quoteReplacement(sb.toString()));
    }
}
