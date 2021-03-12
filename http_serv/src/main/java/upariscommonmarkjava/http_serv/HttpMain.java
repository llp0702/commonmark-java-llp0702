package upariscommonmarkjava.http_serv;

import fi.iki.elonen.SimpleWebServer;
import implementations.App2;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class HttpMain {
    public static void help() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "ssg serve [Options]", ServeMainOptions());
    }

    public static Options ServeMainOptions(){
        Options options = new Options();
        options.addOption(Option.builder("help").desc("Affiche ce message" ).build() );
        options.addOption( Option.builder("d").longOpt("dir").numberOfArgs(1).argName("DIR")
                .desc("Le serveur se lance dans le repertoire DIR ")
                .build() );
        options.addOption( Option.builder("p").longOpt("port").numberOfArgs(1).argName("PORT")
                .desc("Le serveur se lance sur le port PORT ")
                .build() );
        options.addOption(Option.builder("q").longOpt("quiet")
                .desc("Lance le serveur sans les logs")
                .build());
        return options;
    }

    public static void main(String[] args) {
      /*  boolean help =false;
        for(String s : args){
            if(s.equals("--help")) {
                help =true;
                help();

            }
        }
        if(!help)*/
       // App2.run(args);
        //help();

        //SimpleWebServer.main(args);
        Logger logger = Logger.getAnonymousLogger();
        // create Options object
        Options options = ServeMainOptions();
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("help")) {
                help();
            } else {
                App2.run(args);
            }
        }
        catch(Exception exp) {
            // oops, something went wrong
            logger.warning( "Something went wrong :" + exp.getMessage() );
            logger.info("You should execute serve --help");
            logger.warning("stack trace: ");
            exp.printStackTrace();
        }
    }


}

