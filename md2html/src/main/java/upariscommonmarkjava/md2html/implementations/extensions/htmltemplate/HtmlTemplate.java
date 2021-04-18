package upariscommonmarkjava.md2html.implementations.extensions.htmltemplate;

import lombok.NonNull;
import org.tomlj.TomlTable;
import upariscommonmarkjava.md2html.interfaces.metadata.IMetaData;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;
import upariscommonmarkjava.md2html.interfaces.extensions.htmltemplate.IHtmlTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlTemplate implements IHtmlTemplate {
    private static final String PATTERN_VAR = "\\{\\{(.*?)\\}\\}";

    public static final Logger logger = Logger.getLogger("Html Template logger");

    public static List<Map<String,Object>> buildMetaDataLocal(final List<TomlTable> metadata){
        final List<Map<String,Object>> list = new ArrayList<>();
        for(TomlTable meta : metadata)
            list.add(meta.toMap());
        return list;
    }

    protected final String md2HtmlContent;
    protected final ITOMLFile metadataGlobal;
    protected final List<Map<String,Object>> tomlMetadata;
    protected final List<Path> templates;

    protected String templateContent;

    protected HtmlTemplate(final String md2HtmlContent, final ITOMLFile metadataGlobal, final List<Map<String,Object>> tomlMetadata, final List<Path> templates, final String content){
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

    protected static String matchAndReplace(final String pattern, final String innerContent, final Function<Matcher,String> replace) {
        final Matcher replaceElement = Pattern.compile(pattern).matcher(innerContent);
        final StringBuilder tmpReplace = new StringBuilder(innerContent.length());

        while(replaceElement.find())
            replaceElement.appendReplacement(tmpReplace, Matcher.quoteReplacement(replace.apply(replaceElement)));

        replaceElement.appendTail(tmpReplace);
        return tmpReplace.toString();
    }

    protected Optional<IMetaData> getMetadata(@NonNull final String key){
        for(final Map<String,Object> curMetadata : tomlMetadata)
        {
            if(curMetadata.containsKey(key))
                return Optional.of(IMetaData.buildMetaData(curMetadata.get(key)));
        }

        if(metadataGlobal.getData() == null)
            return Optional.empty();

        if(metadataGlobal.getData().contains(key))
            return Optional.of(IMetaData.buildMetaData(metadataGlobal.getData().get(key)));

        return Optional.empty();
    }

    private String allMetadataToHtml(){
        final StringBuilder res = new StringBuilder();

        for(final Map<String,Object> curMetadata:tomlMetadata)
            res.append( IMetaData.buildMetaData(curMetadata).toHtml());

        return res.toString();
    }

    private String applyHtmlTemplate(final Matcher matcher){
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

    private String includeCase(@NonNull final String[] splittedSpace) {
        final String toIncludeName = String.join(" ", Arrays.copyOfRange(splittedSpace, 1, splittedSpace.length)).replace("\"","");

        final Optional<Path> toInclude = this.templates.stream().filter(x -> toIncludeName.equals(x.getFileName().toString())).findAny();

        if(toInclude.isEmpty())
           return "";

        try
        {
            final String content = Files.readString(toInclude.get());
            return new HtmlTemplate(md2HtmlContent,metadataGlobal,tomlMetadata,templates,content).apply();
        }
        catch(IOException ioe) {
            logger.warning("During include : " + ioe.getMessage());
        }
        return "";
    }

    private String metadataCase(@NonNull final String[] splittedDot) {
        if(splittedDot.length == 1)
            return allMetadataToHtml();

        final String metadataSelected = String.join(".", Arrays.copyOfRange(splittedDot, 1, splittedDot.length));
        final Optional<IMetaData> currentMetadata = getMetadata(metadataSelected);

        if(currentMetadata.isPresent())
            return currentMetadata.get().toHtml();
        else
            return metadataSelected;
    }
}
