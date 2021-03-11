package implementations;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.util.ServerRunner;

import java.io.IOException;

public class MyServerRunner extends ServerRunner {

    public static void executeInstance(NanoHTTPD server) {
        try {
            server.start(5000, false);
        } catch (IOException var3) {
            System.exit(-1);
        }

        System.out.println("Server started, Hit Enter to stop.\n");
        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");

        try {
            //Code à mettre ici pour incremental
            System.in.read();
        } catch (Throwable var2) {
        }

        server.stop();
        System.out.println("Server stopped.\n");
    }
}
