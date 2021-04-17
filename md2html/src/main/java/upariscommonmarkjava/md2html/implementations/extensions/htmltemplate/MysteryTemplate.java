package upariscommonmarkjava.md2html.implementations.extensions.htmltemplate;

import upariscommonmarkjava.md2html.interfaces.ITOMLFile;

import java.io.File;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class MysteryTemplate extends AdvancedHtmlTemplate{

    private static final String PATTERN_LIST_FILE = "list_files\\([ ]*\"(.*?)\"[ ]*,[ ]*(true|false)\\)";

    public MysteryTemplate(String md2HtmlContent, ITOMLFile metadataGlobal, List<Map<String,Object>> tomlMetadata, List<Path> templates, String content) {
        super(md2HtmlContent, metadataGlobal, tomlMetadata, templates, content);
    }

    @Override
    public String apply() {
        this.replace(PATTERN_FOR,this::replaceFor);
        this.replace(PATTERN_IF_ELSE,this::replaceIfElse);
        this.replace(PATTERN_LIST_FILE,this::replaceListFiles);
        return super.apply();
    }

    protected String replaceListFiles(final Matcher matcher) {
        final String directory = matcher.group(1).trim();
        final String bool = matcher.group(2).trim();
        final boolean rec = this.evalBoolean(bool);
        ArrayList<String> files = list_files(directory, rec);

        return "";
    }

    /**
     * Renvoie les chemins relatifs des fichiers contnenus dans le répertoire passé en argument
     *
     * @param directory repértoire cible
     * @param rec chercher recursivement tous les fichiers dans les sous répertoires de directory
     * @return
     */
    public ArrayList<String> list_files(String directory, boolean rec) {
       return list_files(directory, rec, "");
    }

    private ArrayList<String> list_files(String directory, boolean rec, String parent) {
        ArrayList<String> result = new ArrayList<>();
        File current = new File(directory);
        if (!current.isDirectory())
            return result;

        File[] files = current.listFiles();
        if (rec) {

            for (File f : files) {
                if (f.isDirectory())
                    result.addAll(list_files(f.getAbsolutePath(), true, parent+f.getName()+"/"));
            }
        }
        for (File f : files) {
            if (!f.isDirectory())
                result.add(parent+f.getName());
        }
        return result;
    }

}
