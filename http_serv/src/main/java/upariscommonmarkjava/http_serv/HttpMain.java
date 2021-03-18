package upariscommonmarkjava.http_serv;

import implementations.AppServer;
import org.apache.commons.cli.*;

import java.util.logging.Logger;

public class HttpMain {
    public static void help() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "ssg serve [Options]", ServeMainOptions());
    }

    public static Options ServeMainOptions(){
        Options options = new Options();
        options.addOption(Option.builder("h").longOpt("help").desc("Affiche ce message" ).build() );
        options.addOption( Option.builder("p").longOpt("port").numberOfArgs(1).argName("PORT")
                .desc("Le serveur se lance sur le port PORT ")
                .build() );

        return options;
    }

    public static void main(String[] args) {

        Logger logger = Logger.getAnonymousLogger();
        // create Options object
        Options options = ServeMainOptions();
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("help")) {
                help();
            } else {

                AppServer.main(args);
            }
        }
        catch(Exception exp) {
            // oops, something went wrong
            logger.warning( "Something went wrong :" + exp.getMessage() );
            logger.info("You should execute serve -help");
            logger.warning("stack trace: ");
            exp.printStackTrace();
        }
    }


}

