package com.dewarim.cinnamon.api.login;

/**
 * Mapping class for Login Connectors.
 */
public class GroupMapping {

    public GroupMapping() {
    }

    public GroupMapping(String externalGroup, String cinnamonGroup) {
        this.externalGroup = externalGroup;
        this.cinnamonGroup = cinnamonGroup;
    }

    private String externalGroup;
    private String cinnamonGroup;

    public String getExternalGroup() {
        return externalGroup;
    }

    public void setExternalGroup(String externalGroup) {
        this.externalGroup = externalGroup;
    }

    public String getCinnamonGroup() {
        return cinnamonGroup;
    }

    public void setCinnamonGroup(String cinnamonGroup) {
        this.cinnamonGroup = cinnamonGroup;
    }
}