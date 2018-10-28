package nl.berg.squirrel_configure_xml.domain.predicate;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
//import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class NodeMetAttribuutWaardePredicate implements Predicate<Node> {



    private String attribuutNaam;
    private String attribuutWaarde;

    public NodeMetAttribuutWaardePredicate(String attribuutNaam, String attribuutWaarde) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(attribuutNaam), "AttribuutNaam is verplicht");
        this.attribuutNaam = attribuutNaam;
        this.attribuutWaarde = attribuutWaarde;
    }

    @Override
    public boolean apply(Node element) {
        String attribuutWaardeUitXml = element.getAttributes().getNamedItem(attribuutNaam).getNodeValue();
        if(!Strings.isNullOrEmpty(attribuutWaarde)){
            return Objects.equal(attribuutWaarde, attribuutWaardeUitXml);
        } else {
            return true;
        }
    }
}
