package upariscommonmarkjava.http_serv.implementations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileDetails {
    private String absolutePath;
    private String name;
    private boolean isInputFile;
    private String absoluteBaseInputPath;
}
