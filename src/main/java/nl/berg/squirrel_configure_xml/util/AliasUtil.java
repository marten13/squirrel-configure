package nl.berg.squirrel_configure_xml.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import nl.berg.squirrel_configure_xml.domain.AliasConfig;
import nl.berg.squirrel_configure_xml.domain.LoadType;
import nl.berg.squirrel_configure_xml.domain.SchemaLoadConfig;
import nl.berg.squirrel_configure_xml.domain.predicate.HeeftChildNodeMetNaamEnWaardePredicate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;
import java.util.UUID;

import static nl.berg.squirrel_configure_xml.util.DocumentUtil.*;

public final class AliasUtil {

    private AliasUtil() {
    }

    public static Element findAliasByName(Document document, String aliasNaam) {
        Iterable<Element> allAliases = getAllAliases(document);
        return Iterables.getOnlyElement(Iterables.filter(allAliases,
                new HeeftChildNodeMetNaamEnWaardePredicate("name", aliasNaam)
        ), null);
    }

    public static void addOrUpdateAlias(Document aliasDocument, Document aliasTreeDocument, AliasConfig aliasConfig, String driverUUID,
            String defaultJdbcUrl) {
        Preconditions.checkArgument(aliasDocument != null, "Document is nodig");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(driverUUID), "DriverUUID is nodig");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(aliasConfig.getUserName()), "UserName is nodig");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(aliasConfig.getPassword()), "Wachtwoord is nodig");

        Element aliasByName = findAliasByName(aliasDocument, aliasConfig.getAliasName());
        if (aliasByName != null) {
            updateAlias(aliasDocument, aliasByName, aliasConfig, driverUUID);
        } else {
            Element aliasElement = createAliasElement(aliasDocument, aliasConfig, driverUUID, defaultJdbcUrl);
            NodeList beansNodesAlias = aliasDocument.getElementsByTagName("Beans");
            Preconditions.checkState(beansNodesAlias.getLength() == 1);
            beansNodesAlias.item(0).appendChild(aliasElement);

            NodeList beansNodesAliasTree = aliasTreeDocument.getElementsByTagName("Beans");
            Preconditions.checkState(beansNodesAliasTree.getLength() == 1);
            beansNodesAliasTree.item(0).appendChild(createTreeElement(aliasTreeDocument, aliasElement));
        }
    }

    public static Element createTreeElement(Document aliasTreeDocument, Element alias){
        Preconditions.checkArgument(aliasTreeDocument != null);
        Preconditions.checkArgument(alias != null);
        String aliasUUID = getAliasUUID(alias);

        Element bean = aliasTreeDocument.createElement("Bean");
        bean.setAttribute("Class", "net.sourceforge.squirrel_sql.client.gui.db.AliasFolderState");

        Element driverIdentifier = aliasTreeDocument.createElement("aliasIdentifier");
        driverIdentifier.setAttribute("Class", "net.sourceforge.squirrel_sql.fw.id.UidIdentifier");
        driverIdentifier.appendChild(createStringElement(aliasTreeDocument, "string", aliasUUID));
        bean.appendChild(driverIdentifier);

        bean.appendChild(createStringElement(aliasTreeDocument, "expanded", "false"));
        bean.appendChild(createStringElement(aliasTreeDocument, "folderName", ""));

        Element kidsNode = aliasTreeDocument.createElement("kids");
        kidsNode.setAttribute("Indexed", "true");
        bean.appendChild(kidsNode);

        bean.appendChild(createStringElement(aliasTreeDocument, "selected", ""));

        return bean;
    }

    public static String getAliasUUID(Element driverElement) {
        Node identifier = Iterables.getOnlyElement(getChildrenByTagNaam(driverElement, "identifier"));
        Node aliasUUIDNode = Iterables.getOnlyElement(getChildrenByTagNaam(identifier, "string"));
        return aliasUUIDNode.getTextContent();
    }

    public static Element createAliasElement(Document aliasDocument, AliasConfig aliasConfig, String driverUUID, String defaultJdbcUrl) {
        Element alias = aliasDocument.createElement("Bean");
        alias.setAttribute("Class", "net.sourceforge.squirrel_sql.client.gui.db.SQLAlias");

        // Autologon
        alias.appendChild(createStringElement(aliasDocument, "autoLogon", "false"));

        // Color
        Element colorProperties = aliasDocument.createElement("colorProperties");
        colorProperties.setAttribute("Class", "net.sourceforge.squirrel_sql.client.gui.db.SQLAliasColorProperties");
        colorProperties.appendChild(createStringElement(aliasDocument, "objectTreeBackgroundColorRgbValue", "0"));
        colorProperties.appendChild(createStringElement(aliasDocument, "overrideObjectTreeBackgroundColor", "false"));
        colorProperties.appendChild(createStringElement(aliasDocument, "overrideStatusBarBackgroundColor", "false"));
        colorProperties.appendChild(createStringElement(aliasDocument, "overrideToolbarBackgroundColor", "false"));
        colorProperties.appendChild(createStringElement(aliasDocument, "statusBarBackgroundColorRgbValue", "0"));
        colorProperties.appendChild(createStringElement(aliasDocument, "toolbarBackgroundColorRgbValue", "0"));
        alias.appendChild(colorProperties);

        // connectAtStartup
        alias.appendChild(createStringElement(aliasDocument, "connectAtStartup", "false"));

        // Connection properties
        Element connectionProperties = aliasDocument.createElement("connectionProperties");
        connectionProperties.setAttribute("Class", "net.sourceforge.squirrel_sql.client.gui.db.SQLAliasConnectionProperties");
        connectionProperties.appendChild(createStringElement(aliasDocument, "enableConnectionKeepAlive", "false"));
        connectionProperties.appendChild(createStringElement(aliasDocument, "keepAliveSleepTimeSeconds", "120"));
        connectionProperties.appendChild(createStringElement(aliasDocument, "keepAliveSqlStatement", ""));
        alias.appendChild(connectionProperties);

        // driverIdentifier
        Element driverIdentifier = aliasDocument.createElement("driverIdentifier");
        driverIdentifier.setAttribute("Class", "net.sourceforge.squirrel_sql.fw.id.UidIdentifier");
        driverIdentifier.appendChild(createStringElement(aliasDocument, "string", driverUUID));
        alias.appendChild(driverIdentifier);

        // driverProperties
        Element driverProperties = aliasDocument.createElement("driverProperties");
        driverProperties.setAttribute("Class", "net.sourceforge.squirrel_sql.fw.sql.SQLDriverPropertyCollection");

        Element driverPropertiesInner = aliasDocument.createElement("driverProperties");
        driverPropertiesInner.setAttribute("Indexed", "true");
        driverProperties.appendChild(driverPropertiesInner);

        alias.appendChild(driverProperties);

        // Identifier
        Element identifier = aliasDocument.createElement("identifier");
        identifier.setAttribute("Class", "net.sourceforge.squirrel_sql.fw.id.UidIdentifier");
        identifier.appendChild(createStringElement(aliasDocument, "string", UUID.randomUUID().toString()));
        alias.appendChild(identifier);

        // name
        alias.appendChild(createStringElement(aliasDocument, "name", aliasConfig.getAliasName()));

        // password
        alias.appendChild(createStringElement(aliasDocument, "password", aliasConfig.getPassword()));

        // SchemaProperties
        Element schemaProperties = aliasDocument.createElement("schemaProperties");
        schemaProperties.setAttribute("Class", "net.sourceforge.squirrel_sql.client.gui.db.SQLAliasSchemaProperties");

        Element allSchemaProceduresNotToBeCached = aliasDocument.createElement("allSchemaProceduresNotToBeCached");
        allSchemaProceduresNotToBeCached.setAttribute("Indexed", "true");
        schemaProperties.appendChild(allSchemaProceduresNotToBeCached);

        schemaProperties.appendChild(createStringElement(aliasDocument, "cacheSchemaIndependentMetaData", "false"));
        schemaProperties.appendChild(createStringElement(aliasDocument, "expectsSomeCachedData", "false"));
        schemaProperties.appendChild(createStringElement(aliasDocument, "globalState", LoadType.DONT_LOAD.toString()));

        Element schemaDetails = aliasDocument.createElement("schemaDetails");
        schemaDetails.setAttribute("Indexed", "true");
        schemaProperties.appendChild(schemaDetails);

        // Schema's toevoegen
        for (SchemaLoadConfig schemaLoadConfig : aliasConfig.getSchemasToLoad()) {
            maakOfUpdateSchemaDetails(aliasDocument, schemaDetails, schemaLoadConfig);
        }

        alias.appendChild(schemaProperties);

        // Jdbc url
        alias.appendChild(createStringElement(aliasDocument, "url", aliasConfig.getJdbcUrl() == null ? defaultJdbcUrl : aliasConfig.getJdbcUrl()));

        // useDriverProperties
        alias.appendChild(createStringElement(aliasDocument, "useDriverProperties", "false"));

        // userName
        alias.appendChild(createStringElement(aliasDocument, "userName", aliasConfig.getUserName()));

        return alias;
    }

    public static void updateAlias(Document aliasDocument, Element aliasElement, AliasConfig aliasConfig, String driverUUID) {
        Preconditions.checkArgument(aliasDocument != null);
        Preconditions.checkArgument(aliasElement != null);
        Preconditions.checkArgument(aliasConfig != null);

        // Update driver identifier
        if(!Strings.isNullOrEmpty(driverUUID)){
            Node driverIdentifier = Iterables.getOnlyElement(getChildrenByTagNaam(aliasElement, "driverIdentifier"));
            updateChildNode(aliasDocument, driverIdentifier, "string", driverUUID);
        }

        // Update password
        updateChildNode(aliasDocument, aliasElement, "password", aliasConfig.getPassword());

        // Update user
        updateChildNode(aliasDocument, aliasElement, "userName", aliasConfig.getUserName());

        // Update jdbcUrl
        if (!Strings.isNullOrEmpty(aliasConfig.getJdbcUrl())) {
            updateChildNode(aliasDocument, aliasElement, "url", aliasConfig.getJdbcUrl());
        }

        // Update schema's to load
        Node schemaPropertiesNode = Iterables.getOnlyElement(getChildrenByTagNaam(aliasElement, "schemaProperties"));
        Node schemaDetailsNode = Iterables.getOnlyElement(getChildrenByTagNaam(schemaPropertiesNode, "schemaDetails"));
        clearChilds(schemaDetailsNode);

        for (SchemaLoadConfig schemaLoadConfig : aliasConfig.getSchemasToLoad()) {
            maakOfUpdateSchemaDetails(aliasDocument, schemaDetailsNode, schemaLoadConfig);
        }
    }

    public static void maakOfUpdateSchemaDetails(Document aliasDocument, Node schemaDetailsNode, SchemaLoadConfig schemaLoadConfig) {
        Preconditions.checkArgument(aliasDocument != null);
        Preconditions.checkArgument(schemaDetailsNode != null);
        Preconditions.checkArgument(schemaLoadConfig != null && !Strings.isNullOrEmpty(schemaLoadConfig.getSchemaName()), "Schema naam is nodig");

        Node bestaandeProperties = findSchemaPropertiesByName(schemaDetailsNode, schemaLoadConfig.getSchemaName());
        if (bestaandeProperties == null) {
            Element schemaDetailProperties = aliasDocument.createElement("Bean");
            schemaDetailProperties.setAttribute("Class", "net.sourceforge.squirrel_sql.client.gui.db.SQLAliasSchemaDetailProperties");
            schemaDetailProperties.appendChild(createStringElement(aliasDocument, "procedure", schemaLoadConfig.getProcedureLoadType().toString()));
            schemaDetailProperties.appendChild(createStringElement(aliasDocument, "schemaName", schemaLoadConfig.getSchemaName()));
            schemaDetailProperties.appendChild(createStringElement(aliasDocument, "table", schemaLoadConfig.getTableLoadType().toString()));
            schemaDetailProperties.appendChild(createStringElement(aliasDocument, "view", schemaLoadConfig.getViewLoadType().toString()));

            schemaDetailsNode.appendChild(schemaDetailProperties);
        } else {
            updateChildNode(aliasDocument, bestaandeProperties, "table", schemaLoadConfig.getTableLoadType().toString());
            updateChildNode(aliasDocument, bestaandeProperties, "procedure", schemaLoadConfig.getProcedureLoadType().toString());
            updateChildNode(aliasDocument, bestaandeProperties, "view", schemaLoadConfig.getViewLoadType().toString());
        }
    }

    public static Node findSchemaPropertiesByName(Node schemaPropertiesNode, String schemaName) {
        Preconditions.checkArgument(schemaPropertiesNode != null);
        NodeList schemaPropertieNodes = schemaPropertiesNode.getChildNodes();

        for (int i = 0; i < schemaPropertieNodes.getLength(); i++) {
            Predicate<Node> schemaPropertiesMetNaamPredicate = Predicates
                    .and(SCHEMA_PROPERTY_NODE, new HeeftChildNodeMetNaamEnWaardePredicate("schemaName", schemaName));
            Node schemaPropertiesMetNaam = schemaPropertieNodes.item(i);
            if (schemaPropertiesMetNaamPredicate.apply(schemaPropertiesMetNaam)) {
                return schemaPropertiesMetNaam;
            }
        }
        return null;
    }

    public static Iterable<Element> getAllAliases(Document document) {
        NodeList beans = document.getElementsByTagName("Bean");
        List<Element> aliassen = Lists.newArrayList();
        for (int i = 0; i < beans.getLength(); i++) {
            Node bean = beans.item(i);
            if (ALIAS_NODE.apply(bean)) {
                aliassen.add((Element) bean);
            }
        }
        return aliassen;
    }
}
