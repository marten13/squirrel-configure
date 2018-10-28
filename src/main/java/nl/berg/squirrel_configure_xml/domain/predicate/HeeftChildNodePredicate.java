package nl.berg.squirrel_configure_xml.domain.predicate;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class HeeftChildNodePredicate implements Predicate<Node> {

    private Predicate<Node> childNodePredicate;

    public HeeftChildNodePredicate(Predicate<Node> childNodePredicate) {
        Preconditions.checkArgument(childNodePredicate != null, "childNodePredicate is verplicht");
        this.childNodePredicate = childNodePredicate;
    }

    @Override
    public boolean apply(Node element) {
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if(childNodePredicate.apply(childNode)){
                return true;
            }
        }
        return false;
    }
}
