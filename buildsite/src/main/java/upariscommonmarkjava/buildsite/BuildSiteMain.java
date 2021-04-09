package upariscommonmarkjava.buildsite;

import org.apache.commons.cli.*;
import upariscommonmarkjava.buildsite.directoryhtml.IDirectoryHtml;
import upariscommonmarkjava.buildsite.directorymd.DirectoryMd;
import upariscommonmarkjava.buildsite.directorymd.IDirectoryMd;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class BuildSiteMain {
    public static void main(String[] args) {
        main(args,  buildsiteMainOptions());
    }
    public static void main(String[] args, Options options){
        Logger logger = Logger.getAnonymousLogger();
        CommandLineParser parser = new DefaultParser();
        String currentDirectory = System.getProperty("user.dir");
        try {
            CommandLine line = parser.parse( options, args, false);
            if( line.hasOption("h")){
                help();
            }else{
                buildSite(
                        Paths.get(line.hasOption("i") ?
                                line.getOptionValue("i"):
                                currentDirectory),
                        line.hasOption("o")?
                                Paths.get(line.getOptionValue("o")):
                                Paths.get(currentDirectory, "_output"),
                        line.hasOption("r")
                        );

            }
        }
        catch(ParseException | SiteFormatException | IOException exp ) {
            // oops, something went wrong
            logger.warning( "Something went wrong :" + exp.getMessage() );
            logger.info("You should execute ssg build -h");
            logger.warning("stack trace: ");
            exp.printStackTrace();
        }
    }

    public static void buildSite(final Path inputDir, final Path outputDir, final boolean isRebuildAll) throws IOException, SiteFormatException {
        DirectoryMd.open(inputDir)
                .generateHtml()
                .save(outputDir, isRebuildAll);
    }

    public static void help() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "ssg build [Options]", buildsiteMainOptions());
    }

    public static Options buildsiteMainOptions(){
        Options options = new Options();
        options.addOption(Option.builder("h").longOpt("help").desc("Affiche ce message" ).build() );
        options.addOption( Option.builder("o").longOpt("output-dir").numberOfArgs(1).argName("DIR")
                .desc("Les fichiers sont produits dans le répertoire _output/ par défaut, ou dans le répertoire DIR ")
                .build() );
        options.addOption(Option.builder("i").longOpt("input-dir").numberOfArgs(1).argName("DIR")
                .desc("Les fichiers en entrée sont récupérés dans le répertoire DIR/content. DIR vaut le répertoire " +
                        "courant par défaut. La présence des fichiers " +
                        "DIR/site.toml et DIR/content/index.md est nécessaire.")
                .build());
        options.addOption(Option.builder("r").longOpt("rebuild-all").numberOfArgs(0)
                .desc("Recompile le projet dans sa globalité")
                .build());
        return options;
    }
}
