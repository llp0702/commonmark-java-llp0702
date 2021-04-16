package upariscommonmarkjava.md2html.implementations.extensions.htmltemplate;

import org.tomlj.TomlTable;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;

import java.io.File;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class MysteryTemplate extends AdvancedHtmlTemplate{

    private static final String PATTERN_LIST_FILE = "list_files\\([ ]*\"(.*?)\"[ ]*,[ ]*(true|false)\\)";

    public MysteryTemplate(String md2HtmlContent, ITOMLFile metadataGlobal, List<TomlTable> tomlMetadata, List<Path> templates, String content) {
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

    private ArrayList<String> list_files(String directory, boolean b) {

        File current = new File(directory);
        ArrayList<String> result = new ArrayList<>();
        if (!current.isDirectory())
            return new ArrayList<>();
        ArrayList<String> files = new ArrayList<>();
        ArrayList<String> directiories = new ArrayList<>();
        if (b) {
            for (String filename : current.list()) {
                File tmp = new File(filename);
                if (tmp.isDirectory())
                    directiories.add(filename);
                else
                    files.add(filename);
            }

            for (String dirname : directiories)
                result.addAll(list_files(dirname, true));
            result.addAll(files);
        }
        else {
            for (String file : current.list()) {
                File tmp = new File(file);
                if (!tmp.isDirectory())
                    result.add(file);
            }
        }
        return result;
    }
}
