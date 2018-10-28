package nl.berg.squirrel_configure_xml.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory;
import nl.berg.squirrel_configure_xml.domain.predicate.NodeMetAttribuutWaardePredicate;
import nl.berg.squirrel_configure_xml.domain.predicate.NodeMetNaamPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class DocumentUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentUtil.class);

    public static final Predicate<Node> DRIVER_NODE = Predicates.and(new NodeMetNaamPredicate("Bean"),
            new NodeMetAttribuutWaardePredicate("Class", "net.sourceforge.squirrel_sql.fw.sql.SQLDriver"));
    public static final Predicate<Node> ALIAS_NODE = Predicates.and(new NodeMetNaamPredicate("Bean"),
            new NodeMetAttribuutWaardePredicate("Class", "net.sourceforge.squirrel_sql.client.gui.db.SQLAlias"));
    public static final Predicate<Node> IDENTIFIER_NODE = Predicates.and(new NodeMetNaamPredicate("identifier"),
            new NodeMetAttribuutWaardePredicate("Class", "net.sourceforge.squirrel_sql.fw.id.UidIdentifier"));
    public static final Predicate<Node> SCHEMA_PROPERTY_NODE = Predicates.and(new NodeMetNaamPredicate("Bean"),
            new NodeMetAttribuutWaardePredicate("Class", "net.sourceforge.squirrel_sql.client.gui.db.SQLAliasSchemaDetailProperties"));
    @SuppressWarnings("unchecked")
	public static final Predicate<Node> SCHEMA_DETAILS_NODE = Predicates.and();

    public static final Document parseDocument(File file) {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            LOGGER.error("Fout bij aanmaken parser voor file {}", file, e);
            throw new RuntimeException(e);
        }
        try {
            return docBuilder.parse(file);
        } catch (SAXException e) {
            LOGGER.error("Fout bij parsen {}", file, e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            LOGGER.error("Fout bij uitlezen {}", file, e);
            throw new RuntimeException(e);
        }
    }

    public static final void updateChildNode(Document document, Node parentElement, String childNodeName, String childNodeValue) {
        Preconditions.checkArgument(parentElement != null);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(childNodeName));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(childNodeValue));
        NodeList childNodes = parentElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNodeName.equals(childNode)) {
                // Oude waarde verwijderen
                clearChilds(childNode);
                // Nieuwe waarde setten
                childNode.appendChild(document.createTextNode(childNodeValue));
            }
        }
    }

    public static void clearChilds(Node parentElement) {
        while (parentElement.hasChildNodes())
            parentElement.removeChild(parentElement.getFirstChild());
    }

    public static List<Node> getChildrenByTagNaam(Node parentNode, String naam) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(naam), "Naam is nodig voor zoeken");
        NodeMetNaamPredicate nodeMetNaamPredicate = new NodeMetNaamPredicate(naam);

        List<Node> results = Lists.newArrayList();
        NodeList childNodes = parentNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (nodeMetNaamPredicate.apply(childNode)) {
                results.add(childNode);
            }
        }
        return results;
    }

    public static final void writeDocument(Document document, File file) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "4");
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            LOGGER.error("Fout bij aanmaker transformer {}", file, e);
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            LOGGER.error("Fout bij terugschrijven naar file {}", file, e);
            throw new RuntimeException(e);
        }
    }

    public static Element createStringElement(Document document, String name, String value) {
        Element node = document.createElement(name);
        node.appendChild(document.createTextNode(value));
        return node;
    }
}
