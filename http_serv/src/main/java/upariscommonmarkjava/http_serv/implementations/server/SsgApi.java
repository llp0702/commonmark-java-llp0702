package upariscommonmarkjava.http_serv.implementations.server;

import lombok.Getter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SsgApi {
    @Getter
    private final  Path outputBasePath;
    @Getter
    private final Path inputBasePath;
    SsgApi(Path outputBasePath,Path inputBasePath) {
        this.inputBasePath = inputBasePath;
        this.outputBasePath = outputBasePath;
    }
    public List<Path> getInputFiles(){
        return new ArrayList<>();
    }
}
