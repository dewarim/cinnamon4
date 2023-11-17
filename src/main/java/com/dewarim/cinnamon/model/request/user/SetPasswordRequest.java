package com.dewarim.cinnamon.model.request.user;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.Optional;

@JacksonXmlRootElement(localName = "setPasswordRequest")
public class SetPasswordRequest implements ApiRequest<SetPasswordRequest> {

    private Long   userId;
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

    public Optional<SetPasswordRequest> validateRequest() {
        if (password != null && userId != null && userId > 0) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<SetPasswordRequest>> examples() {
        return List.of(new SetPasswordRequest(123L, "my-new-secret-password"));
    }
}
