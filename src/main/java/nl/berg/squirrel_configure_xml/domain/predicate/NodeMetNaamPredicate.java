package nl.berg.squirrel_configure_xml.domain.predicate;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import org.w3c.dom.Node;

public class NodeMetNaamPredicate implements Predicate<Node>{

    private String nodeNaam;

    public NodeMetNaamPredicate(String nodeNaam) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(nodeNaam), "NodeNaam is verplicht");
        this.nodeNaam = nodeNaam;
    }

    @Override
    public boolean apply(Node input) {
        return nodeNaam.equals(input.getNodeName());
    }
}
