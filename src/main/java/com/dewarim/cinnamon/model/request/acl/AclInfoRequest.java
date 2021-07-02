package com.dewarim.cinnamon.model.request.acl;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "aclInfoRequest")
public class AclInfoRequest implements ApiRequest {
    
    private Long aclId;
    private String name;

    public AclInfoRequest() {
    }

    public AclInfoRequest(Long aclId, String name) {
        this.aclId = aclId;
        this.name = name;
    }

    public Long getAclId() {
        return aclId;
    }

    public void setAclId(Long aclId) {
        this.aclId = aclId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public boolean byId(){
        return aclId != null;
    }
    
    public boolean byName(){
        return name != null;
    }
}
