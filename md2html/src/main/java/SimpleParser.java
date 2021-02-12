import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.FileNotFoundException;

public class SimpleParser {
    public static void main(String[] args) throws FileNotFoundException {
        HTML5Validator v = new HTML5Validator();
        v.validate();
        Parser parser = Parser.builder().build();
        Node document = parser.parse("This is *Sparta*");
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        renderer.render(document);  // "<p>This is <em>Sparta</em></p>\n"
        System.out.println("hello");
    }
}
