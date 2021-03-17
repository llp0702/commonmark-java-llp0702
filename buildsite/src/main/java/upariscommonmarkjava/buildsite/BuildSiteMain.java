package upariscommonmarkjava.buildsite;

import org.apache.commons.cli.*;
import upariscommonmarkjava.buildsite.directoryhtml.IDirectoryHtml;
import upariscommonmarkjava.buildsite.directorymd.DirectoryMd;
import upariscommonmarkjava.buildsite.directorymd.IDirectoryMd;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class BuildSiteMain {
    public static void main(String[] args) {
        Logger logger = Logger.getAnonymousLogger();
        // create Options object
        Options options = buildsiteMainOptions();
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine line = parser.parse( options, args );
            if( line.hasOption("h")){
                help();
            }else{
                String currentDirectory = System.getProperty("user.dir");
                IDirectoryMd directoryMd;
                if(line.hasOption("i"))
                {
                    directoryMd = DirectoryMd.open(line.getOptionValue("i"));
                }
                else
                {
                    directoryMd = DirectoryMd.open(currentDirectory);
                }

                IDirectoryHtml directoryHtml = directoryMd.generateHtml();

                if(line.hasOption("o")) {
                    directoryHtml.save(Paths.get(line.getOptionValue("o")));
                }
                else{
                    directoryHtml.save(Paths.get(currentDirectory,"_output"));
                }
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
        return options;
    }
}
