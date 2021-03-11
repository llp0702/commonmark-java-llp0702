package implementations;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.SimpleWebServer;
import fi.iki.elonen.WebServerPluginInfo;
import fi.iki.elonen.util.ServerRunner;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class App2 extends SimpleWebServer {


    public App2(String host, int port, File wwwroot, boolean quiet) {
        super(host, port, wwwroot, quiet);
    }
    public static void run(String[] args) {
        // Defaults
        int port = 8080;

        String host = null; // bind to all interfaces by default
        List<File> rootDirs = new ArrayList<File>();
        boolean quiet = false;
        String cors = null;
        Map<String, String> options = new HashMap<String, String>();

        // Parse command-line, with short and long versions of the options.
        for (int i = 0; i < args.length; ++i) {
            if ("-h".equalsIgnoreCase(args[i]) || "--host".equalsIgnoreCase(args[i])) {
                host = args[i + 1];
            } else if ("-p".equalsIgnoreCase(args[i]) || "--port".equalsIgnoreCase(args[i])) {
                port = Integer.parseInt(args[i + 1]);
            } else if ("-q".equalsIgnoreCase(args[i]) || "--quiet".equalsIgnoreCase(args[i])) {
                quiet = true;
            } else if ("-d".equalsIgnoreCase(args[i]) || "--dir".equalsIgnoreCase(args[i])) {
                rootDirs.add(new File(args[i + 1]).getAbsoluteFile());
            } else if (args[i].startsWith("--cors")) {
                cors = "*";
                int equalIdx = args[i].indexOf('=');
                if (equalIdx > 0) {
                    cors = args[i].substring(equalIdx + 1);
                }
            } else if ("--licence".equalsIgnoreCase(args[i])) {
                System.out.println(  "\n");
            } else if (args[i].startsWith("-X:")) {
                int dot = args[i].indexOf('=');
                if (dot > 0) {
                    String name = args[i].substring(0, dot);
                    String value = args[i].substring(dot + 1, args[i].length());
                    options.put(name, value);
                }
            }
        }

        if (rootDirs.isEmpty()) {
            rootDirs.add(new File(".").getAbsoluteFile());
        }
        options.put("host", host);
        options.put("port", "" + port);
        options.put("quiet", String.valueOf(quiet));
        StringBuilder sb = new StringBuilder();
        for (File dir : rootDirs) {
            if (sb.length() > 0) {
                sb.append(":");
            }
            try {
                sb.append(dir.getCanonicalPath());
            } catch (IOException ignored) {
            }
        }
        options.put("home", sb.toString());
        ServiceLoader<WebServerPluginInfo> serviceLoader = ServiceLoader.load(WebServerPluginInfo.class);
        for (WebServerPluginInfo info : serviceLoader) {
            String[] mimeTypes = info.getMimeTypes();
            for (String mime : mimeTypes) {
                String[] indexFiles = info.getIndexFilesForMimeType(mime);
                if (!quiet) {
                    System.out.print("# Found plugin for Mime type: \"" + mime + "\"");
                    if (indexFiles != null) {
                        System.out.print(" (serving index files: ");
                        for (String indexFile : indexFiles) {
                            System.out.print(indexFile + " ");
                        }
                    }
                    System.out.println(").");
                }
                registerPluginForMimeType(indexFiles, mime, info.getWebServerPlugin(mime), options);
            }
        }
        MyServerRunner.executeInstance(new SimpleWebServer(host, port, rootDirs, quiet, cors));
    }



}
