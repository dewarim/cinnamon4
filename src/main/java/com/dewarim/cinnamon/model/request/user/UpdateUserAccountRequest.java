package com.dewarim.cinnamon.model.request.user;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.response.UserInfo;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Optional;

@JacksonXmlRootElement(localName = "updateUserAccountRequest")
public class UpdateUserAccountRequest extends UserInfo implements ApiRequest {

    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private boolean validated(){
        boolean hasValue = getName() != null ||
                getLoginType() != null ||
                (getUiLanguageId() != null && getUiLanguageId() > 0) ||
                getEmail() != null ||
                getFullname() != null;
        return getId() != null && getId() > 0 && hasValue;
    }

    public Optional<UpdateUserAccountRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

}
