package nl.berg.squirrel_configure_xml.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AliasConfig {

    private final String aliasName;
    private final String jdbcUrl;
    private final String userName;
    private final String password;

    @JsonProperty
    private List<SchemaLoadConfig> schemasToLoad;

    @JsonCreator
    public AliasConfig(@JsonProperty(value = "naam", required = true) String aliasName,
            @JsonProperty(value = "jdbcUrl", required = false) String defaultJdbcUrl,
            @JsonProperty(value = "user", required = true) String user,
            @JsonProperty(value = "password", required = true) String password) {
        this.aliasName = aliasName;
        this.jdbcUrl = defaultJdbcUrl;
        this.userName = user;
        this.password = password;
    }

    public String getAliasName() {
        return aliasName;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public List<SchemaLoadConfig> getSchemasToLoad() {
        return schemasToLoad;
    }

    public void setSchemasToLoad(List<SchemaLoadConfig> schemasToLoad) {
        this.schemasToLoad = schemasToLoad;
    }
}
