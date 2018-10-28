package nl.berg.squirrel_configure_xml.util;

import static nl.berg.squirrel_configure_xml.util.DocumentUtil.getChildrenByTagNaam;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import nl.berg.squirrel_configure_xml.domain.AliasConfig;
import nl.berg.squirrel_configure_xml.domain.DriverConfig;
import nl.berg.squirrel_configure_xml.domain.LoadType;
import nl.berg.squirrel_configure_xml.domain.SchemaLoadConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class OpdrachtUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpdrachtUtil.class);

    private OpdrachtUtil(){}

    public static void aanmakenAlias(Document aliasDocument, Document aliasTreeDocument, Element driverElement, Map<String, String> propertyMap) {
        String driverConfigLocation = propertyMap.get(Constants.ALIAS_CONFIG);
        Preconditions.checkArgument(driverConfigLocation != null,
                "Alias config is nodig voor aanmaken driver. Lever aan als %s=pad", Constants.ALIAS_CONFIG);

        File aliasConfigFile = new File(driverConfigLocation);
        Preconditions.checkState(aliasConfigFile.isFile(), "Driver config file: %s niet gevonden:", aliasConfigFile);
        try {
            AliasConfig aliasConfig = new ObjectMapper().readValue(aliasConfigFile, AliasConfig.class);
            AliasUtil.addOrUpdateAlias(aliasDocument, aliasTreeDocument, aliasConfig, DriverUtil.getDriverUUID(driverElement),
                    DriverUtil.getDriverJdbcUrl(driverElement));
        } catch (IOException e) {
            LOGGER.error("Fout bij uitlezen driverConfig: %s", driverConfigLocation);
        }
    }

    public static void aanmakenDriver(Document driverDocument, Map<String, String> propertyMap) {
        String driverConfigLocation = propertyMap.get(Constants.DRIVER_CONFIG);
        Preconditions.checkArgument(driverConfigLocation != null,
                "Driver config is nodig voor aanmaken driver. Lever aan als %s=pad", Constants.DRIVER_CONFIG);

        File driverConfigFile = new File(driverConfigLocation);
        Preconditions.checkState(driverConfigFile.isFile(), "Driver config file: %s niet gevonden:", driverConfigFile);

        try {
            DriverConfig driverConfig = new ObjectMapper().readValue(driverConfigFile, DriverConfig.class);
            DriverUtil.addNewDriver(driverDocument, driverConfig);
        } catch (IOException e) {
            LOGGER.error("Fout bij uitlezen driverConfig: %s", driverConfigLocation);
        }
    }

    public static void toevoegenSchema(Document aliasDocument, Map<String, String> propertyMap) {
        String aliasNaam = propertyMap.get(Constants.NAAM_ALIAS);
        Preconditions.checkArgument(aliasNaam != null, "Aliasnaam is nodig om aan alias schema toe te voegen. Lever aan met %s=ONT", Constants.NAAM_ALIAS);

        String schemaNaam = propertyMap.get(Constants.SCHEMA);
        Preconditions.checkArgument(schemaNaam != null, "Schemanaam is nodig om aan alias schema toe te voegen. Lever aan met %s=IN999ONT", Constants.SCHEMA);

        String tabelLaden = propertyMap.get(Constants.TABEL_LADEN);
        String proceduresLaden = propertyMap.get(Constants.PROCEDURES_LADEN);
        String viewsLaden = propertyMap.get(Constants.VIEWS_LADEN);

        SchemaLoadConfig schemaLoadConfig = new SchemaLoadConfig(schemaNaam,
                tabelLaden == null ? null : LoadType.valueOf(tabelLaden),
                viewsLaden == null ? null : LoadType.valueOf(viewsLaden),
                proceduresLaden == null ? null : LoadType.valueOf(proceduresLaden));

        // Opzoeken details node
        Element aliasByName = AliasUtil.findAliasByName(aliasDocument, aliasNaam);
        Node schemaPropertiesNode = Iterables.getOnlyElement(getChildrenByTagNaam(aliasByName, "schemaProperties"));
        Node schemaDetailsNode = Iterables.getOnlyElement(getChildrenByTagNaam(schemaPropertiesNode, "schemaDetails"));

        AliasUtil.maakOfUpdateSchemaDetails(aliasDocument, schemaDetailsNode, schemaLoadConfig);
    }
}
