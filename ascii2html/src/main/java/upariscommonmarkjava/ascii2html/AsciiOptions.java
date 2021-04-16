package upariscommonmarkjava.ascii2html;

import org.asciidoctor.SafeMode;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class AsciiOptions {

    private boolean saveToFile = true;
    private String fileName = "";
    private Path filePath = null;
    private boolean isSafe = false;
    private boolean withHeaderFooter = false;
    private String[] attributes = null;

    public AsciiOptions() {}

    public AsciiOptions saveToFile(boolean saveToFile) {
        this.saveToFile = saveToFile;
        return this;
    }

    public AsciiOptions fileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public AsciiOptions isSafe(boolean isSafe) {
        this.isSafe = isSafe;
        return this;
    }

    public AsciiOptions withHeaderFooter(boolean withHeaderFooter) {
        this.withHeaderFooter = withHeaderFooter;
        return this;
    }

    public AsciiOptions attributes(String[] attributes) {
        this.attributes = attributes;
        return this;
    }

    public AsciiOptions filePath(Path path) {
        this.filePath = path;
        return this;
    }

    public Map<String, Object> get() {
        Map<String, Object> optionsMap = new HashMap<String, Object>();
        optionsMap.put("backend", "html");

        if(!saveToFile)
            optionsMap.put("to_file", false);
        else if(fileName.equals(""))
            optionsMap.put("to_file", true);
        else if(filePath != null) {
            optionsMap.put("to_file", filePath);
        } else {
            File targetFile = new File(fileName);
            optionsMap.put("to_file", targetFile.getPath());
        }
        if(isSafe)
            optionsMap.put("isSafe", SafeMode.SAFE);
        else
            optionsMap.put("isSafe", SafeMode.UNSAFE);
        optionsMap.put("headerFooter", withHeaderFooter);
        if(attributes != null)
            optionsMap.put("attributes", attributes);
        return optionsMap;
    }
}
