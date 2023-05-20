package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;

public class ChangeTrigger implements Identifiable {

    private Long id;
    private String name;
    private String controller;
    private String action;
    private boolean active;
    private boolean preTrigger;
    private boolean postTrigger;
    private boolean copyFileContent;
    private int ranking;
    private String config = "<config/>";
    private ChangeTriggerType triggerType;

    public ChangeTrigger() {
    }

    public ChangeTrigger(Long id, String name, String controller, String action,
                         boolean active, boolean preTrigger, boolean postTrigger, boolean copyFileContent,
                         String config, ChangeTriggerType changeTriggerType, int ranking) {
        this.id = id;
        this.name = name;
        this.controller = controller;
        this.action = action;
        this.active = active;
        this.preTrigger = preTrigger;
        this.postTrigger = postTrigger;
        this.copyFileContent = copyFileContent;
        this.config = config;
        this.triggerType=changeTriggerType;
        this.ranking=ranking;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isPreTrigger() {
        return preTrigger;
    }

    public void setPreTrigger(boolean preTrigger) {
        this.preTrigger = preTrigger;
    }

    public boolean isPostTrigger() {
        return postTrigger;
    }

    public void setPostTrigger(boolean postTrigger) {
        this.postTrigger = postTrigger;
    }

    public boolean isCopyFileContent() {
        return copyFileContent;
    }

    public void setCopyFileContent(boolean copyFileContent) {
        this.copyFileContent = copyFileContent;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public ChangeTriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(ChangeTriggerType triggerType) {
        this.triggerType = triggerType;
    }

    @Override
    public String toString() {
        return "ChangeTrigger{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", controller='" + controller + '\'' +
                ", action='" + action + '\'' +
                ", active=" + active +
                ", preTrigger=" + preTrigger +
                ", postTrigger=" + postTrigger +
                ", copyFileContent=" + copyFileContent +
                ", config='" + config + '\'' +
                '}';
    }


}
