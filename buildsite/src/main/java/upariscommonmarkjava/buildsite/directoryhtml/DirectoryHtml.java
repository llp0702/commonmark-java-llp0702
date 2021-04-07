package upariscommonmarkjava.buildsite.directoryhtml;

import lombok.Getter;
import lombok.NonNull;
import upariscommonmarkjava.buildsite.theme.ITheme;
import upariscommonmarkjava.md2html.implementations.CMFile;
import upariscommonmarkjava.md2html.implementations.ConverterMd2Html;
import upariscommonmarkjava.md2html.implementations.incremental.Hierarchie;
import upariscommonmarkjava.md2html.interfaces.ICMFile;
import upariscommonmarkjava.md2html.interfaces.IConverterMd2Html;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirectoryHtml implements IDirectoryHtml {
    public static final Logger logger = Logger.getLogger("Directory html logger");

    @Getter
    private final List<Path> inputFilesMdPaths;


    protected ITOMLFile tomlOptions;
    protected List<Path> staticFilesPaths;
    protected List<Path> templatesPaths;
    protected List<Path> directory;
    protected Hierarchie hier;
    protected ITheme theme;
    private final Path inputContentBasePath;

    public static DirectoryHtml create(@NonNull Path inputPath, ITOMLFile tomlOptions, @NonNull List<Path> mdFilesPaths,
                                       List<Path> staticFiles, List<Path> templatesFiles, ITheme theme, Optional<Hierarchie> hierOpt) {
        return new DirectoryHtml(inputPath, tomlOptions, mdFilesPaths, staticFiles, templatesFiles, theme,hierOpt);
    }

    protected DirectoryHtml(@NonNull Path inputContentBasePath, ITOMLFile tomlOptions, @NonNull List<Path> mdFilesPaths, List<Path> staticFilesPaths,
                            List<Path> templatesPaths, ITheme theme, Optional<Hierarchie> hierOpt) {
        this.inputContentBasePath = inputContentBasePath;

        this.tomlOptions = tomlOptions;

        this.inputFilesMdPaths = mdFilesPaths;

        this.staticFilesPaths = staticFilesPaths;
        if(this.staticFilesPaths ==null)this.staticFilesPaths = new ArrayList<>();

        this.templatesPaths = templatesPaths;
        if(this.templatesPaths ==null)this.templatesPaths = new ArrayList<>();

        this.theme = theme;
        

        if(theme != null && theme.isValid()){
            directory.addAll(theme.getStaticPaths());
            for(Path themeTemplate: theme.getTemplatePaths()){
                if(themeTemplate==null)continue;
                if(this.templatesPaths.stream().noneMatch(templatePath->templatePath.getFileName()
                        .equals(themeTemplate.getFileName()))){
                    templatesPaths.add(themeTemplate);
                }
            }
        }

        directory = new ArrayList<Path>();
        directory.addAll(mdFilesPaths);
        directory.addAll(templatesPaths);
        directory.addAll(staticFilesPaths);
        try {
            directory.add(Paths.get(tomlOptions.getStringPath()));
        } catch (IOException e) {}
    }

    private  void setHier(@NonNull final Path targetBasePath) {
        try{
            Optional<File> hierOpt = Optional.empty();
            File file = targetBasePath.toFile();
            if(file.exists()){
                hierOpt = Arrays.stream(targetBasePath.toFile().listFiles()).filter(x->x.getName().equals("hierarchie.ser"))
                .findAny();
            }
            if(hierOpt.isPresent()){
                FileInputStream fichier = new FileInputStream(hierOpt.get());
                ObjectInputStream ois = new ObjectInputStream(fichier);
                hier = (Hierarchie) ois.readObject();
            }else{
                hier = new Hierarchie(directory);
                try{
                    hier.addFromCollection(tomlOptions.getStringPath(), inputFilesMdPaths);
                }catch(IOException e){}
            }
        }catch(IOException  e){
            logger.log(Level.WARNING,"IOException Hierarchie");
        }catch(ClassNotFoundException e){
            logger.log(Level.WARNING,"ClassNotFoundException Hierarchie");
        }
        System.out.println(hier);
    }

    private void update(@NonNull final Path targetBasePath) throws IOException {
        System.out.println(hashCode() + "     " + hier.hashCode());
        if(hashCode() == hier.hashCode()){
            return;
        }
        List<String> rebuild = new ArrayList<String>();
        for(Path p :  directory){
            System.out.println(hier.getHashCourant(p.toString()) + "    " + p.toString() + "    " + getHash(p));
            if(hier.getHashCourant(p.toString()) != getHash(p) || rebuild.contains(p.toString())){
                if(staticFilesPaths.contains(p)){
                    copyStaticFiles(targetBasePath, inputContentBasePath, true, p);
                }else if(inputFilesMdPaths.contains(p)){
                    convertMd2HtmlAndCopyHrefs(targetBasePath, p);
                }
                rebuild.addAll(hier.getDepCourant(p.toString()));
                hier.setHashCourant(p.toString(),getHash(p));
            }
        }
        hier.setGlobalHash(hashCode());
        saveHier(targetBasePath);
    }

    private void saveHier(@NonNull final Path targetBasePath){
        try {
            FileOutputStream out = new FileOutputStream(targetBasePath.resolve("hierarchie.ser").toString());
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(hier);
            oos.flush();
            oos.close();
        } catch (IOException e) {
        }
    }

    @Override
    public void save(@NonNull final Path targetBasePath,boolean all) throws IOException  {
        setHier(targetBasePath);
        if(all){
            save(targetBasePath);
        }else{
            update(targetBasePath);
        }
    }


    private void save(@NonNull final Path targetBasePath) throws IOException {
        File tmp = new File(targetBasePath.toString());
        if(!tmp.mkdirs()){
            logger.log(Level.INFO,"No dir was created");
        }

        //Copy static files
        for(Path staticFile : this.staticFilesPaths){
            copyStaticFiles(targetBasePath, inputContentBasePath, true, staticFile);
        }
        for(Path inputMdFile: inputFilesMdPaths){
            //Convert Md to Html then Copy hrefs
            convertMd2HtmlAndCopyHrefs(targetBasePath, inputMdFile);
        }

        if(theme != null && theme.isValid()){
            //If there is template, we copy its static files
            for(Path staticFile : theme.getStaticPaths()){
                copyStaticFiles(targetBasePath ,theme.getBasePath().resolve("static"), false, staticFile);
            }

        }
        hier.setGlobalHash(hashCode());
        saveHier(targetBasePath);

    }

    private void convertMd2HtmlAndCopyHrefs(@NonNull Path targetBasePath, Path inputMdFile) throws IOException {
            Path outputPath = callMd2Html(targetBasePath, inputMdFile);
            //Save hrefs if not present
            copyHrefsIfAbsent(targetBasePath, outputPath);
    }

    private Path callMd2Html(@NonNull Path targetBasePath, @NonNull Path inputMdFile) throws IOException {
        Path outputPath = extension2Html(targetBasePath.resolve(inputContentBasePath.relativize(inputMdFile)));
        Files.createDirectories(outputPath.getParent());

        ICMFile cmFile = CMFile.fromPath(inputMdFile);
        IConverterMd2Html converterMd2Html = new ConverterMd2Html(this.tomlOptions,templatesPaths,hier);
        /*if(theme != null && theme.isValid()){
            for(Path themeTemplate: theme.getTemplatePaths()){
                if(themeTemplate==null)continue;
                if(this.templatesPaths.stream().noneMatch(templatePath->templatePath.getFileName()
                        .equals(themeTemplate.getFileName()))){
                    templatesPaths.add(themeTemplate);
                }
            }
        }*/
        converterMd2Html.parseAndConvert2HtmlAndSave(cmFile, outputPath);
        return outputPath;
    }

    public static boolean isUrl(String url) {
        /* Try creating a valid URL */
        try {
            new URL(url).toURI();
            return true;
        }

        // If there was an Exception
        // while creating URL object
        catch (Exception e) {
            return false;
        }
    }

    private void copyHrefsIfAbsent(@NonNull Path targetBasePath, @NonNull Path html) throws IOException {
        List<String> hrefs = getHrefs(html);
        for(String href:hrefs) {
            if (!isUrl(href) && !Paths.get(href).isAbsolute()) {
                Path hrefShouldBe = Paths.get(targetBasePath.toString(), href);
                if (!Files.exists(hrefShouldBe)) {
                    //In this case we search it in templates folder
                    Path hrefRecuperationFrom = templatesPaths.stream()
                            .filter(x -> x.getFileName().toString().equals(hrefShouldBe.getFileName().toString()))
                            .findAny()
                            .orElse(null);
                    if (hrefRecuperationFrom != null) {
                        Files.createDirectories(hrefShouldBe.getParent());
                        Files.copy(hrefRecuperationFrom, hrefShouldBe, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }

    private void copyStaticFiles(@NonNull Path targetBasePath, @NonNull Path fromBasePath, boolean replaceIfExisting, Path staticPath) throws IOException {
            int hash = getHash(staticPath);
            Path output = targetBasePath.resolve(fromBasePath.relativize(staticPath));
            Files.createDirectories(output.getParent());
            if(replaceIfExisting){
                Files.copy(staticPath, output,StandardCopyOption.REPLACE_EXISTING);
            }else if(!Files.exists(output)){
                Files.copy(staticPath, output);
            }
            //set the new static file hash in hierarchie
            hier.setHashCourant(staticPath.toString(), hash);    
    }

    @Override
    public int hashCode(){
        int res = 0;
        for(Path e : this.directory){
            res += getHash(e);
        }
        return Integer.valueOf(res).hashCode();
    }

    private int getHash(Path input){
        String s = "";
        try{
            s = Files.readString(input);
        }catch(IOException e){

        }
        return s.hashCode();
    }

    private List<String> getHrefs(@NonNull Path htmlPath){
        if(!Files.exists(htmlPath) || !Files.isRegularFile(htmlPath))return Collections.emptyList();
        String htmlContent="";
        try {
            htmlContent = Files.readString(htmlPath);
        } catch (IOException e) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        Matcher matcher  = Pattern.compile("href[ ]*=[ ]*['\"](.*?)['\"]").matcher(htmlContent);
        while (matcher.find()){
            result.add(matcher.group(1));
        }
        return result;
    }

    private Path extension2Html(@NonNull Path pathMd){
        final String name = pathMd.getFileName().toString();
        return pathMd.resolveSibling(name.substring(0, name.lastIndexOf('.')) + ".html");
    }

}
