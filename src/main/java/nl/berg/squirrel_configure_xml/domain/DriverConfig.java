package nl.berg.squirrel_configure_xml.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DriverConfig {

    private final String jarPath;
    private final String driverName;
    private final String defaultJdbcUrl;

    @JsonCreator
    public DriverConfig(@JsonProperty(value = "driverPath", required = true) String jarPath,
            @JsonProperty(value = "naam", required = true) String driverName,
            @JsonProperty(value = "jdbcUrl", required = false) String defaultJdbcUrl) {
        this.jarPath = jarPath;
        this.driverName = driverName;
        this.defaultJdbcUrl = defaultJdbcUrl;
    }

    public String getJarPath() {
        return jarPath;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getDefaultJdbcUrl() {
        return defaultJdbcUrl;
    }
}
