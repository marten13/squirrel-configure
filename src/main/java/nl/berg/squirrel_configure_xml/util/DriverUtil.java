package nl.berg.squirrel_configure_xml.util;

import static nl.berg.squirrel_configure_xml.util.DocumentUtil.DRIVER_NODE;
import static nl.berg.squirrel_configure_xml.util.DocumentUtil.IDENTIFIER_NODE;
import static nl.berg.squirrel_configure_xml.util.DocumentUtil.createStringElement;
import static nl.berg.squirrel_configure_xml.util.DocumentUtil.getChildrenByTagNaam;

import java.util.List;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import nl.berg.squirrel_configure_xml.domain.DriverConfig;
import nl.berg.squirrel_configure_xml.domain.predicate.HeeftChildNodeMetNaamEnWaardePredicate;
import nl.berg.squirrel_configure_xml.domain.predicate.HeeftChildNodePredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class DriverUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriverUtil.class);

    private DriverUtil() {
    }

    public static Element findDriver(Document driverDocument, String driver) {
        // Driver vinden
        Element driverElement = null;
        if (!Strings.isNullOrEmpty(driver)) {
            driverElement = DriverUtil.findDriverBijNaam(driverDocument, driver);
            if (driverElement == null) {
                driverElement = DriverUtil.findDriverBijUUID(driverDocument, driver);
            }
        }
        if (driverElement == null) {
            throw new IllegalArgumentException("Geen driver gevonden bij UUID of naam: " + driver);
        } else {
            return driverElement;
        }
    }

    public static Element findDriverBijUUID(Document document, String driverUUID) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(driverUUID), "DriverUUID naam is verplicht");
        Iterable<Element> allDrivers = getAllDrivers(document);

        Predicate<Node> childPredicate = Predicates.and(IDENTIFIER_NODE,
                new HeeftChildNodeMetNaamEnWaardePredicate("string", driverUUID));
        return Iterables.getOnlyElement(Iterables.filter(allDrivers, new HeeftChildNodePredicate(childPredicate)), null);
    }

    public static Element findDriverBijNaam(Document document, String driverName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(driverName), "Driver naam is verplicht");
        Iterable<Element> allDrivers = getAllDrivers(document);
        return Iterables.getOnlyElement(Iterables.filter(allDrivers,
                new HeeftChildNodeMetNaamEnWaardePredicate("name", driverName)
        ), null);
    }

    public static List<String> getDriverNames(Document document) {
        Iterable<Element> allDrivers = getAllDrivers(document);
        List<String> driverNames = Lists.newArrayList();

        for (Element driverElement : allDrivers) {
            NodeList driverNameNodes = driverElement.getElementsByTagName("name");
            Preconditions.checkArgument(driverNameNodes.getLength() == 1, "Driver hoort maar 1 naam te hebben");
            driverNames.add(driverNameNodes.item(0).getTextContent());
        }
        return driverNames;
    }

    public static String getDriverUUID(Element driverElement) {
        Node identifier = Iterables.getOnlyElement(getChildrenByTagNaam(driverElement, "identifier"));
        Node driverUUIDNode = Iterables.getOnlyElement(getChildrenByTagNaam(identifier, "string"));
        return driverUUIDNode.getTextContent();
    }

    public static String getDriverJdbcUrl(Element driverElement) {
        Node url = Iterables.getOnlyElement(getChildrenByTagNaam(driverElement, "url"));
        return url.getTextContent();
    }

    public static String addNewDriver(Document driverDocument, DriverConfig driverConfig) {
        Preconditions.checkArgument(driverDocument != null, "Document is nodig");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(driverConfig.getDriverName()), "Naam voor Driver is nodig");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(driverConfig.getJarPath()), "Pad naar driver is nodig");
        String driverId = UUID.randomUUID().toString();

        Element driver = driverDocument.createElement("Bean");
        driver.setAttribute("Class", "net.sourceforge.squirrel_sql.fw.sql.SQLDriver");

        // Classname
        driver.appendChild(createStringElement(driverDocument, "driverClassName", "com.ibm.as400.access.AS400JDBCDriver"));

        // Identifier
        Element identifier = driverDocument.createElement("identifier");
        identifier.setAttribute("Class", "net.sourceforge.squirrel_sql.fw.id.UidIdentifier");
        identifier.appendChild(createStringElement(driverDocument, "string", driverId));
        driver.appendChild(identifier);

        // JarFileName
        driver.appendChild(createStringElement(driverDocument, "jarFileName", ""));

        // JarFileNames
        Element jarFileNames = driverDocument.createElement("jarFileNames");
        jarFileNames.setAttribute("Indexed", "true");

        Element jarFile = driverDocument.createElement("Bean");
        jarFile.setAttribute("Class", "net.sourceforge.squirrel_sql.fw.util.beanwrapper.StringWrapper");
        jarFile.appendChild(createStringElement(driverDocument, "string", driverConfig.getJarPath()));
        jarFileNames.appendChild(jarFile);
        driver.appendChild(jarFileNames);

        // Name
        driver.appendChild(createStringElement(driverDocument, "name", driverConfig.getDriverName()));

        // Default jdbc url
        String jdbcUrl = Strings.isNullOrEmpty(driverConfig.getDefaultJdbcUrl()) ? "jdbc:as400://172.30.253.31" : driverConfig.getDefaultJdbcUrl();
        driver.appendChild(createStringElement(driverDocument, "url", jdbcUrl));

        // WebsiteUrl
        driver.appendChild(createStringElement(driverDocument, "websiteUrl", ""));

        driverDocument.appendChild(driver);

        LOGGER.info("Driver aangemaakt met jarPath: {}, driverName: {}, jdbcUrl: {} en UUID: {}",
                driverConfig.getJarPath(), driverConfig.getDriverName(), jdbcUrl, driverId);

        return driverId;
    }

    public static Iterable<Element> getAllDrivers(Document document) {
        NodeList beans = document.getElementsByTagName("Bean");
        List<Element> driverList = Lists.newArrayList();
        for (int i = 0; i < beans.getLength(); i++) {
            Node bean = beans.item(i);
            if (DRIVER_NODE.apply(bean)) {
                driverList.add((Element) bean);
            }
        }
        return driverList;
    }
}
