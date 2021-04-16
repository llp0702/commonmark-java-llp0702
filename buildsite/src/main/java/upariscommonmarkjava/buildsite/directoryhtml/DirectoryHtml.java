package upariscommonmarkjava.buildsite.directoryhtml;

import lombok.Getter;
import lombok.NonNull;
import org.eclipse.jetty.util.IO;
import upariscommonmarkjava.buildsite.theme.ITheme;
import upariscommonmarkjava.md2html.implementations.CMFile;
import upariscommonmarkjava.md2html.implementations.ConverterMd2Html;
import upariscommonmarkjava.md2html.implementations.incremental.Hierarchie;
import upariscommonmarkjava.md2html.interfaces.ICMFile;
import upariscommonmarkjava.md2html.interfaces.IConverterMd2Html;
import upariscommonmarkjava.md2html.interfaces.IFile;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;

import java.io.File;
import java.io.FileInputStream;
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
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirectoryHtml implements IDirectoryHtml {
    public static final Logger logger = Logger.getLogger("Directory html logger");

    @Getter
    protected final List<Path> inputFilesMdPaths;


    protected ITOMLFile tomlOptions;
    protected List<Path> staticFilesPaths;
    protected List<Path> templatesPaths;
    protected List<Path> directory;
    protected Hierarchie hier;
    protected final Optional<ITheme> optTheme;
    protected final Path inputContentBasePath;

    public DirectoryHtml(@NonNull final Path inputContentBasePath, @NonNull final ITOMLFile tomlOptions,
                            @NonNull final List<Path> mdFilesPaths, @NonNull final List<Path> staticFilesPaths,
                            @NonNull final List<Path> templatesPaths, @NonNull final Optional<ITheme> theme) {
        this.inputContentBasePath = inputContentBasePath;
        this.tomlOptions = tomlOptions;
        this.inputFilesMdPaths = mdFilesPaths;
        this.staticFilesPaths = staticFilesPaths;
        this.templatesPaths = templatesPaths;
        this.optTheme = theme;

        applyToValid(optTheme, currentTheme -> {
            for (Path themeTemplate : currentTheme.getTemplatePaths()) {
                if (this.templatesPaths.stream().noneMatch(templatePath -> templatePath.getFileName()
                        .equals(themeTemplate.getFileName()))) {
                    templatesPaths.add(themeTemplate);
                }
            }
        });

        directory = new ArrayList<Path>();
        directory.addAll(mdFilesPaths);
        directory.addAll(templatesPaths);
        directory.addAll(staticFilesPaths);
        try {
            directory.add(Paths.get(tomlOptions.getStringPath()));
        } catch (IOException ignored) {}
    }

    protected void setHier(@NonNull final Path targetBasePath) {
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
                }catch(IOException ignore){}
            }
        }catch(IOException  e){
            logger.log(Level.WARNING,"IOException Hierarchie");
        }catch(ClassNotFoundException e){
            logger.log(Level.WARNING,"ClassNotFoundException Hierarchie");
        }
    }

    protected void update(@NonNull final Path targetBasePath) throws IOException {
        if(hashCode() == hier.hashCode()){
            return;
        }
        for(final Path path :  directory){
            if(hier.getHashCourant(path.toString()) != getHash(path)){
                compileFile(path,targetBasePath);
                final List<String> list =  hier.getDepCourant(path.toString());

                for(int i = 0; i < list.size(); i++){
                    final String dependance = list.get(i);
                    final Path dep = Path.of(dependance);
                    compileFile(dep,targetBasePath);
                    hier.setHashCourant(dependance,getHash(dep));
                }
                hier.setHashCourant(path.toString(),getHash(path));
            }
        }
        saveGlobalHierarchie(targetBasePath);
    }

    protected void compileFile(@NonNull final Path path,@NonNull final Path targetBasePath){
        try {
            if (staticFilesPaths.contains(path)) {
                copyStaticFiles(targetBasePath, inputContentBasePath, true, path);
            } else if (inputFilesMdPaths.contains(path)) {
                convertMd2HtmlAndCopyHrefs(targetBasePath, path);
            }
        } catch (IOException ignore) {

        }
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

    protected void saveGlobalHierarchie(@NonNull final Path targetBasePath){
        hier.setGlobalHash(hashCode());
        saveHier(targetBasePath);
    }

    protected void createFolder(@NonNull final Path targetBasePath){
        final File tmp = new File(targetBasePath.toString());
        if(!tmp.mkdirs()){
            logger.log(Level.INFO,"No dir was created");
        }
    }

    protected void save(@NonNull final Path targetBasePath) throws IOException {
        createFolder(targetBasePath);

        for(final Path staticFile : this.staticFilesPaths){
            copyStaticFiles(targetBasePath, inputContentBasePath, true, staticFile);
        }
        for(final Path inputMdFile: inputFilesMdPaths){
            //Convert Md to Html then Copy hrefs
            convertMd2HtmlAndCopyHrefs(targetBasePath, inputMdFile);
        }
        applyToValid(optTheme, theme -> {
            try {
                for(final Path staticFile: theme.getStaticPaths()){
                    copyStaticFiles(targetBasePath ,theme.getBasePath().resolve("static"), false, staticFile);
                }
            } catch (IOException e) {
                logger.warning("Exception when trying to copy static files");
                e.printStackTrace();
            }
        });

        saveGlobalHierarchie(targetBasePath);
    }

    protected void applyToValid(final Optional<ITheme> optTheme, final Consumer<ITheme> fun) {
        optTheme.ifPresent(theme -> {
            if (theme.isValid())
                fun.accept(theme);
        });
    }

    protected Optional<Hierarchie> convertMd2HtmlGethierarchy(Path inputMdFile){
        try {
            final ConverterMd2Html converterMd2Html = new ConverterMd2Html(this.tomlOptions, templatesPaths, hier);
            final ICMFile cmFile = CMFile.fromPath(inputMdFile);
            return converterMd2Html.getActualHierarchie(cmFile);
        }
        catch(IOException ignore){}
        return Optional.empty();
    }

    protected void convertMd2HtmlAndCopyHrefs(@NonNull Path targetBasePath, Path inputMdFile) throws IOException {
            Path outputPath = callMd2Html(targetBasePath, inputMdFile);
            //Save hrefs if not present
            copyHrefsIfAbsent(targetBasePath, outputPath);
    }

    private Path callMd2Html(@NonNull final Path targetBasePath, @NonNull final Path inputMdFile) throws IOException {
        final Path outputPath = extension2Html(targetBasePath.resolve(inputContentBasePath.relativize(inputMdFile)));
        Files.createDirectories(outputPath.getParent());

        final ICMFile cmFile = CMFile.fromPath(inputMdFile);
        final IConverterMd2Html converterMd2Html = new ConverterMd2Html(this.tomlOptions,templatesPaths,hier);

        converterMd2Html.parseAndConvert2HtmlAndSave(cmFile, outputPath);
        return outputPath;
    }

    public static boolean isUrl(final String url) {
        try {
            new URL(url).toURI();
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    private void copyHrefsIfAbsent(@NonNull final Path targetBasePath, @NonNull final Path html) throws IOException {
        final List<String> hrefs = getHrefs(html);
        for(String href : hrefs) {
            if (!isUrl(href) && !Paths.get(href).isAbsolute()) {
                final Path hrefShouldBe = Paths.get(targetBasePath.toString(), href);
                if (!Files.exists(hrefShouldBe)) {
                    //In this case we search it in templates folder
                    final Optional<Path> hrefRecuperationFrom = templatesPaths.stream()
                            .filter(x -> x.getFileName().toString().equals(hrefShouldBe.getFileName().toString()))
                            .findAny();

                    if (hrefRecuperationFrom.isPresent()) {
                        Files.createDirectories(hrefShouldBe.getParent());
                        Files.copy(hrefRecuperationFrom.get(), hrefShouldBe, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }

    protected void refreshHash(@NonNull final Path staticPath){
        final int hash = getHash(staticPath);
        hier.setHashCourant(staticPath.toString(), hash);
    }

    protected void copyStaticFiles(@NonNull Path targetBasePath, @NonNull Path fromBasePath, boolean replaceIfExisting,@NonNull Path staticPath) throws IOException {
            Path output = targetBasePath.resolve(fromBasePath.relativize(staticPath));
            Files.createDirectories(output.getParent());
            if(replaceIfExisting){
                Files.copy(staticPath, output,StandardCopyOption.REPLACE_EXISTING);
            }else if(!Files.exists(output)){
                Files.copy(staticPath, output);
            }
            //set the new static file hash in hierarchie
            refreshHash(staticPath);
    }

    @Override
    public int hashCode(){
        int res = 0;
        for(Path e : this.directory){
            res += getHash(e);
        }
        return Integer.valueOf(res).hashCode();
    }

    protected int getHash(Path input){
        String s = "";
        try{
            s = Files.readString(input);
        }catch(IOException e){

        }
        return s.hashCode();
    }

    private List<String> getHrefs(@NonNull final Path htmlPath){
        if(!Files.exists(htmlPath) || !Files.isRegularFile(htmlPath))
            return Collections.emptyList();

        try {
            final String htmlContent = Files.readString(htmlPath);
            final List<String> result = new ArrayList<>();
            final Matcher matcher = Pattern.compile("href[ ]*=[ ]*['\"](.*?)['\"]").matcher(htmlContent);
            while (matcher.find()){
                result.add(matcher.group(1));
            }
            return result;

        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private Path extension2Html(@NonNull final Path pathMd){
        final String name = pathMd.getFileName().toString();
        return pathMd.resolveSibling(name.substring(0, name.lastIndexOf('.')) + ".html");
    }

}
