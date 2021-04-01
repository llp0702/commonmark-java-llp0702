package upariscommonmarkjava.md2html.implementations.extensions.toml;

import org.commonmark.internal.DocumentBlockParser;
import org.commonmark.node.Block;
import org.commonmark.parser.block.*;
import upariscommonmarkjava.md2html.interfaces.extensions.toml.ITomlBlockParser;

import java.util.regex.Pattern;

public class TomlBlockParser extends AbstractBlockParser implements ITomlBlockParser {
    private static final Pattern REGEX_BEGIN = Pattern.compile("^\\+{3}(\\s.*)?");
    private static final Pattern REGEX_END = Pattern.compile("^(\\+{3}|\\.{3})(\\s.*)?");

    private final StringBuilder currentValue;
    private final TomlBlock block;

    public TomlBlockParser() {
        currentValue = new StringBuilder();
        block = new TomlBlock();
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState parserState) {
        final CharSequence line = parserState.getLine().getContent();

        if (REGEX_END.matcher(line).matches()) {
            String currentValueString = currentValue.toString();
            if(!currentValueString.isBlank() && !currentValueString.isEmpty()){
                block.appendChild(new TomlNode(currentValueString));
            }
            return BlockContinue.finished();
        }
        currentValue.append("\n");
        currentValue.append(line);
        return BlockContinue.atIndex(parserState.getIndex());
    }

    public static class Factory extends AbstractBlockParserFactory {
        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            CharSequence line = state.getLine().getContent();
            BlockParser parentParser = matchedBlockParser.getMatchedBlockParser();
            // check whether this line is the first line of whole document or not
            if (parentParser instanceof DocumentBlockParser && parentParser.getBlock().getFirstChild() == null &&
                    REGEX_BEGIN.matcher(line).matches()) {
                return BlockStart.of(new TomlBlockParser()).atIndex(state.getNextNonSpaceIndex());
            }

            return BlockStart.none();
        }
    }
}
