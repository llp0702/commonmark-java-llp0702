package upariscommonmarkjava.md2html;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MysteryTemplateTest extends HtmlTemplateTest{

    @Test
    void mysteryTemplateTest(){
        assertEquals("_list_files_1",getResult("testListFiles"));
        assertEquals("test2::fil1::file3:://test/fil1::test2/test2::test3/test4/file3::",getResult("testListFilesFor"));
    }
}
