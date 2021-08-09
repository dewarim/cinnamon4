package com.dewarim.cinnamon.model.request.user;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "userInfoRequest")
public class UserInfoRequest implements ApiRequest {
    
    private Long userId;
    private String username;

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
