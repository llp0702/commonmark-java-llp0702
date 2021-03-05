package upariscommonmarkjava.buildsite;

import upariscommonmarkjava.md2html.implementations.CMFile;
import upariscommonmarkjava.md2html.implementations.ConverterMd2Html;
import upariscommonmarkjava.md2html.interfaces.ICMFile;
import upariscommonmarkjava.md2html.interfaces.IConverterMd2Html;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DirectoryHtml {

    protected HashMap<String,String> files;
    protected ITOMLFile tomlOptions;
    protected List<String> staticFiles;
    protected List<Path> templatesFiles;
    private final String inputPath;
    public static DirectoryHtml create(String input_path, ITOMLFile toml_options, List<String> htmlFiles,
                                       List<String> staticFiles, List<Path> templatesFiles)
    {
        return new DirectoryHtml(input_path,toml_options,htmlFiles,staticFiles, templatesFiles);
    }

    protected DirectoryHtml(String inputPath, ITOMLFile tomlOptions, List<String> htmlFiles, List<String> staticFiles,
                            List<Path> templatesFiles)
    {
        this.inputPath = inputPath;
        this.tomlOptions = tomlOptions;
        files = new HashMap<>();
        for(String path : htmlFiles)
        {
            files.put(path, nameHtml(path));
        }
        this.staticFiles = staticFiles;
        this.templatesFiles = templatesFiles;
    }

    private String nameHtml(String path_md)
    {
        return (path_md.substring(0, path_md.length() - 2) + "html");
    }

    //create path\_output\...
    public void save(String path) throws IOException
    {
        save(path,"_output");
    }

    //create path\dir\...
    public void save(String path, String dir) throws IOException
    {
        Path output_folder = Paths.get(path, dir);
        File tmp = new File(output_folder.toString());
        if(tmp.exists())
            Files.walkFileTree(output_folder,
                    new SimpleFileVisitor<>() {

                        // delete directories or folders
                        @Override
                        public FileVisitResult postVisitDirectory(Path dir,
                                                                  IOException exc)
                                throws IOException {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }

                        // delete files
                        @Override
                        public FileVisitResult visitFile(Path file,
                                                         BasicFileAttributes attrs)
                                throws IOException {
                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }
                    }
            );
        if(!tmp.mkdirs()){
            Logger.getAnonymousLogger().log(Level.INFO,"No dir was created");
        }

        for(Map.Entry<String, String> entry : this.files.entrySet())
        {
            final String path_md = entry.getKey();
            String name_html = files.get(path_md);

            Path currentInputPath = Paths.get(this.inputPath,path_md);

            Path outputPath = Paths.get(output_folder.toString(), name_html);
            Files.createDirectories(outputPath.getParent());

            ICMFile cmFile = CMFile.fromPath(currentInputPath);
            IConverterMd2Html converterMd2Html = new ConverterMd2Html();

            converterMd2Html.parseAndConvert2HtmlAndSave(cmFile, tomlOptions, outputPath, templatesFiles);
        }

        for(String static_path : this.staticFiles) {
            Path input = Paths.get(inputPath,static_path);
            Path output =  Paths.get(path, dir, static_path);
            Files.createDirectories(output);
            Files.copy(input,output,StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
