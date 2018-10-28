package nl.berg.squirrel_configure_xml.domain.predicate;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

public class HeeftNodeMetAttribuutPredicate implements Predicate<Node> {

    private NodeMetAttribuutWaardePredicate nodeMetAttribuutWaardePredicate;

    public HeeftNodeMetAttribuutPredicate(NodeMetAttribuutWaardePredicate nodeMetAttribuutWaardePredicate) {
        Preconditions.checkArgument(nodeMetAttribuutWaardePredicate != null, "nodeMetAttribuutWaardePredicate is verplicht");
        this.nodeMetAttribuutWaardePredicate = nodeMetAttribuutWaardePredicate;
    }

    @Override
    public boolean apply(Node input) {
        NodeList childNodes = input.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (nodeMetAttribuutWaardePredicate.apply(childNodes.item(i))) {
                return true;
            }
        }
        return false;
    }
}
