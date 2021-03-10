package upariscommonmarkjava.md2html.implementations.extensions.htmltemplate;

import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.w3c.css.util.Warning;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdvancedHtmlTemplate extends HtmlTemplate {
    AdvancedHtmlTemplate(String md2HtmlContent, ITOMLFile metadataGlobal, List<TomlParseResult> tomlMetadata, String templateContent, List<Path> templates) {
        super(md2HtmlContent, metadataGlobal, tomlMetadata, templateContent, templates);
    }

    @Override
    public String apply() throws IOException {
        Matcher matcher = Pattern.compile("\\{%[ ]*for[ ]+(.*?)[ ]+in[ ]+(.*?)[ ]*%\\}(.*?)\\{% endfor %\\}").matcher(templateContent);

        StringBuffer result = new StringBuffer(templateContent.length());
        while (matcher.find()) {
            String element = matcher.group(1).trim();
            String array = matcher.group(2).trim();
            String inner_content = matcher.group(3).trim();

            Object element_object = super.getMetadata(array);
            if (element_object instanceof TomlArray) {
                for (Object o : ((TomlArray) element_object).toList()) {
                    Matcher match_element = Pattern.compile("\\{\\{[ ]*" + element + "(\\.[^ ]+?)\\}\\}").matcher(inner_content);
                    StringBuffer result_element = new StringBuffer(inner_content.length());

                    while (match_element.find()) {
                        String currentMatch = matcher.group(1).trim();
                        currentMatch = "{{ metadata" + currentMatch + " }}";
                        match_element.appendReplacement(result, Matcher.quoteReplacement(currentMatch));
                    }
                    match_element.appendTail(result_element);

                    result.append(HtmlTemplate.builder()
                            .md2HtmlContent(this.md2HtmlContent)
                            .metadataGlobal(this.metadataGlobal)
                            .tomlMetadata(List.of((TomlParseResult) o))
                            .templateContent(result_element.toString())
                            .templates(this.templates).build().apply());
                }
            } else {
                logger.warning("Element non iterable");
                array = "";
                element = "";
            }
        }

        //TODO

        return super.apply();
    }
}
