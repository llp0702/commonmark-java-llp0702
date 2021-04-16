package upariscommonmarkjava.ascii2html;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.Document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.asciidoctor.OptionsBuilder.options;

public class Ascii2HtmlMain {

    private final Asciidoctor asciidoctor;

    public Ascii2HtmlMain() {
        asciidoctor = Asciidoctor.Factory.create();
    }

    public void convert(File file) {
        Map<String, Object> options = new AsciiOptions().get();
        asciidoctor.convertFile(file, options);
    }

    public void convert(File inputFile, Path outputPath) {
        Map<String, Object> options = new AsciiOptions().filePath(outputPath).get();
        asciidoctor.convertFile(inputFile, options);
    }

    public void convertAsFile(String content) {
        Map<String, Object> options = new AsciiOptions().get();
        asciidoctor.convert(content, options);
    }

    public String convertAsString(String content) {
        Map<String, Object> options = new AsciiOptions().saveToFile(false).get();
        String tmp = asciidoctor.convert(content, options);
        Document document = asciidoctor.load(content, new HashMap<>());
        return "<!DOCTYPE html><html lang=\"en\"><head><title>" + document.getDoctitle() + "</title></head><body>" + tmp + "</body></html>";
    }

    public void convertAs(String content, String fileName) throws IOException {
        String nameFile = fileName.split("\\.")[0];
        Instant timestamp = Instant.now();
        long currentTimeInMillis = timestamp.toEpochMilli();
        File tmpFile = new File(nameFile + currentTimeInMillis + ".adoc");
        BufferedWriter writer = new BufferedWriter(new FileWriter(nameFile + currentTimeInMillis + ".adoc"));
        writer.write(content);
        Map<String, Object> options = new AsciiOptions().fileName(fileName).get();
        asciidoctor.convertFile(tmpFile, options);
        tmpFile.deleteOnExit();
    }

    public void convertAs(File file, String fileName) {
        Map<String, Object> options = new AsciiOptions().fileName(fileName).get();
        asciidoctor.convertFile(file, options);
    }
}