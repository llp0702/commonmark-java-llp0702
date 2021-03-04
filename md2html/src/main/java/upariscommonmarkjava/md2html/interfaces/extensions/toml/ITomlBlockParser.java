package upariscommonmarkjava.md2html.interfaces.extensions.toml;

import org.commonmark.node.Block;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.BlockParser;
import org.commonmark.parser.block.ParserState;

public interface ITomlBlockParser extends BlockParser {
    @Override
    Block getBlock();

    @Override
    BlockContinue tryContinue(ParserState parserState);
}
