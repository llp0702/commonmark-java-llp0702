package upariscommonmarkjava.md2html.implementations.extensions.toml;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.CustomNode;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

public class TomlVisitor extends AbstractVisitor {
    private TomlParseResult data;

    @Override
    public void visit(CustomNode customNode) {
        if (customNode instanceof TomlNode) {
            data = Toml.parse(((TomlNode)customNode).getRawContent());
        } else {
            super.visit(customNode);
        }
    }

    public TomlParseResult getData() {
        return data;
    }
}
