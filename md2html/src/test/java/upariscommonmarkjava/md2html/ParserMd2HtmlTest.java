package upariscommonmarkjava.md2html;

import com.jcabi.w3c.ValidationResponse;
import com.jcabi.w3c.ValidatorBuilder;
import org.junit.jupiter.api.Test;
import upariscommonmarkjava.md2html.implementations.CMFile;
import upariscommonmarkjava.md2html.implementations.ParserMd2Html;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ParserMd2HtmlTest {
    @Test
    public void testParseCm() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream is = classLoader.getResourceAsStream("cmexample1.md");
        if(is==null) fail("cmexample1.md not found");
        CMFile cmFile = CMFile.fromString(new String(is.readAllBytes()));
        ParserMd2Html parser = ParserMd2Html.builder().build();

        String result = parser.parseAndRenderToString(cmFile);
        ValidationResponse response =
                new ValidatorBuilder().html().validate(result);
        assertTrue(response.valid());
    }
}
