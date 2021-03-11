package upariscommonmarkjava.http_serv;

import fi.iki.elonen.SimpleWebServer;
import implementations.App2;

public class HttpMain {
    public static void main(String[] args) {
        App2.run(args);
        //SimpleWebServer.main(args);
    }
}
