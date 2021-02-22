package upariscommonmarkjava.buildsite;

import java.io.File;
import java.io.IOException;
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
            if (!this.files.containsKey(name_html(path_md)))
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
            //IParserMd2Html parser = new ParserMd2Html(CMFile.fromString(path_md),Paths.get(path, dir, name_html));
        }
    }
}
