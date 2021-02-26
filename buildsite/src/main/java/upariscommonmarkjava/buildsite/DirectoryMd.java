package upariscommonmarkjava.buildsite;

import upariscommonmarkjava.md2html.implementations.TomlFile;
import upariscommonmarkjava.md2html.interfaces.ItoMLFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

public class DirectoryMd {
    protected  ArrayList<String> paths_md;
    protected  ArrayList<String> paths_other;
    protected ItoMLFile toml_options;
    private String input_path;

    public static DirectoryMd open(String path) throws SiteFormatException
    {
        File folder = new File(path);
        //System.out.println(folder.getAbsolutePath());

        File[] files = folder.listFiles();

        if(folder.isDirectory())
        {
            Optional<File> optToml = Arrays.stream(files)
                    .filter(x -> x.getName().equals("site.toml")).findAny();

            if(optToml.isPresent())
            {
                Optional<File> optContent = Arrays.stream(files)
                        .filter(x -> x.getName().equals("content")).findAny();

                if(optContent.isPresent())
                {
                    if(optContent.get().isDirectory()) {
                        Optional<File> optIndex = Arrays.stream(optContent.get().listFiles())
                                .filter(x -> x.getName().equals("index.md")).findAny();

                        if (optIndex.isPresent())
                        {
                            try
                            {
                                return new DirectoryMd(optToml.get(), optContent.get());
                            }
                            catch (IOException ioe)
                            {
                                throw new SiteFormatException("Error IOException : " + ioe.getMessage());
                            }
                        }
                        else
                        {
                            throw new SiteFormatException("No index.md founded ! ");
                        }
                    }
                    else
                    {
                        throw new SiteFormatException("Content is not a folder ! ");
                    }
                }
                else
                {
                    throw new SiteFormatException("No content folder ! ");
                }
            }
            else
            {
                throw new SiteFormatException("No Site.Toml founded ! ");
            }
        }
        else
        {
            throw new SiteFormatException("The file is not a folder!");
        }
    }

    private ItoMLFile initOption(File toml) throws IOException
    {
        TomlFile it = TomlFile.fromPath(Paths.get(toml.getAbsolutePath()));
        it.parse();
        return it;
    }

    protected void parcours(File content, String path){
        if(content == null)
            return;

        for(File file : content.listFiles()) {
            if(file == null)
                continue;

            if(file.isDirectory())
            {
                parcours(file, path + "/" + file.getName());
            }
            else if (file.getName().endsWith(".md"))
            {
                paths_md.add(path + "/" + file.getName());
            }
            else
            {
                paths_other.add(path + "/" + file.getName());
            }
        }
    }

    protected DirectoryMd(File toml, File content) throws IOException
    {
        this.input_path = content.getAbsolutePath();
        this.toml_options = initOption(toml);
        paths_md = new ArrayList<>();
        paths_other = new ArrayList<>();

        parcours(content,"");
    }

    public ArrayList<String> getPaths()
    {
        return this.paths_md;
    }

    public DirectoryHtml generateHtml()
    {
        return DirectoryHtml.create(this.input_path,this.toml_options,this.paths_md,this.paths_other);
    }
}
