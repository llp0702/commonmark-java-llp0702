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
import java.util.*;

public class DirectoryHtmlParallel extends DirectoryHtml{

    private int nb_thread;

    public DirectoryHtmlParallel(@NonNull final Path inputContentBasePath, @NonNull final ITOMLFile tomlOptions,
                                 @NonNull final List<Path> mdFilesPaths, @NonNull final List<Path> staticFilesPaths,
                                 @NonNull final List<Path> templatesPaths, @NonNull final Optional<ITheme> theme){
        super(inputContentBasePath,tomlOptions,mdFilesPaths,staticFilesPaths,templatesPaths,theme);
    }

    private List<List<Path>> buildIndependantQueue(boolean rebuild) throws NeedsException {
        final HashMap<String, Needs<Path>> tableConstraints = new HashMap<>();

        for(final Path staticFile : this.staticFilesPaths){
            if(rebuild || hier.getHashCourant(staticFile.toString()) != getHash(staticFile)) {
                refreshHash(staticFile);
                tableConstraints.put(staticFile.toString(), new Needs<>(staticFile));
            }
        }

        for(final Path inputMdFile: inputFilesMdPaths) {
            if(rebuild || hier.getHashCourant(inputMdFile.toString()) != getHash(inputMdFile)) {
                refreshHash(inputMdFile);
                tableConstraints.put(inputMdFile.toString(), new Needs<>(inputMdFile));
            }
        }

        /*
        TODO
            applyToValid(optTheme, theme -> {
            try {
                for(final Path staticFile: theme.getStaticPaths()){
                    copyStaticFiles(targetBasePath ,theme.getBasePath().resolve("static"), false, staticFile);
                }
            } catch (IOException e) {
                logger.warning("Exception when trying to copy static files");
                e.printStackTrace();
            }});
        */

        for(final Path inputMdFile: inputFilesMdPaths) {
            final Optional<Hierarchie> hier = convertMd2HtmlGethierarchy(inputMdFile);
            if(hier.isPresent())
            {
                for(final String dep : hier.get().getDepCourant(inputMdFile.toString())) {
                    tableConstraints.get(dep).addNeededValue(inputMdFile);
                }
            }
        }

        final List<Needs<Path>> constraints = new ArrayList<>();
        for(final String key : tableConstraints.keySet()) {
            constraints.add(tableConstraints.get(key));
        }
        return IndependantQueue.generate(constraints);
    }

    @Override
    public void save(@NonNull final Path targetBasePath,boolean rebuild) throws IOException  {
        setHier(targetBasePath);
        if(!rebuild && hashCode() == hier.hashCode()){
            return;
        }

        createFolder(targetBasePath);

        try {
            final List<List<Path>> indQueue = buildIndependantQueue(rebuild);
            for(final List<Path> list : indQueue){
                new ObserverThread<>(nb_thread, new LinkedList<>(list), path -> this.compileFile(path,targetBasePath));
            }
        }catch(NeedsException needsException){

        }
    }

    public void setNbThread(final int nb_thread) {
        if(nb_thread > 0)
            this.nb_thread = nb_thread;
        else
            throw new Error("Nombre de thread invalide");
    }
}
