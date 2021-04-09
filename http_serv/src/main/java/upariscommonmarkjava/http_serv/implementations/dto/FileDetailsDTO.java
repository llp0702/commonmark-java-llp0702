package upariscommonmarkjava.http_serv.implementations.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileDetailsDTO {
    private String absolutePath;
    private String name;
    private boolean isInputFile;
    private String absoluteBaseInputPath;
    private String absoluteBaseOutputPath;
}
