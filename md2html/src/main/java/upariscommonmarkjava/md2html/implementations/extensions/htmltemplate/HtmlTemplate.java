package upariscommonmarkjava.md2html.implementations.extensions.htmltemplate;

import lombok.NonNull;
import org.tomlj.TomlTable;
import upariscommonmarkjava.md2html.implementations.metadata.IMetaData;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;
import upariscommonmarkjava.md2html.interfaces.extensions.htmltemplate.IHtmlTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlTemplate implements IHtmlTemplate {
    private static final String PATTERN_VAR = "\\{\\{(.*?)\\}\\}";

    public static final Logger logger = Logger.getLogger("Html Template logger");

    protected String md2HtmlContent;
    protected ITOMLFile metadataGlobal;
    protected List<TomlTable> tomlMetadata;
    protected List<Path> templates;
    protected String templateContent;

    protected HtmlTemplate(String md2HtmlContent, ITOMLFile metadataGlobal, List<TomlTable> tomlMetadata, List<Path> templates, String content){
        this.md2HtmlContent = md2HtmlContent;
        this.metadataGlobal = metadataGlobal;
        this.tomlMetadata = tomlMetadata;
        this.templates = templates;
        this.templateContent = content;
    }

    public String apply() {
        replace(PATTERN_VAR, this::applyHtmlTemplate);
        return this.templateContent;
    }

    protected void replace(final String Pattern, final Function<Matcher,String> fun) {
        this.templateContent = matchAndReplace(Pattern, this.templateContent,fun);
    }

    public static String buildTemplate(String md2HtmlContent, ITOMLFile metadataGlobal, List<TomlTable> tomlMetadata, List<Path> templates, String content) {
        return new HtmlTemplate(md2HtmlContent,metadataGlobal,tomlMetadata,templates,content).apply();
    }

    protected static String matchAndReplace(String pattern, String innerContent, Function<Matcher,String> replace) {
        Matcher replaceElement = Pattern.compile(pattern).matcher(innerContent);
        StringBuilder tmpReplace = new StringBuilder(innerContent.length());

        while(replaceElement.find())
            replaceElement.appendReplacement(tmpReplace, Matcher.quoteReplacement(replace.apply(replaceElement)));

        replaceElement.appendTail(tmpReplace);
        return tmpReplace.toString();
    }

    protected Optional<IMetaData> getMetadata(@NonNull String key){
        for(TomlTable curMetadata : tomlMetadata)
        {
            if(curMetadata.contains(key))
                return Optional.of(IMetaData.buildMetaData(curMetadata.get(key)));
        }

        if(metadataGlobal.getData() == null)
            return Optional.empty();

        if(metadataGlobal.getData().contains(key))
            return Optional.of(IMetaData.buildMetaData(metadataGlobal.getData().get(key)));

        return Optional.empty();
    }

    private String allMetadataToHtml(){
        StringBuilder res = new StringBuilder();

        for(TomlTable curMetadata:tomlMetadata)
            res.append( IMetaData.buildMetaData(curMetadata).toHtml());

        return res.toString();
    }

    private String applyHtmlTemplate(Matcher matcher){
        final String currentMatch = matcher.group(1).trim();

        //Process current match then append it
        if("content".equalsIgnoreCase(currentMatch.trim()))
            return md2HtmlContent;

        final String[] splittedDot = currentMatch.split("[ ]*\\.[ ]*");
        if(splittedDot.length >= 1 && "metadata".equalsIgnoreCase(splittedDot[0].trim()))
            return metadataCase(splittedDot);

        final String[] splittedSpace = currentMatch.split("[ ]+");
        if(splittedDot.length > 1 && "include".equalsIgnoreCase(splittedSpace[0].trim()))
            return includeCase(splittedSpace);

        return currentMatch;
    }

    private String includeCase(@NonNull String[] splittedSpace) {
        String toIncludeName = String.join(" ", Arrays.copyOfRange(splittedSpace, 1, splittedSpace.length)).replace("\"","");

        Optional<Path> toInclude = this.templates.stream().filter(x -> toIncludeName.equals(x.getFileName().toString())).findAny();

        if(toInclude.isEmpty())
           return "";

        try
        {
            final String content = Files.readString(toInclude.get());
            return buildTemplate(md2HtmlContent,metadataGlobal,tomlMetadata,templates,content);
        }
        catch(IOException ioe) {
            logger.warning("During include : " + ioe.getMessage());
            return "";
        }
    }

    private String metadataCase(@NonNull String[] splittedDot) {
        if(splittedDot.length == 1)
            return allMetadataToHtml();

        String metadataSelected = String.join(".", Arrays.copyOfRange(splittedDot, 1, splittedDot.length));
        Optional<IMetaData> currentMetadata = getMetadata(metadataSelected);
        if(currentMetadata.isPresent())
            return currentMetadata.get().toHtml();
        else
            return metadataSelected;
    }
}
