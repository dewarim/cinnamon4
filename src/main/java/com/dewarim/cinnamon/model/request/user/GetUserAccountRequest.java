package com.dewarim.cinnamon.model.request.user;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "getUserAccountRequest")
public class GetUserAccountRequest implements ApiRequest<GetUserAccountRequest> {
    
    private Long userId;
    private String username;

    public GetUserAccountRequest() {
    }

    public GetUserAccountRequest(Long userId, String username) {
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

    @Override
    public List<ApiRequest<GetUserAccountRequest>> examples() {
        return List.of(new GetUserAccountRequest(1L,null),new GetUserAccountRequest(null, "by-name") );
    }
}
