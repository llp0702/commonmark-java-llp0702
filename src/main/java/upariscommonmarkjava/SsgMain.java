package upariscommonmarkjava;

import org.apache.commons.cli.*;
import upariscommonmarkjava.md2html.Md2HtmlMain;
import upariscommonmarkjava.md2html.implementations.CMFile;
import upariscommonmarkjava.md2html.implementations.ConverterMd2Html;
import upariscommonmarkjava.md2html.interfaces.ICMFile;
import upariscommonmarkjava.md2html.interfaces.IConverterMd2Html;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class SsgMain {
    public final static List<String> commands = List.of("build");

    public static void main(String[] args) {
        Logger logger = Logger.getAnonymousLogger();
        // create Options object
        Options options = ssgOptions();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse( options, args );
            if(line.hasOption("h")){
                if(line.getArgs().length>0 && commands.contains(line.getArgs()[0])){
                    work(line.getArgs()[0], options, Arrays.copyOfRange(args, 1, args.length));
                }else{
                    help(options);
                }
            }else{
                if(line.getArgs().length==0){
                    throw new ParseException("Unspecified ssg command");
                }
                String command=line.getArgs()[0];
                String[] argsWithoutCommand = new String[args.length-1];
                int j=0;
                for (int i = 0; i < args.length; i++) {
                    if(!args[i].equals(command)){
                        argsWithoutCommand[j++]=args[i];
                    }
                }
                work(command, options, argsWithoutCommand);
            }
        }
        catch(ParseException exp ) {
            // oops, something went wrong
            logger.warning( "Something went wrong :" + exp.getMessage() );
            logger.info("You should execute ssg -h");
        }
    }

    private static void help(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "ssg <command> [args] [Options]", options);
    }


    public static Options ssgOptions(){
        Options options = new Options();
        options.addOption(Option.builder("h").longOpt("help").desc("Affiche ce message" ).build() );
        return options;
    }

    public static void work(String command, Options options, String[] args){
        if(!commands.contains(command)){
            help(options);
        }
        if(command.equals("build")){
            Md2HtmlMain.main(args);
        }

    }

}
