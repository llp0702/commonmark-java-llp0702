package upariscommonmarkjava.md2html.implementations.extensions.yaml;

import lombok.NoArgsConstructor;
import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.parser.block.BlockParserFactory;

@NoArgsConstructor
public class YamlMeta implements Parser.ParserExtension{
    @Override
    public void extend(Parser.Builder parserBuilder) {
        BlockParserFactory factory = new YamlMetaParser.Factory();
        parserBuilder.customBlockParserFactory(factory);
    }
    public static Extension create() {
        return new YamlMeta();
    }
}
