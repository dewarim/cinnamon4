package com.dewarim.cinnamon.model.request;

public class CreateRelationRequest {

    private Long   leftId;
    private Long   rightId;
    private String typeName;
    private String metadata;

    public CreateRelationRequest() {
    }

    public CreateRelationRequest(Long leftId, Long rightId, String typeName, String metadata) {
        this.leftId = leftId;
        this.rightId = rightId;
        this.typeName = typeName;
        this.metadata = metadata;
    }

    public Long getLeftId() {
        return leftId;
    }

    public void setLeftId(Long leftId) {
        this.leftId = leftId;
    }

    public Long getRightId() {
        return rightId;
    }

    public void setRightId(Long rightId) {
        this.rightId = rightId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getMetadata() {
        if(metadata == null){
            return "<meta/>";
        }
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public boolean validated() {
        return leftId != null && rightId != null && typeName != null && 
               leftId > 0 && rightId > 0 && typeName.trim().length() > 0;
    }

}
