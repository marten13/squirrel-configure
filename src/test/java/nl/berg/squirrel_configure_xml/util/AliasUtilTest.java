package nl.berg.squirrel_configure_xml.util;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import nl.berg.squirrel_configure_xml.domain.AliasConfig;
import nl.berg.squirrel_configure_xml.domain.SchemaLoadConfig;
import nl.berg.squirrel_configure_xml.domain.predicate.HeeftChildNodeMetNaamEnWaardePredicate;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.List;
import java.util.UUID;

import static nl.berg.squirrel_configure_xml.domain.LoadType.DONT_LOAD;
import static nl.berg.squirrel_configure_xml.domain.LoadType.LOAD_AND_CACHE;
import static nl.berg.squirrel_configure_xml.domain.LoadType.LOAD_BUT_DONT_CACHE;
import static nl.berg.squirrel_configure_xml.util.TestUtil.createNewDocument;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

public class AliasUtilTest {

    String jdbcUrl = "jdbc:as400://172.30.253.31";
    String aliasNaam = "AliasjeVoorTest";
    String driverUUID = UUID.randomUUID().toString();
    String username = "TestUser";
    String password = "PasWoord";
    String schemaNaam = "IN025BER";


    @Test
    public void testAddAlias() throws Exception {
        Document aliasDocument = createNewDocument();
        Element beans1 = aliasDocument.createElement("Beans");

        aliasDocument.appendChild(beans1);

        Document aliasTreeDocument = createNewDocument();
        Element beans2 = aliasTreeDocument.createElement("Beans");
        aliasTreeDocument.appendChild(beans2);

        AliasConfig aliasConfig = new AliasConfig(aliasNaam, jdbcUrl, username, password);
        List<SchemaLoadConfig> schemaLoadConfigs = Lists.newArrayList(new SchemaLoadConfig(schemaNaam, LOAD_BUT_DONT_CACHE, LOAD_AND_CACHE, DONT_LOAD));
        aliasConfig.setSchemasToLoad(schemaLoadConfigs);

        AliasUtil.addOrUpdateAlias(aliasDocument, aliasTreeDocument, aliasConfig, driverUUID, jdbcUrl);

        Element aliasBijNaam = AliasUtil.findAliasByName(aliasDocument, aliasNaam);
        assertThat(aliasBijNaam, not(nullValue()));

        assertTrue(new HeeftChildNodeMetNaamEnWaardePredicate("name", aliasNaam).apply(aliasBijNaam));
        assertTrue(new HeeftChildNodeMetNaamEnWaardePredicate("userName", username).apply(aliasBijNaam));
        assertTrue(new HeeftChildNodeMetNaamEnWaardePredicate("password", password).apply(aliasBijNaam));
        assertTrue(new HeeftChildNodeMetNaamEnWaardePredicate("url", jdbcUrl).apply(aliasBijNaam));

        Node schemaPropertieNode = Iterables.getOnlyElement(DocumentUtil.getChildrenByTagNaam(aliasBijNaam, "schemaProperties"));
        assertTrue(new HeeftChildNodeMetNaamEnWaardePredicate("globalState", DONT_LOAD.toString()).apply(schemaPropertieNode));

        Node schemaDetailsNode = Iterables.getOnlyElement(DocumentUtil.getChildrenByTagNaam(schemaPropertieNode, "schemaDetails"));
        NodeList schemaDetailNodes = schemaDetailsNode.getChildNodes();
        assertThat(schemaDetailNodes.getLength(), is(1));

        Node schemaDetails = schemaDetailsNode.getFirstChild();
        assertTrue(new HeeftChildNodeMetNaamEnWaardePredicate("procedure", "2").apply(schemaDetails));
        assertTrue(new HeeftChildNodeMetNaamEnWaardePredicate("schemaName", schemaNaam).apply(schemaDetails));
        assertTrue(new HeeftChildNodeMetNaamEnWaardePredicate("table", "0").apply(schemaDetails));
        assertTrue(new HeeftChildNodeMetNaamEnWaardePredicate("view", "1").apply(schemaDetails));

        Node driverIdentifier = Iterables.getOnlyElement(DocumentUtil.getChildrenByTagNaam(aliasBijNaam, "driverIdentifier"));
        assertTrue(new HeeftChildNodeMetNaamEnWaardePredicate("string", driverUUID).apply(driverIdentifier));

        Node identifier = Iterables.getOnlyElement(DocumentUtil.getChildrenByTagNaam(aliasBijNaam, "identifier"));
        assertFalse(new HeeftChildNodeMetNaamEnWaardePredicate("string", driverUUID).apply(identifier));


        File aliasFile = new File("target/aliasTest.xml");
        DocumentUtil.writeDocument(aliasDocument, aliasFile);

        File aliasTreeFile = new File("target/aliasTreeTest.xml");
        DocumentUtil.writeDocument(aliasTreeDocument, aliasTreeFile);
    }
}