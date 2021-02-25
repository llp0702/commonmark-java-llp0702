package upariscommonmarkjava.md2html.implementations.extensions.toml;

import lombok.NoArgsConstructor;
import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.parser.block.BlockParserFactory;
import upariscommonmarkjava.md2html.implementations.extensions.yaml.YamlMeta;

@NoArgsConstructor
public class TomlMetaParser implements Parser.ParserExtension{
    @Override
    public void extend(Parser.Builder parserBuilder) {
        BlockParserFactory factory = new TomlBlockParser.Factory();
        parserBuilder.customBlockParserFactory(factory);
    }
    public static Extension create() {
        return new TomlMetaParser();
    }
}
