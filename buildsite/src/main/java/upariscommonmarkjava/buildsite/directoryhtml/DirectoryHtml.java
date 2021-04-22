package upariscommonmarkjava.buildsite.directoryhtml;

import lombok.Getter;
import lombok.NonNull;
import upariscommonmarkjava.ascii2html.Ascii2HtmlMain;
import upariscommonmarkjava.buildsite.theme.ITheme;
import upariscommonmarkjava.md2html.implementations.CMFile;
import upariscommonmarkjava.md2html.implementations.ConverterMd2Html;
import upariscommonmarkjava.md2html.implementations.incremental.Hierarchie;
import upariscommonmarkjava.md2html.interfaces.ICMFile;
import upariscommonmarkjava.md2html.interfaces.IConverterMd2Html;
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

    @Getter
    protected List<Path> asciiFilesPaths;

    protected ITOMLFile tomlOptions;
    protected List<Path> staticFilesPaths;
    protected List<Path> templatesPaths;
    protected List<Path> directory;
    protected Hierarchie hier;
    protected final Optional<ITheme> optTheme;
    protected final Path inputContentBasePath;

    public DirectoryHtml(@NonNull final Path inputContentBasePath, @NonNull final ITOMLFile tomlOptions,
                            @NonNull final List<Path> mdFilesPaths, @NonNull final List<Path> staticFilesPaths,
                            @NonNull final List<Path> asciiFilesPath, @NonNull final List<Path> templatesPaths,
                            @NonNull final Optional<ITheme> theme) {
        this.inputContentBasePath = inputContentBasePath;
        this.tomlOptions = tomlOptions;
        this.inputFilesMdPaths = mdFilesPaths;
        this.asciiFilesPaths = asciiFilesPath;
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
        directory.addAll(asciiFilesPath);
        try {
            directory.add(Paths.get(tomlOptions.getStringPath()));
        } catch (IOException ignored) {}
    }
    @Override
    public final int hashCode(){
        int res = 0;
        for(Path e : this.directory){
            res += getHash(e);
        }
        return Integer.valueOf(res).hashCode();
    }

    /** calcule le hash du fichier file et le sauvegarde dans la hierarchie */
    protected final void refreshHash(@NonNull final Path file){
        final int hash = getHash(file);
        hier.setHashCourant(file.toString(), hash);
    }

    /** calcule le hash du fichier input */
    protected final int getHash(final Path input){
        String content = "";
        try{
            content = Files.readString(input);
        }catch(IOException ignored){  }
        return content.hashCode();
    }

    protected final boolean newFileToHier(){
        return directory.size() != hier.nombrePath();
    }

    protected final void updateNewFilesToHier(){
        if(!newFileToHier()){
            return;
        }
        for(final Path p : directory){
            if(!hier.existPath(p.toString())){
                hier.addNewPath(p.toString());
            }
        }
    }
    /** Récupère la hierarchie si existante sinon l'initialise
     * @param targetBasePath répertoire ou se trouve la hierarchie
     */
    protected final void setHier(@NonNull final Path targetBasePath) {
        try{
            final File file = targetBasePath.toFile();
            Optional<File> hierOpt = Optional.empty();

            if(file.exists()){
                hierOpt = Arrays.stream(targetBasePath.toFile().listFiles())
                        .filter(x->x.getName().equals("hierarchie.ser"))
                        .findAny();
            }

            if(hierOpt.isPresent()){
                final FileInputStream fichier = new FileInputStream(hierOpt.get());
                final ObjectInputStream ois = new ObjectInputStream(fichier);
                hier = (Hierarchie) ois.readObject();
                updateNewFilesToHier();
            }else{
                hier = new Hierarchie(directory);
                hier.addFromCollection(tomlOptions.getStringPath(), inputFilesMdPaths);
            }
        }catch(IOException  e){
            logger.log(Level.WARNING,"IOException Hierarchie");
        }catch(ClassNotFoundException e){
            logger.log(Level.WARNING,"ClassNotFoundException Hierarchie");
        }
    }

    /** Sauvegarde la hierarchie dans le répertoire targetBasePath */
    protected final void saveHier(@NonNull final Path targetBasePath){
        try {
            FileOutputStream out = new FileOutputStream(targetBasePath.resolve("hierarchie.ser").toString());
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(hier);
            oos.flush();
            oos.close();
        } catch (IOException e) {
        }
    }

    /** Met à jour le hash et sauvegarde la hierarchie */
    protected final void saveGlobalHierarchie(@NonNull final Path targetBasePath){
        hier.setGlobalHash(hashCode());
        saveHier(targetBasePath);
    }

    /** Récupère la hierarchie après l'avoir construite et mise à jour (sans sauvegarder et construire le fichier md) */
    protected final Optional<Hierarchie> convertMd2HtmlGethierarchy(Path inputMdFile){
        try {
            final ConverterMd2Html converterMd2Html = new ConverterMd2Html(this.tomlOptions, templatesPaths, hier);
            final ICMFile cmFile = CMFile.fromPath(inputMdFile);
            return converterMd2Html.getActualHierarchie(cmFile);
        }
        catch(IOException ignore){}
        return Optional.empty();
    }

    /** Copy le fichier statique path d'un thème vers target */
    private void compileFileTheme(final Path target, final ITheme theme, final Path path) throws IOException{
        copyStaticFile(target, theme.getBasePath().resolve("static"), true, path);
    }

    /** Compile le fichier path en fonction de son type */
    protected void compileFile(@NonNull final Path path,@NonNull final Path targetBasePath){
        try {
            if (staticFilesPaths.contains(path)) {
                copyStaticFile(targetBasePath, inputContentBasePath, true, path);
            } else if(optTheme.isPresent() && optTheme.get().isValid() && optTheme.get().getStaticPaths().contains(path)) {
                compileFileTheme(targetBasePath, optTheme.get(), path);
            }
            else if (inputFilesMdPaths.contains(path)) {
                convertMd2HtmlAndCopyHrefs(targetBasePath, path);
            }else if(asciiFilesPaths.contains(path)) {
                convertAscii2HtmlAndCopyHrefs(targetBasePath,path);
            }
            refreshHash(path);
        } catch (IOException ignore) { }
    }

    /** Compile le site si des changement sont détectés */
    protected void update(@NonNull final Path targetBasePath) {
        if(hashCode() == hier.hashCode())
            return;
        for(final Path path :  directory){
            if(hier.getHashCourant(path.toString()) != getHash(path)){
                if(inputFilesMdPaths.contains(path) && convertMd2HtmlGethierarchy(path).isEmpty()){
                    logger.warning("Attention hierarchie incomplète");
                }
                compileFile(path,targetBasePath);
                final List<String> list =  hier.getDepCourant(path.toString());
                for(int i = 0; i < list.size(); i++){
                    final String dependance = list.get(i);
                    final Path dep = Path.of(dependance);
                    compileFile(dep,targetBasePath);
                }
            }
        }
    }

    @Override
    /** sauvegarde le projet en prenant en compte rebuildAll*/
    public void save(@NonNull final Path targetBasePath,boolean rebuildAll) throws IOException  {
        setHier(targetBasePath);
        if(rebuildAll)
            save(targetBasePath);
        else
            update(targetBasePath);
        saveGlobalHierarchie(targetBasePath);
    }

    /** créé un directory et ses parents en cas d'absence */
    protected final void createFolder(@NonNull final Path targetBasePath){
        final File tmp = new File(targetBasePath.toString());
        if(!tmp.mkdirs()){
            logger.log(Level.INFO,"No dir was created");
        }
    }

    /** recompile tous les fichiers du site et les sauvegardes */
    protected void save(@NonNull final Path targetBasePath) throws IOException {

        createFolder(targetBasePath);

        for(final Path staticFile : this.staticFilesPaths){
            copyStaticFile(targetBasePath, inputContentBasePath, true, staticFile);
        }
        for(final Path inputMdFile: inputFilesMdPaths){
            //Convert Md to Html then Copy hrefs
            convertMd2HtmlAndCopyHrefs(targetBasePath, inputMdFile);
        }
        for(Path inputAsciiFile: asciiFilesPaths) {
            convertAscii2HtmlAndCopyHrefs(targetBasePath,inputAsciiFile);
        }
        applyToValid(optTheme, theme -> {
            try {
                for(final Path staticFile: theme.getStaticPaths()){
                    compileFileTheme(targetBasePath,theme, staticFile);
                }
            } catch (IOException e) {
                logger.warning("Exception when trying to copy static files");
                e.printStackTrace();
            }
        });
    }

    /** applique une fonction si le theme est présent et valide */
    protected final void applyToValid(final Optional<ITheme> optTheme, final Consumer<ITheme> fun) {
        optTheme.ifPresent(theme -> {
            if (theme.isValid())
                fun.accept(theme);
        });
    }

    /** sauvegarde un fichier markdown en html et copier ses liens */
    protected void convertMd2HtmlAndCopyHrefs(@NonNull final Path targetBasePath,final Path inputMdFile) throws IOException {
            final Path outputPath = callMd2Html(targetBasePath, inputMdFile);
            //Save hrefs if not present
            copyHrefsIfAbsent(targetBasePath, outputPath);
            refreshHash(inputMdFile);
    }

    /** appel la traduction grace à MD2HTML */
    private Path callMd2Html(@NonNull final Path targetBasePath, @NonNull final Path inputMdFile) throws IOException {
        final Path outputPath = extension2Html(targetBasePath.resolve(inputContentBasePath.relativize(inputMdFile)));
        Files.createDirectories(outputPath.getParent());

        final ICMFile cmFile = CMFile.fromPath(inputMdFile);
        final IConverterMd2Html converterMd2Html = new ConverterMd2Html(this.tomlOptions,templatesPaths,hier);

        converterMd2Html.parseAndConvert2HtmlAndSave(cmFile, outputPath);
        return outputPath;
    }

    /** sauvegarde les fichiers ascii en html et copier leurs liens */
    private void convertAscii2HtmlAndCopyHrefs(@NonNull Path targetBasePath,@NonNull Path inputAsciiFile) throws IOException {
        final Path outputPath = callAscii2Html(targetBasePath, inputAsciiFile);
        copyHrefsIfAbsent(targetBasePath, outputPath);
        refreshHash(inputAsciiFile);
    }

    /** appel la traduction grace à Ascii2ToHtml */
    private Path callAscii2Html(@NonNull Path targetBasePath, @NonNull Path inputAsciiFile) throws IOException {
        final Path outputPath = extension2Html(targetBasePath.resolve(inputContentBasePath.relativize(inputAsciiFile)));
        Files.createDirectories(outputPath.getParent());
        final Ascii2HtmlMain converter = new Ascii2HtmlMain();
        converter.convert(inputAsciiFile.toFile(), targetBasePath);
        return outputPath;
    }

    /** vérifie que un String est un url valide */
    private static boolean isUrl(final String url) {
        try {
            new URL(url).toURI();
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    /** Récupère tous les Hrefs du fichier html et les copies dans le répertoire si ils sont absents */
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

    /** copie le fichier statique */
    protected final void copyStaticFile(@NonNull final Path targetBasePath, @NonNull final Path fromBasePath, boolean replaceIfExisting, @NonNull final Path staticPath) throws IOException {
            final Path output = targetBasePath.resolve(fromBasePath.relativize(staticPath));
            Files.createDirectories(output.getParent());
            if(replaceIfExisting){
                Files.copy(staticPath, output,StandardCopyOption.REPLACE_EXISTING);
            }else if(!Files.exists(output)){
                Files.copy(staticPath, output);
            }
            refreshHash(staticPath);
    }

    /** récupère les hrefs du fichier html */
    private List<String> getHrefs(@NonNull final Path htmlPath){
        if(!Files.exists(htmlPath) || !Files.isRegularFile(htmlPath))
            return Collections.emptyList();

        try {
            final String htmlContent = Files.readString(htmlPath);
            final List<String> result = new ArrayList<>();
            final Matcher matcher = Pattern.compile("href[ ]*=[ ]*['\"](.*?)['\"]").matcher(htmlContent);
            while (matcher.find())
                result.add(matcher.group(1));

            return result;

        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    /** change l'extension d'un nom de fichier */
    private Path extension2Html(@NonNull final Path pathMd){
        final String name = pathMd.getFileName().toString();
        return pathMd.resolveSibling(name.substring(0, name.lastIndexOf('.')) + ".html");
    }

}
