package com.dewarim.cinnamon.model.request.aclGroup;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Optional;

@JacksonXmlRootElement(localName = "aclGroupListRequest")
public class AclGroupListRequest implements ApiRequest {

    public enum IdType{
        ACL,GROUP
    }

    private Long id;
    private IdType idType;


    public AclGroupListRequest() {
    }

    public AclGroupListRequest(Long id, IdType idType) {
        this.id = id;
        this.idType = idType;
    }

    private boolean validated() {
        return id != null && id> 0  || (idType != null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IdType getIdType() {
        return idType;
    }

    public void setIdType(IdType idType) {
        this.idType = idType;
    }

    public Optional<AclGroupListRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return "AclGroupListRequest{" +
                "id=" + id +
                ", idType=" + idType +
                '}';
    }
}
