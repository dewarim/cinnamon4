package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.FolderType;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.IndexItem;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.ObjectType;
import com.dewarim.cinnamon.model.Permission;
import com.dewarim.cinnamon.model.UiLanguage;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class ConfigWrapper implements ApiResponse, Wrapper<ConfigWrapper> {

    @JacksonXmlElementWrapper(localName = "acls")
    @JacksonXmlProperty(localName = "acl")
    private    List<Acl> acls = new ArrayList<>();
    
    @JacksonXmlElementWrapper(localName = "folderTypes")
    @JacksonXmlProperty(localName = "folderType")
    private List<FolderType> folderTypes = new ArrayList<>();

    @JacksonXmlElementWrapper(localName = "formats")
    @JacksonXmlProperty(localName = "format")
    private List<Format> formats = new ArrayList<>();

    @JacksonXmlElementWrapper(localName = "groups")
    @JacksonXmlProperty(localName = "group")
    private List<Group> groups = new ArrayList<>();
    
    @JacksonXmlElementWrapper(localName = "indexItems")
    @JacksonXmlProperty(localName = "indexItem")
    private List<IndexItem> indexItems = new ArrayList<>();

    @JacksonXmlElementWrapper(localName = "languages")
    @JacksonXmlProperty(localName = "language")
    private List<Language> languages = new ArrayList<>();

    @JacksonXmlElementWrapper(localName = "lifecycles")
    @JacksonXmlProperty(localName = "lifecycle")
    private List<Lifecycle> lifecycles = new ArrayList<>();

    @JacksonXmlElementWrapper(localName = "metasetTypes")
    @JacksonXmlProperty(localName = "metasetType")
    private List<MetasetType> metasetTypes = new ArrayList<>();

    @JacksonXmlElementWrapper(localName = "objectTypes")
    @JacksonXmlProperty(localName = "objectType")
    private List<ObjectType> objectTypes = new ArrayList<>();

    @JacksonXmlElementWrapper(localName = "permissions")
    @JacksonXmlProperty(localName = "permission")
    private List<Permission> permissions = new ArrayList<>();

    @JacksonXmlElementWrapper(localName = "relationTypes")
    @JacksonXmlProperty(localName = "relationType")
    private List<RelationType> relationTypes = new ArrayList<>();

    @JacksonXmlElementWrapper(localName = "uiLanguages")
    @JacksonXmlProperty(localName = "uiLanguage")
    private List<UiLanguage> uiLanguages = new ArrayList<>();

    @JacksonXmlElementWrapper(localName = "users")
    @JacksonXmlProperty(localName = "user")
    List<UserAccount> users = new ArrayList<>();
    
    public List<Acl> getAcls() {
        return acls;
    }

    public void setAcls(List<Acl> acls) {
        this.acls = acls;
    }

    public List<FolderType> getFolderTypes() {
        return folderTypes;
    }

    public void setFolderTypes(List<FolderType> folderTypes) {
        this.folderTypes = folderTypes;
    }

    public List<Format> getFormats() {
        return formats;
    }

    public void setFormats(List<Format> formats) {
        this.formats = formats;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<IndexItem> getIndexItems() {
        return indexItems;
    }

    public void setIndexItems(List<IndexItem> indexItems) {
        this.indexItems = indexItems;
    }

    public List<Language> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Language> languages) {
        this.languages = languages;
    }

    public List<Lifecycle> getLifecycles() {
        return lifecycles;
    }

    public void setLifecycles(List<Lifecycle> lifecycles) {
        this.lifecycles = lifecycles;
    }

    public List<MetasetType> getMetasetTypes() {
        return metasetTypes;
    }

    public void setMetasetTypes(List<MetasetType> metasetTypes) {
        this.metasetTypes = metasetTypes;
    }

    public List<ObjectType> getObjectTypes() {
        return objectTypes;
    }

    public void setObjectTypes(List<ObjectType> objectTypes) {
        this.objectTypes = objectTypes;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    public List<RelationType> getRelationTypes() {
        return relationTypes;
    }

    public void setRelationTypes(List<RelationType> relationTypes) {
        this.relationTypes = relationTypes;
    }

    public List<UiLanguage> getUiLanguages() {
        return uiLanguages;
    }

    public void setUiLanguages(List<UiLanguage> uiLanguages) {
        this.uiLanguages = uiLanguages;
    }

    public List<UserAccount> getUsers() {
        return users;
    }

    public void setUsers(List<UserAccount> users) {
        this.users = users;
    }

    @Override
    public List<ConfigWrapper> list() {
        return Collections.singletonList(this);
    }

    @Override
    public Wrapper<ConfigWrapper> setList(List<ConfigWrapper> configWrappers) {
        throw new IllegalStateException("Cannot set list of configWrappers");
    }

}
