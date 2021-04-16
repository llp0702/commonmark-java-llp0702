package upariscommonmarkjava;

import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import upariscommonmarkjava.buildsite.BuildSiteMain;

public class buildSiteMainTest {
    @Test
    void buildsiteMainOptionsTest(){
        final Options opt = BuildSiteMain.buildsiteMainOptions();
        Assertions.assertNotNull(opt.getOption("h"));
        Assertions.assertNotNull(opt.getOption("o"));
        Assertions.assertNotNull(opt.getOption("i"));
        Assertions.assertNotNull(opt.getOption("r"));
        Assertions.assertNotNull(opt.getOption("j"));

        Assertions.assertNotNull(opt.getOption("help"));
        Assertions.assertNotNull(opt.getOption("output-dir"));
        Assertions.assertNotNull(opt.getOption("input-dir"));
        Assertions.assertNotNull(opt.getOption("rebuild-all"));
        Assertions.assertNotNull(opt.getOption("jobs"));


        Assertions.assertNull(opt.getOption("zdzdzbs"));
        Assertions.assertNull(opt.getOption("u"));
    }
}
