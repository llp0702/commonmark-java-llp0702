package upariscommonmarkjava.md2html.implementations.extensions.htmltemplate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.tomlj.TomlParseResult;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;
import upariscommonmarkjava.md2html.interfaces.extensions.htmltemplate.IHtmlTemplate;

import java.util.List;

@AllArgsConstructor
public class HtmlTemplate implements IHtmlTemplate {
    String md2HtmlContent;
    ITOMLFile metadataGlobal;
    List<TomlParseResult> tomlMetadata;
    String templateContent;

    public String apply(){
        return "";
    }
}
