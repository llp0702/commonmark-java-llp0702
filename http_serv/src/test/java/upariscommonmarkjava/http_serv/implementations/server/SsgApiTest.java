package upariscommonmarkjava.http_serv.implementations.server;

import org.junit.jupiter.api.Test;
import upariscommonmarkjava.http_serv.implementations.dto.FileDetailsDTO;

import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SsgApiTest {
    final String directory = System.getProperty("user.dir") + "/src/test/resources/";
    @Test
    void getInputFiles(){
        SsgApi ssgApi = new SsgApi(Paths.get(directory,"dir_test"),Paths.get(directory,"dir_test"));
        List<FileDetailsDTO> result = ssgApi.getInputFiles();
        check(result);
    }

    @Test
    void getOutputFiles(){
        SsgApi ssgApi = new SsgApi(Paths.get(directory,"dir_test"),Paths.get(directory,"dir_test"));
        List<FileDetailsDTO> result = ssgApi.getOutputFiles();
        check(result);
    }

    private void check(List<FileDetailsDTO> result) {
        result.sort(Comparator.comparing(FileDetailsDTO::getName));
        assertEquals(2, result.size());
        assertEquals("file_test_1", result.get(0).getName());
        assertEquals("file_test_2", result.get(1).getName());
    }
}
