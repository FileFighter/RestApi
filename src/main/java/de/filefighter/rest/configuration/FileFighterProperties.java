package de.filefighter.rest.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "filefighter")
public class FileFighterProperties {

    /**
     * Version String.
     */
    private String version = "undefined";
    private String date = "undefined";
    private boolean disablePasswordCheck = false;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isDisablePasswordCheck() {
        return disablePasswordCheck;
    }

    public void setDisablePasswordCheck(boolean disablePasswordCheck) {
        this.disablePasswordCheck = disablePasswordCheck;
    }
}