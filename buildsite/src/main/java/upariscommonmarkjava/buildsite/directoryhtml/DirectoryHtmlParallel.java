package upariscommonmarkjava.buildsite.directoryhtml;

import lombok.NonNull;
import upariscommonmarkjava.buildsite.parallel.IndependantQueue;
import upariscommonmarkjava.buildsite.parallel.Needs;
import upariscommonmarkjava.buildsite.parallel.NeedsException;
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

    private List<List<Path>> buildIndependantQueue() throws NeedsException {
        final HashMap<String, Needs<Path>> tableConstraints = new HashMap<>();

        for(final Path staticFile : this.staticFilesPaths){
            refreshHash(staticFile);
            tableConstraints.put(staticFile.toString(), new Needs<>(staticFile));
        }

        for(final Path inputMdFile: inputFilesMdPaths) {
            refreshHash(inputMdFile);
            tableConstraints.put(inputMdFile.toString(), new Needs<>(inputMdFile));
        }

        for(final Path inputMdFile: inputFilesMdPaths) {
            final Optional<Hierarchie> hier = convertMd2HtmlGethierarchy(inputMdFile);
            if(hier.isPresent())
            {
                for(final String dep : hier.get().getDepCourant(inputMdFile.toString())) {
                    tableConstraints.get(dep).addNeededValue(inputMdFile);
                }
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
        final List<Needs<Path>> constraints = new ArrayList<>();
        for(final String key : tableConstraints.keySet()) {
            constraints.add(tableConstraints.get(key));
        }
        return IndependantQueue.generate(constraints);
    }
    @Override
    protected void save(@NonNull final Path targetBasePath) throws IOException {
        createFolder(targetBasePath);
        try {
            final List<List<Path>> indQueue = buildIndependantQueue();
            for(final List<Path> list : indQueue){
                System.out.println(list);
            }
        }catch(NeedsException needsException){

        }

        saveGlobalHierarchie(targetBasePath);
    }

    public void setNbThread(final int nb_thread){
        this.nb_thread = nb_thread;
    }
}
