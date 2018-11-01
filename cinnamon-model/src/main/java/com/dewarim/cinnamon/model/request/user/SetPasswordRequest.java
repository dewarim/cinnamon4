package com.dewarim.cinnamon.model.request.user;

public class SetPasswordRequest {
    
    private Long userId;
    private String password;

    public SetPasswordRequest() {
    }

    public SetPasswordRequest(Long userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
