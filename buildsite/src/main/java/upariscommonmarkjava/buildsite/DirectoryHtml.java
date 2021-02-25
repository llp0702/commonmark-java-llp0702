package upariscommonmarkjava.buildsite;

import upariscommonmarkjava.md2html.implementations.CMFile;
import upariscommonmarkjava.md2html.implementations.ConverterMd2Html;
import upariscommonmarkjava.md2html.interfaces.ICMFile;
import upariscommonmarkjava.md2html.interfaces.IConverterMd2Html;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class DirectoryHtml {

    protected HashMap<String,String> files;

    public static DirectoryHtml create(ArrayList<String> htmlFiles, HashMap<String, String> options)
    {
        return new DirectoryHtml(htmlFiles,options);
    }

    protected DirectoryHtml(ArrayList<String> paths, HashMap<String, String> option_toml)
    {
        files = new HashMap();
        for(String path : paths)
        {
            String name = name_html(path);
            files.put(path, name);
        }
    }

    private String name_html(String path_md)
    {
        return new File(path_md.substring(0, path_md.length() - 2) + "html").getName();
    }

    public boolean isSimilare(DirectoryMd d)
    {

        if(d.getPaths().size() != this.files.size())
            return false;

        for(String path_md : d.getPaths()) {
            if (!this.files.containsKey(path_md))
                return false;
        }
        return true;
    }

    public boolean isSimilare(File folder)
    {
        return false;
    }

    //create path\_output\...
    public void save(String path) throws IOException
    {
        save(path,"_output");
    }

    //create path\dir\...
    public void save(String path, String dir) throws IOException
    {
        for(String path_md : this.files.keySet())
        {
            String name_html = files.get(path_md);

            Path inputPath = Paths.get(path_md);
            Path output_folder = Paths.get(path,dir);
            Path outputPath = Paths.get(output_folder.toString(), name_html);
            ICMFile cmFile = CMFile.fromPath(inputPath);
            IConverterMd2Html converterMd2Html = new ConverterMd2Html();

            File tmp = new File(output_folder.toString());
            tmp.mkdirs();

            converterMd2Html.parseAndConvert2HtmlAndSave(cmFile, outputPath);
        }
    }
}
