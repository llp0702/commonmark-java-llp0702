package upariscommonmarkjava.http_serv;

import org.apache.commons.cli.*;
import upariscommonmarkjava.http_serv.implementations.server.SSGServer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class HttpMain {
    public static void help() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "ssg serve [Options]", serveMainOptions());
    }

    public static Options serveMainOptions(){
        Options options = new Options();
        options.addOption(Option.builder("h").longOpt("help").desc("Affiche ce message" ).build() );
        options.addOption( Option.builder("p").longOpt("port").numberOfArgs(1).argName("PORT")
                .desc("Le serveur se lance sur le port PORT ")
                .build() );
        return options;
    }

    public static void main(String[] args) {
        main(args, serveMainOptions());
    }

    public static void main(String[] args, Options options){
        Logger logger = Logger.getAnonymousLogger();
        // create Options object
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args, false);
            if (line.hasOption("help")) {
                help();
            } else {
                final String current_directory = System.getProperty("user.dir");
                Path output = line.hasOption("o") ?
                        Paths.get(line.getOptionValue("o")):
                        Paths.get(current_directory,"_output");
                Path input = Paths.get(line.hasOption("i") ?
                        line.getOptionValue("i"):
                        current_directory);
                int port = line.hasOption("p") ?
                        Integer.parseInt(line.getOptionValue("p")) :
                        SSGServer.DEF_PORT;
                work(input, output, port);
            }
        }catch(Exception exp) {
            // oops, something went wrong
            logger.warning( "Something went wrong :" + exp.getMessage() );
            logger.info("You should execute serve -help");
            exp.printStackTrace();
        }
    }

    private static void work(final Path input, final Path output, int port) throws InterruptedException {

        SSGServer ssgServer = new SSGServer(port, input, output);
        ssgServer.run();
    }


}

