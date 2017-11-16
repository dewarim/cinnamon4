package com.dewarim.cinnamon.model.request;

public class UserInfoRequest {
    
    Long userId;
    String username;

    public UserInfoRequest() {
    }

    public UserInfoRequest(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public boolean byId(){
        return userId != null;
    }
    
    public boolean byName(){
        return username != null;
    }
}
