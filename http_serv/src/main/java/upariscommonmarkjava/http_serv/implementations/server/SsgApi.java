package upariscommonmarkjava.http_serv.implementations.server;

import lombok.Getter;
import lombok.NonNull;
import upariscommonmarkjava.buildsite.BuildSiteMain;
import upariscommonmarkjava.buildsite.SiteFormatException;
import upariscommonmarkjava.http_serv.implementations.dto.FileDetailsDTO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SsgApi {
    @Getter
    private final  Path outputBasePath;
    @Getter
    private final Path inputBasePath;

    SsgApi(Path inputBasePath, Path outputBasePath) {
        this.inputBasePath = inputBasePath;
        this.outputBasePath = outputBasePath;
    }
    public List<FileDetailsDTO> getInputFiles(){
        return this.getFilesArbo(inputBasePath).stream().peek(i-> i.setInputFile(true))
                .collect(Collectors.toList());
    }

    private List<FileDetailsDTO> getFilesArbo(Path from){
        final List<FileDetailsDTO> result = new ArrayList<>();
        try(Stream<Path> inputSubFilesStream = Files.list(from)){
            inputSubFilesStream.forEach(inputFilePathCur->{
                if(Files.isDirectory(inputFilePathCur)){
                    result.addAll(getFilesArbo(inputFilePathCur));
                }else if(Files.isRegularFile(inputFilePathCur)){
                    result.add(FileDetailsDTO.builder()
                            .absolutePath(inputFilePathCur.toAbsolutePath().toString())
                            .name(inputFilePathCur.getFileName().toString())
                            .absoluteBaseInputPath(inputBasePath.toAbsolutePath().toString())
                            .absoluteBaseOutputPath(outputBasePath.toAbsolutePath().toString())
                            .build());
                }
            });
            return result;
        }catch(IOException e){
            return new ArrayList<>();
        }
    }
    public List<FileDetailsDTO> getOutputFiles(){
        return getFilesArbo(outputBasePath).stream().peek(o-> o.setInputFile(false)).collect(Collectors.toList());
    }

    public void updateInputHandler() throws IOException, SiteFormatException {
        BuildSiteMain.buildSite(inputBasePath, outputBasePath, false);
    }
    public void updateFile(@NonNull final Path pathToUpdate, String content) throws IOException {
        if(content==null)content ="";
        Files.writeString(pathToUpdate, content, StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.CREATE);
    }
}
