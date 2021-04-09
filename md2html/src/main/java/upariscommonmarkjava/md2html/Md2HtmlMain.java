package upariscommonmarkjava.md2html;

import org.apache.commons.cli.*;
import upariscommonmarkjava.md2html.implementations.CMFile;
import upariscommonmarkjava.md2html.implementations.ConverterMd2Html;
import upariscommonmarkjava.md2html.interfaces.ICMFile;
import upariscommonmarkjava.md2html.interfaces.IConverterMd2Html;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;


public class Md2HtmlMain {

    public static final String OUTPUT_DIR = "_output/";

    public static void main(String[] args) {
        Logger logger = Logger.getAnonymousLogger();
        // create Options object
        Options options = md2htmlMainOptions();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse( options, args, false );
            if( line.hasOption("h")){
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "ssg build [filename] [Options]", options );
            }else{
                if(line.getArgs().length==0){
                    throw new ParseException("Unspecified input file");
                }
                String fileInputPath = line.getArgs()[0];

                String outputDir = OUTPUT_DIR;
                if( line.hasOption( "o" ) ) {
                    outputDir = line.getOptionValue("o");
                    if(!outputDir.endsWith("/"))outputDir += "/";
                }

                workMd2Html(fileInputPath, outputDir);

                //Cas ou on utilise filename et non pas buildsite
            }
        }
        catch(ParseException | IOException exp ) {
            // oops, something went wrong
            logger.warning( "Something went wrong :" + exp.getMessage() );
            logger.info("You should execute ssg build -h");
            logger.warning("stack trace: ");
            exp.printStackTrace();
        }
    }

    public static Options md2htmlMainOptions(){
        Options options = new Options();
        options.addOption(Option.builder("h").longOpt("help").desc("Affiche ce message" ).build() );
        options.addOption( Option.builder("o").longOpt("output-dir").numberOfArgs(1).argName("DIR")
                .desc("Les fichiers sont produits dans le répertoire _output/ par défaut, ou dans le répertoire DIR ")
                .build() );
        return options;
    }

    public static void workMd2Html(final String filePath, final String outputDir) throws IOException {
        Path inputPath = Paths.get(filePath);
        String filenameInput = inputPath.getFileName().toString();
        String filenameOutput = filenameInput.substring(0, filenameInput.lastIndexOf('.'))+".html";
        Path outputPath = Paths.get(outputDir+filenameOutput);
        ICMFile cmFile = CMFile.fromPath(inputPath);
        IConverterMd2Html converterMd2Html = new ConverterMd2Html();
        converterMd2Html.parseAndConvert2HtmlAndSave(cmFile,outputPath);
    }
}
