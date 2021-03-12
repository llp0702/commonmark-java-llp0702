package upariscommonmarkjava.buildsite.directorymd;

import lombok.Getter;
import upariscommonmarkjava.buildsite.directoryhtml.DirectoryHtml;
import upariscommonmarkjava.buildsite.SiteFormatException;
import upariscommonmarkjava.buildsite.directoryhtml.IDirectoryHtml;
import upariscommonmarkjava.md2html.implementations.TomlFile;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DirectoryMd implements IDirectoryMd{
    @Getter
    protected final List<Path> mdFilesPaths;

    @Getter
    protected final List<Path> staticFilesPaths;

    protected final ITOMLFile tomlOptions;

    @Getter
    protected final Path basePath;


    public static DirectoryMd open(final String path) throws SiteFormatException
    {
        File folder = new File(path);
        if(!folder.isDirectory())throw new SiteFormatException("The file is not a folder");

        File[] files = folder.listFiles();

        if(files==null)throw new SiteFormatException("No files found");

        Optional<File> optToml = Arrays.stream(files)
                .filter(x -> x.getName().equals("site.toml")).findAny();
        if(optToml.isEmpty())throw new SiteFormatException("No Site.Toml found ! ");

        Optional<File> optContent = Arrays.stream(files)
                .filter(x -> x.getName().equals("content")).findAny();
        if(optContent.isEmpty())throw new SiteFormatException("No content folder ! ");
        if(!optContent.get().isDirectory())throw new SiteFormatException("Content is not a folder ! ");
        File content = optContent.get();
        File[] contentFiles = content.listFiles();
        if(contentFiles==null)throw new SiteFormatException("Content is empty");
        Optional<File> optIndex = Arrays.stream(contentFiles)
                .filter(x -> x.getName().equals("index.md")).findAny();
        if (optIndex.isEmpty())throw new SiteFormatException("No index.md found ! ");

        Optional<File> optTemplatesDir = Arrays.stream(files)
                .filter(x->"templates".equals(x.getName())).findAny();

        try
        {
            return optTemplatesDir.isEmpty() ?
            new DirectoryMd(optToml.get(), content) :
            new DirectoryMdWithTemplate(optToml.get(), content, optTemplatesDir.get().toPath());
        }
        catch (IOException ioe)
        {
            throw new SiteFormatException("Error IOException : " + ioe.getMessage());
        }
    }

    private ITOMLFile initOption(File toml) throws IOException
    {
        TomlFile it = TomlFile.fromPath(Paths.get(toml.getAbsolutePath()));
        it.parse();
        return it;
    }

    protected void parcours(File content, String basePath){
        if(content == null)
            return;
        File[] contentFiles = content.listFiles();
        if(contentFiles==null)
            return;

        for(File file : contentFiles) {
            if(file == null)
                continue;

            if(file.isDirectory())
            {
                parcours(file, basePath + file.getName() + "/");
            }
            else if (file.getName().endsWith(".md"))
            {
                mdFilesPaths.add(Paths.get(basePath,  file.getName()));
            }
            else
            {
                staticFilesPaths.add(Paths.get(basePath, file.getName()));
            }
        }
    }

    protected DirectoryMd(File toml, File content) throws IOException
    {
        this.basePath = content.toPath();
        this.tomlOptions = initOption(toml);
        mdFilesPaths = new ArrayList<>();
        staticFilesPaths = new ArrayList<>();
        parcours(content,"");
    }



    public IDirectoryHtml generateHtml()
    {
        return DirectoryHtml.create(this.basePath,this.tomlOptions,this.mdFilesPaths,this.staticFilesPaths, new ArrayList<>());
    }
}
