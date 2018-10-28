package nl.berg.squirrel_configure_xml.domain.predicate;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class HeeftChildNodeMetNaamEnWaardePredicate implements Predicate<Node> {

    private String nodeNaam;
    private String nodeInhoud;

    public HeeftChildNodeMetNaamEnWaardePredicate(String nodeNaam, String nodeInhoud) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(nodeNaam), "ChildNaam is verplicht");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(nodeInhoud), "ChildWaarde is verplicht");
        this.nodeNaam = nodeNaam;
        this.nodeInhoud = nodeInhoud;
    }

    @Override
    public boolean apply(Node element) {
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if(nodeNaam.equals(childNode.getNodeName()) && nodeInhoud.equals(childNode.getTextContent())){
                return true;
            }
        }
        return false;
    }
}