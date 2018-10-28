package nl.berg.squirrel_configure_xml.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SchemaLoadConfig {

    private final String schemaName;
    private final LoadType tableLoadType;
    private final LoadType viewLoadType;
    private final LoadType procedureLoadType;

    @JsonCreator
    public SchemaLoadConfig(@JsonProperty(value = "schemaName", required = true) String schemaName,
            @JsonProperty(value = "tableLoadType", required = false) LoadType tableLoadType,
            @JsonProperty(value = "viewLoadType", required = false)LoadType viewLoadType,
            @JsonProperty(value = "procedureLoadType", required = false) LoadType procedureLoadType) {
        this.schemaName = schemaName;
        this.tableLoadType = tableLoadType == null ? LoadType.LOAD_BUT_DONT_CACHE : tableLoadType;
        this.viewLoadType = viewLoadType == null ? LoadType.DONT_LOAD : viewLoadType;
        this.procedureLoadType = procedureLoadType == null ? LoadType.DONT_LOAD : procedureLoadType;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public LoadType getTableLoadType() {
        return tableLoadType;
    }

    public LoadType getViewLoadType() {
        return viewLoadType;
    }

    public LoadType getProcedureLoadType() {
        return procedureLoadType;
    }
}
