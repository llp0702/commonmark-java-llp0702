package upariscommonmarkjava.http_serv.implementations.server;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class UtilConstants {
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
    public static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

    public static final Path PATH_INTO_HOME_HTML = Paths.get("http_serv/src/main/resources/static/home.html");
    public static final Path PATH_INTO_HOME_JS = Paths.get("http_serv/src/main/resources/static/home.js");


    public static final Pattern API_GET_INPUT_FILES_PATHS_URL = Pattern.compile("/getInp");
    public static final Pattern API_GET_OUTPUT_FILES_PATHS_URL = Pattern.compile("/getOut");

    public static final String HEADER_GET_ANY_FILE = "PLEASE_GIVE_ME_A_FILE";
}
