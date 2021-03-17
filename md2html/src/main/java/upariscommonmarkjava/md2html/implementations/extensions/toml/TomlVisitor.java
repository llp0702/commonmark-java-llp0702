package upariscommonmarkjava.md2html.implementations.extensions.toml;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.CustomNode;
import org.tomlj.Toml;
import org.tomlj.TomlTable;

import java.util.ArrayList;
import java.util.List;

public class TomlVisitor extends AbstractVisitor {
    private List<TomlTable> data=new ArrayList<>();

    @Override
    public void visit(CustomNode customNode) {
        if (customNode instanceof TomlNode) {
            data.add(Toml.parse(((TomlNode)customNode).getRawContent()));
        } else {
            super.visit(customNode);
        }
    }

    public List<TomlTable> getData() {
        return data;
    }
}
