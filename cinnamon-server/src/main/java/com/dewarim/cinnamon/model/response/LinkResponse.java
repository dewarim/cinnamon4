package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.model.*;
import com.dewarim.cinnamon.model.links.LinkType;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "link")
public class LinkResponse {

    private LinkType linkType;
    private ObjectSystemData osd;
    private Folder folder;
    private Long ownerId;
    private Long parentId;
    private Long aclId;
    
    public LinkType getLinkType() {
        return linkType;
    }

    public void setLinkType(LinkType linkType) {
        this.linkType = linkType;
    }

    public ObjectSystemData getOsd() {
        return osd;
    }

    public void setOsd(ObjectSystemData osd) {
        this.osd = osd;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    
    public Long getAclId() {
        return aclId;
    }

    public void setAclId(Long aclId) {
        this.aclId = aclId;
    }
}
