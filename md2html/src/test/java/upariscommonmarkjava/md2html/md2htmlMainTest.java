package upariscommonmarkjava.md2html;

import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class md2htmlMainTest {
    @Test
    void md2htmlMainOptionsTest(){
        final Options opt = Md2HtmlMain.md2htmlMainOptions();
        Assertions.assertNotNull(opt.getOption("h"));
        Assertions.assertNotNull(opt.getOption("o"));

        Assertions.assertNotNull(opt.getOption("help"));
        Assertions.assertNotNull(opt.getOption("output-dir"));

        Assertions.assertNull(opt.getOption("hzzhz"));
        Assertions.assertNull(opt.getOption("m"));
    }
}
