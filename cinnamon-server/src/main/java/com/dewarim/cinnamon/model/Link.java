package com.dewarim.cinnamon.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "link")
public class Link {
    
    private Long id;
    private LinkType type;
    private LinkResolver resolver;
    private Long userId;
    private Long aclId;
    private Long parentId;
    private Long folderId;
    private Long objectId;
    private Long version;
    
}
