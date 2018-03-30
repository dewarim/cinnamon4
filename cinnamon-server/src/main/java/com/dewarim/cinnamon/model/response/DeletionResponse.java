package com.dewarim.cinnamon.model.response;

public class DeletionResponse {
    
    private boolean notFound;
    private boolean success;

    public DeletionResponse() {
    }

    public boolean isNotFound() {
        return notFound;
    }

    public void setNotFound(boolean notFound) {
        this.notFound = notFound;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
