package upariscommonmarkjava.buildsite;

import org.apache.commons.cli.*;

import java.io.IOException;
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
                String current_directory = System.getProperty("user.dir");
                DirectoryMd directoryMd;
                if(line.hasOption("i"))
                {
                    directoryMd = DirectoryMd.open(line.getOptionValue("i"));
                }
                else
                {
                    directoryMd = DirectoryMd.open(current_directory);
                }

                DirectoryHtml directoryHtml = directoryMd.generateHtml();

                if(line.hasOption("o")) {
                    directoryHtml.save(current_directory, line.getOptionValue("o"));
                }
                else
                    directoryHtml.save(current_directory);
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
                .desc("Les fichiers sont produits dans le r??pertoire _output/ par d??faut, ou dans le r??pertoire DIR ")
                .build() );
        options.addOption(Option.builder("i").longOpt("input-dir").numberOfArgs(1).argName("DIR")
                .desc("Les fichiers en entr??e sont r??cup??r??s dans le r??pertoire DIR/content. DIR vaut le r??pertoire " +
                        "courant par d??faut. La pr??sence des fichiers " +
                        "DIR/site.toml et DIR/content/index.md est n??cessaire.")
                .build());
        return options;
    }
}
