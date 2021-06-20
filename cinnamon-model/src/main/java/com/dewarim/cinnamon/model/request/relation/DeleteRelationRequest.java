package com.dewarim.cinnamon.model.request.relation;

public class DeleteRelationRequest {
    
    private Long leftId;
    private Long rightId;
    private String typeName;

    public DeleteRelationRequest() {
    }

    public DeleteRelationRequest(Long leftId, Long rightId, String typeName) {
        this.leftId = leftId;
        this.rightId = rightId;
        this.typeName = typeName;
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

    public boolean validated() {
        return leftId != null && rightId != null && typeName != null &&
               leftId > 0 && rightId > 0 && typeName.trim().length() > 0;
    }
}
