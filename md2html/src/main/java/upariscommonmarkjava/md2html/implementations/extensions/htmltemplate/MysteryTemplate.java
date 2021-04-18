package upariscommonmarkjava.md2html.implementations.extensions.htmltemplate;

import upariscommonmarkjava.md2html.interfaces.ITOMLFile;

import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;

public class MysteryTemplate extends AdvancedHtmlTemplate{
    private static final String PATTERN_LIST_FILE = "list_files\\([ ]*\"(.*?)\"[ ]*,[ ]*(true|false)[ ]*\\)";

    private static int key_file_array = 0;

    public MysteryTemplate(final String md2HtmlContent, final ITOMLFile metadataGlobal, final List<Map<String,Object>> tomlMetadata, final List<Path> templates,final String content) {
        super(md2HtmlContent, metadataGlobal, tomlMetadata, templates, content);
    }

    @Override
    public String apply() {
        this.replace(PATTERN_LIST_FILE,this::replaceListFiles);
        return super.apply();
    }

    private String replaceListFiles(final Matcher matcher) {
        key_file_array++;
        final String name_list_file = "_list_files_" + key_file_array;

        final String directory = matcher.group(1).trim();
        final boolean rec = Boolean.parseBoolean(matcher.group(2).trim());
        final ArrayList<String> files = new ArrayList<>();

        Path dir = Paths.get(directory);
        if(!dir.isAbsolute())
                dir = Paths.get(System.getProperty("user.dir"), dir.toString());
        if(rec)
            list_files_rec(dir,dir,files);
        else
            list_files(dir,files);

        final Map<String,Object> meta = new HashMap<>();
        meta.put(name_list_file, files);
        tomlMetadata.add(meta);
        return name_list_file;
    }

    /**
     * Renvoie les chemins relatifs des fichiers contnenus dans le répertoire passé en argument
     *
     * @param directory repértoire cible
     * @param result la liste des chemins en String
     */

    private void list_files(final Path directory,final ArrayList<String> result) {
        final File[] currents = directory.toFile().listFiles();
        if(currents != null)
             Arrays.stream(currents)
                     .filter(File::isFile)
                     .map(File::toPath)
                     .map(directory::relativize)
                     .map(Path::toString)
                     .forEach(result::add);
    }

    private void list_files_rec(final Path initial_directory,final Path directory,final ArrayList<String> result) {
        final File current = directory.toFile();

        if (current.isDirectory()) {
            final File[] files = current.listFiles();

            if(files != null)
                for (final File file : files)
                    list_files_rec(initial_directory,file.toPath() , result);
        }
        else
            result.add(initial_directory.relativize(current.toPath()).toString());
    }
}
