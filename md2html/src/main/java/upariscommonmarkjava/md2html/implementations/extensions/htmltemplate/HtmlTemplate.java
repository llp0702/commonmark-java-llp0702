package upariscommonmarkjava.md2html.implementations.extensions.htmltemplate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;
import upariscommonmarkjava.md2html.interfaces.extensions.htmltemplate.IHtmlTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Builder
public class HtmlTemplate implements IHtmlTemplate {
    private Pattern REGEX_MOTIF = Pattern.compile("\\{\\{(.*?)\\}\\}");

    String md2HtmlContent;
    ITOMLFile metadataGlobal;
    List<TomlParseResult> tomlMetadata;
    String templateContent;

    public String apply(){
        Matcher matcher  = REGEX_MOTIF.matcher(templateContent);
        StringBuffer result = new StringBuffer(templateContent.length());
        while (matcher.find()){
            String currentMatch = matcher.group(1).trim();
            //Process current match then append it
            if("content".equals(currentMatch)){

            }else{
                String[] splittedDot = currentMatch.split(".");
                if(splittedDot.length>1 && "metadata".equals(splittedDot[0])){
                    String metadataSelected = String.join(".", Arrays.copyOfRange(splittedDot, 1, splittedDot.length));

                }else{
                    String[] splittedSpace = currentMatch.split(" ");
                    if(splittedDot.length>1 && "include".equals(splittedSpace[0])){
                        String toInclude = String.join(".", Arrays.copyOfRange(splittedSpace, 1, splittedSpace.length));
                    }
                }
            }
            //Append
            matcher.appendReplacement(result, Matcher.quoteReplacement(currentMatch));
        }
        matcher.appendTail(result);
        return result.toString();

    }
}
