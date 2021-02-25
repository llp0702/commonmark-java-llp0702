package upariscommonmarkjava.md2html.implementations.extensions.toml;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.commonmark.node.CustomNode;

@AllArgsConstructor
@Getter
@Setter
public class TomlNode extends CustomNode {
    private String rawContent;
}

