package upariscommonmarkjava;

import org.apache.commons.cli.*;
import upariscommonmarkjava.buildsite.BuildSiteMain;
import upariscommonmarkjava.http_serv.HttpMain;
import upariscommonmarkjava.md2html.Md2HtmlMain;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class SsgMain {
    public static final String BUILD = "build";
    public static final String SERVE = "serve";
    public static final List<String> commands = List.of(BUILD, "help",SERVE);
    static Logger logger;
    public static void main(String[] args) {
        logger = Logger.getAnonymousLogger();
        // create Options object
        Options options = ssgOptions();
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine line = parser.parse( options, args, true );
            if(line.hasOption("h")){
                if(line.getArgs().length>0 && BUILD.equals(line.getArgs()[0])){
                    BuildSiteMain.help();
                }else{
                    help(options);
                }
            }else{
                if(line.getArgs().length==0){
                    throw new ParseException("Unspecified ssg command");
                }
                String[] argsWithoutCommand = getArgsWithoutCommand(args, line);
                work(line.getArgs()[0], options, argsWithoutCommand);
            }
        }
        catch(ParseException exp ) {
            // oops, something went wrong
            logger.warning( "Something went wrong :" + exp.getMessage() );
            logger.info("You should execute ssg -h");
        }
    }

    private static String[] getArgsWithoutCommand(String[] args, CommandLine line) {
        String command= line.getArgs()[0];
        String[] argsWithoutCommand = new String[args.length-1];
        int j=0;
        for (String arg : args) {
            if (arg!=null && !arg.equals(command)) {
                argsWithoutCommand[j++] = arg;
            }
        }
        return argsWithoutCommand;
    }

    private static void help(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "ssg <build | help> [args] [Options]\nbuild\tfor converting md file or website (use ssg build -h for more info)\nhelp\tShows this message\n", options);
    }


    public static Options ssgOptions(){
        Options options = new Options();
        options.addOption(Option.builder("h").longOpt("help").desc("Affiche ce message" ).build() );
        return options;
    }

    public static void work(String command, Options options, String[] args){
        if((command==null) || (!commands.contains(command)) || (command.equals("help")) ){
            help(options);
            return;
        }

        if(command.equals(BUILD)){
            if(Arrays.stream(args).anyMatch(x -> x.endsWith(".md"))){
                Md2HtmlMain.main(args);
            }else if(command.equals(SERVE)){
                HttpMain.main(args);
            }
            else{
                BuildSiteMain.main(args);
            }
        }

    }

}
