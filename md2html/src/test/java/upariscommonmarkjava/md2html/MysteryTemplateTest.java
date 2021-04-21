package upariscommonmarkjava.md2html;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MysteryTemplateTest extends HtmlTemplateTest{

    @Test
    void mysteryTemplateTest(){
        assertEquals("_list_files_1",getResult("testListFiles"));
        final String[] result = getResult("testListFilesFor").split("//");

        System.out.println(result);
        assertTrue(result[0].contains("test2::")
                && result[0].contains("fil1::")
                && result[0].contains("file3::")
                && result[1].contains("test")
                && result[1].contains("fil1")
                && result[1].contains("test2")
                && result[1].contains("test3")
                && result[1].contains("test4")
                && result[1].contains("file3"));

    }
}
