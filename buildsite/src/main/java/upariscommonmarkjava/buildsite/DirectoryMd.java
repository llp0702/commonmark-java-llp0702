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
    protected  ArrayList<String> paths_md = null;
    protected ItoMLFile toml_options = null;

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

    protected DirectoryMd(File toml, File content) throws IOException
    {
        this.toml_options = initOption(toml);
        paths_md = new ArrayList<>();

        if(content == null)
            return;

        for(File file : content.listFiles()) {
            if (file.getName().contains(".md")) {
                paths_md.add(file.getAbsolutePath());
            }
        }
    }

    public ArrayList<String> getPaths()
    {
        return this.paths_md;
    }

    public DirectoryHtml generateHtml()
    {
        return DirectoryHtml.create(this.toml_options,this.paths_md);
    }
}
