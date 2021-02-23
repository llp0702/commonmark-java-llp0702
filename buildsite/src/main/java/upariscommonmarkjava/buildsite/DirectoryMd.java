package upariscommonmarkjava.buildsite;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class DirectoryMd {
    protected  ArrayList<String> paths_md = null;
    protected HashMap<String, String> options = null;

    public static DirectoryMd open(String path) throws SiteFormatException
    {
        File folder = new File(path);
        //System.out.println(folder.getAbsolutePath());

        if(folder.isDirectory())
        {
            Optional<File> optToml = Arrays.stream(folder.listFiles())
                    .filter(x -> x.getName().equals("site.toml")).findAny();

            if(optToml.isPresent())
            {
                Optional<File> optContent = Arrays.stream(folder.listFiles())
                        .filter(x -> x.getName().equals("content")).findAny();

                if(optContent.isPresent())
                {
                    if(optContent.get().isDirectory()) {
                        Optional<File> optIndex = Arrays.stream(optContent.get().listFiles())
                                .filter(x -> x.getName().equals("index.md")).findAny();

                        if (optIndex.isPresent())
                        {
                            return new DirectoryMd(optToml.get(), optContent.get());
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

    private void initOption(File toml)
    {
        options = new HashMap<>();

        //TODO
    }

    protected DirectoryMd(File toml, File content)
    {
        initOption(toml);

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
        return DirectoryHtml.create(this.paths_md,this.options);
    }
}
