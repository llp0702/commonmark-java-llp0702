package upariscommonmarkjava.buildsite;

import upariscommonmarkjava.md2html.implementations.CMFile;
import upariscommonmarkjava.md2html.implementations.ConverterMd2Html;
import upariscommonmarkjava.md2html.interfaces.ICMFile;
import upariscommonmarkjava.md2html.interfaces.IConverterMd2Html;
import upariscommonmarkjava.md2html.interfaces.ItoMLFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;

public class DirectoryHtml {

    protected HashMap<String,String> files;
    protected ItoMLFile toml_options;
    protected ArrayList<String> staticFiles;
    private String input_path;
    public static DirectoryHtml create(String input_path,ItoMLFile toml_options,ArrayList<String> htmlFiles, ArrayList<String> staticFiles)
    {
        return new DirectoryHtml(input_path,toml_options,htmlFiles,staticFiles);
    }

    protected DirectoryHtml(String input_path,ItoMLFile toml_options,ArrayList<String> htmlFiles, ArrayList<String> staticFiles)
    {
        this.input_path = input_path;
        this.toml_options = toml_options;
        files = new HashMap();
        for(String path : htmlFiles)
        {
            String name = name_html(path);
            files.put(path, name);
        }

        this.staticFiles = staticFiles;
    }

    private String name_html(String path_md)
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
        Path output_folder = Paths.get(path,dir);
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
                            System.out.printf("Directory is deleted : %s%n", dir);
                            return FileVisitResult.CONTINUE;
                        }

                        // delete files
                        @Override
                        public FileVisitResult visitFile(Path file,
                                                         BasicFileAttributes attrs)
                                throws IOException {
                            Files.delete(file);
                            System.out.printf("File is deleted : %s%n", file);
                            return FileVisitResult.CONTINUE;
                        }
                    }
            );

        tmp.mkdirs();


        for(String path_md : this.files.keySet())
        {
            String name_html = files.get(path_md);

            Path inputPath = Paths.get(input_path,path_md);

            Path outputPath = Paths.get(output_folder.toString(), name_html);
            Files.createDirectories(outputPath.getParent());

            ICMFile cmFile = CMFile.fromPath(inputPath);
            IConverterMd2Html converterMd2Html = new ConverterMd2Html();

            converterMd2Html.parseAndConvert2HtmlAndSave(cmFile, outputPath);
        }

        for(String static_path : this.staticFiles) {
            Path input = Paths.get(input_path,static_path);
            Path output =  Paths.get(path, dir, static_path);
            Files.createDirectories(output);
            Files.copy(input,output,StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
