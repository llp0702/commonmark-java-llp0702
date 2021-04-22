package upariscommonmarkjava.buildsite.directoryhtml;

import lombok.NonNull;
import upariscommonmarkjava.buildsite.parallel.IndependantQueue;
import upariscommonmarkjava.buildsite.parallel.Needs;
import upariscommonmarkjava.buildsite.parallel.NeedsException;
import upariscommonmarkjava.buildsite.parallel.ObserverThread;
import upariscommonmarkjava.buildsite.theme.ITheme;
import upariscommonmarkjava.md2html.implementations.incremental.Hierarchie;
import upariscommonmarkjava.md2html.interfaces.ITOMLFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class DirectoryHtmlParallel extends DirectoryHtml{

    //nombre de thread utilisable
    private int nbThread;

    public DirectoryHtmlParallel(@NonNull final Path inputContentBasePath, @NonNull final ITOMLFile tomlOptions,
                                 @NonNull final List<Path> mdFilesPaths, @NonNull final List<Path> staticFilesPaths,
                                 @NonNull final List<Path> asciiFilesPath,
                                 @NonNull final List<Path> templatesPaths, @NonNull final Optional<ITheme> theme){
        super(inputContentBasePath,tomlOptions,mdFilesPaths, staticFilesPaths,asciiFilesPath, templatesPaths,theme);
    }

    private void addDependance(Map<String,Needs<Path>> tableConstraints, Path file){
        for(final String dep : hier.getDepCourant(file.toString())){
            tableConstraints.put(dep, new Needs<>(Paths.get(dep)));
            for(final String need : hier.getNeeds(dep)){
                tableConstraints.get(dep).addNeededValue(Paths.get(need));
            }
        }
    }

    /** Construit la liste des fichiers qui peuvent être compilé en meme temps en utilisant leur hierarchie */
    private List<List<Path>> buildIndependantQueue(boolean rebuild) throws NeedsException {
        final HashMap<String, Needs<Path>> tableConstraints = new HashMap<>();

        for(final Path staticFile : this.staticFilesPaths){
            if(rebuild || hier.getHashCourant(staticFile.toString()) != getHash(staticFile)) {
                refreshHash(staticFile);
                tableConstraints.put(staticFile.toString(), new Needs<>(staticFile));
                addDependance(tableConstraints, staticFile);
            }
        }

        for(final Path ascPath : this.asciiFilesPaths){
            if(rebuild || hier.getHashCourant(ascPath.toString()) != getHash(ascPath)) {
                refreshHash(ascPath);
                tableConstraints.put(ascPath.toString(), new Needs<>(ascPath));
                addDependance(tableConstraints, ascPath);
            }
        }

        for(final Path inputMdFile: inputFilesMdPaths) {
            if(rebuild || hier.getHashCourant(inputMdFile.toString()) != getHash(inputMdFile)) {
                convertMd2HtmlGethierarchy(inputMdFile);
                refreshHash(inputMdFile);
                tableConstraints.put(inputMdFile.toString(), new Needs<>(inputMdFile));
                addDependance(tableConstraints, inputMdFile);
            }
        }

        applyToValid(optTheme, theme -> {
            for (final Path themeFile : theme.getStaticPaths()) {
                if (rebuild || hier.getHashCourant(themeFile.toString()) != getHash(themeFile)) {
                    refreshHash(themeFile);
                    tableConstraints.put(themeFile.toString(), new Needs<>(themeFile));
                    addDependance(tableConstraints, themeFile);
                }
            }
        });
        try{
            final Path toml = Paths.get(tomlOptions.getStringPath());
            if(rebuild || hier.getHashCourant(toml.toString()) != getHash(toml)) {
                refreshHash(toml);
                tableConstraints.put(toml.toString(), new Needs<>(toml));
                addDependance(tableConstraints, toml);
            }
        }catch(IOException ignored){}


        final List<Needs<Path>> constraints = new ArrayList<>();
        for(final String key : tableConstraints.keySet()) {
            constraints.add(tableConstraints.get(key));
        }
        return IndependantQueue.generate(constraints);
    }

    @Override
    /** sauvegarde le projet de manière parallel */
    public void save(@NonNull final Path targetBasePath,boolean rebuild) {
        setHier(targetBasePath);
        if(!rebuild && hashCode() == hier.hashCode()){
            return;
        }

        createFolder(targetBasePath);

        try {
            final List<List<Path>> indQueue = buildIndependantQueue(rebuild);
            for(final List<Path> list : indQueue){
                new ObserverThread<>(nbThread, new LinkedList<>(list), path -> this.compileFile(path,targetBasePath));
            }
        }catch(NeedsException needsException){

        }
        saveGlobalHierarchie(targetBasePath);
    }

    public final void setNbThread(final int nb_thread) {
        if(nb_thread > 0)
            this.nbThread = nb_thread;
        else
            throw new Error("Nombre de thread invalide");
    }
}
