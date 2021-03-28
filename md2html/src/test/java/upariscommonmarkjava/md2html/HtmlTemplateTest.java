package upariscommonmarkjava.md2html;

import org.junit.jupiter.api.BeforeEach;
import upariscommonmarkjava.md2html.implementations.CMFile;
import upariscommonmarkjava.md2html.implementations.ConverterMd2Html;
import upariscommonmarkjava.md2html.implementations.extensions.htmltemplate.HtmlTemplate;

public class HtmlTemplateTest {

    HtmlTemplate htmlTemplate;

    @BeforeEach
    public void init()
    {
        ConverterMd2Html convert = new ConverterMd2Html();


        /*htmlTemplate = HtmlTemplate(md2HtmlContent, , final List<TomlTable> tomlMetadata, final List<Path> templates, final String content){
        this.md2HtmlContent = md2HtmlContent;
        this.metadataGlobal = metadataGlobal;
        this.tomlMetadata = tomlMetadata;
        this.templates = templates;
        this.templateContent = content;*/
    }
}