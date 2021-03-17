package upariscommonmarkjava.md2html.implementations.extensions.htmltemplate;

import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.tomlj.TomlArray;
import org.tomlj.TomlInvalidTypeException;
import org.tomlj.TomlTable;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;
import upariscommonmarkjava.md2html.interfaces.extensions.htmltemplate.IHtmlTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuperBuilder
public class HtmlTemplate implements IHtmlTemplate {
    public static final Logger logger = Logger.getLogger("Html Template logger");

    String md2HtmlContent;
    ITOMLFile metadataGlobal;
    List<TomlTable> tomlMetadata;
    String templateContent;
    List<Path> templates;

    public static Object getMetadata(@NonNull String key,ITOMLFile metadataGlobal,List<TomlTable> tomlMetadata)
    {
        for(TomlTable curMetadata:tomlMetadata){
            if(curMetadata==null)continue;
            Object curObject = curMetadata.get(key);
            try{
                if(curObject!=null){
                    return curObject;
                }
            }catch(IllegalArgumentException| TomlInvalidTypeException e){
                logger.warning("Exception when parsing key "+key+" : "+e);
            }
        }
        if(metadataGlobal == null || metadataGlobal.getData() == null)return null;
        try{
            Object curObject = metadataGlobal.getData().get(key);
            if(curObject!=null){
                return curObject;
            }
        }catch(IllegalArgumentException| TomlInvalidTypeException e){
            logger.warning("Exception when parsing key "+key+" : "+e);
        }
        return null;
    }

    protected static StringBuilder  matchAndReplace(String pattern, String innerContent, BiConsumer<Matcher,StringBuilder> replace) {
        Matcher replaceElement = Pattern.compile(pattern).matcher(innerContent);
        StringBuilder tmpReplace = new StringBuilder(innerContent.length());

        while(replaceElement.find())
        {
            replace.accept(replaceElement,tmpReplace);
        }

        replaceElement.appendTail(tmpReplace);
        return tmpReplace;
    }

    protected Object getMetadata(@NonNull String key){
        return getMetadata(key,metadataGlobal,tomlMetadata);
    }

    private String allMetadataToHtml(){
        StringBuilder res=new StringBuilder();
        for(TomlTable curMetadata:tomlMetadata){
            res.append(metadataToHtml(curMetadata));
        }
        return res.toString();
    }
    private String metadataToHtml(Object metadata){
        if(metadata==null)return "";
        StringBuilder result=new StringBuilder();
        if(metadata instanceof TomlTable){
            Map<String, Object> map = ((TomlTable)metadata).toMap();
            result.append("<ul>");
            for(Map.Entry<String, Object> e:map.entrySet()){
                result.append("<li>")
                        .append(e.getKey())
                        .append(" : ")
                        .append(metadataToHtml(e.getValue()))
                        .append("</li>");
            }
            result.append("</ul>");
            return result.toString();
        }else if(metadata instanceof TomlArray){
            List<Object> lst = ((TomlArray)metadata).toList();
            result.append("<ul>");
            lst.forEach(x->result.append("<li>").append(x.toString()).append("</li>"));
            result.append("</ul>");
            return result.toString();
        }
        return metadata.toString();
    }

    public String apply() throws IOException {
        return matchAndReplace("\\{\\{(.*?)\\}\\}",templateContent, this::applyHtmlTemplate).toString();
    }

    private void applyHtmlTemplate(Matcher matcher, StringBuilder result){
        String currentMatch = matcher.group(1).trim();
        //Process current match then append it
        if("content".equalsIgnoreCase(currentMatch.trim())){
            currentMatch = md2HtmlContent;
        }else{
            String[] splittedDot = currentMatch.split("[ ]*\\.[ ]*");
            if(splittedDot.length>=1 && "metadata".equalsIgnoreCase(splittedDot[0].trim())){
                currentMatch = metadataCase(splittedDot);
            }else{
                String[] splittedSpace = currentMatch.split("[ ]+");
                if(splittedDot.length>1 && "include".equalsIgnoreCase(splittedSpace[0].trim())){
                    try {
                        currentMatch = includeCase(splittedSpace);
                    }
                    catch(IOException ioe)
                    {
                        logger.warning("During applyHtmlTemplate : " + ioe.getMessage());
                        currentMatch = "";
                    }
                }
            }
        }
        //Append
        matcher.appendReplacement(result, Matcher.quoteReplacement(currentMatch));
    }

    private String includeCase(@NonNull String[] splittedSpace) throws IOException {
        String currentMatch;
        String toIncludeName = String.join(" ", Arrays.copyOfRange(splittedSpace, 1, splittedSpace.length)).replace("\"","");
        Path toInclude = this.templates.stream().filter(x->toIncludeName.equals(x.getFileName().toString()))
                .findAny().orElse(null);
        if(toInclude==null){
            currentMatch = "";
        }else{
            currentMatch = HtmlTemplate.builder()
                    .md2HtmlContent(this.md2HtmlContent)
                    .metadataGlobal(this.metadataGlobal)
                    .tomlMetadata(this.tomlMetadata)
                    .templateContent(Files.readString(toInclude))
                    .templates(this.templates).build().apply();
        }
        return currentMatch;
    }

    private String metadataCase(@NonNull String[] splittedDot) {
        String currentMatch;
        if(splittedDot.length==1){
            currentMatch = allMetadataToHtml();
        }else{
            String metadataSelected = String.join(".", Arrays.copyOfRange(splittedDot, 1, splittedDot.length));
            currentMatch = metadataToHtml(getMetadata(metadataSelected));
        }
        return currentMatch;
    }
}
