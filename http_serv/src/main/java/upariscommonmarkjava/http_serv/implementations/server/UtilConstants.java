package upariscommonmarkjava.http_serv.implementations.server;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public final class UtilConstants {
    private UtilConstants(){}

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    public static final Path PATH_INTO_HOME_HTML = Paths.get("http_serv/src/main/resources/static/home.html");
    public static final Path PATH_INTO_HOME_JS = Paths.get("http_serv/src/main/resources/static/home.js");


    public static final Pattern API_GET_INPUT_FILES_PATHS_URL = Pattern.compile("/getInp");
    public static final Pattern API_GET_OUTPUT_FILES_PATHS_URL = Pattern.compile("/getOut");
    public static final Pattern API_GET_UPDATE_SITE = Pattern.compile("/reloadsite");

    public static final Pattern API_POST_UPDATE_FILE = Pattern.compile("/updateFile");


    public static final String HEADER_GET_ANY_FILE = "PLEASE_GIVE_ME_A_FILE";
    public static final String HEADER_FILE_PATH = "FILE_PATH";
}
