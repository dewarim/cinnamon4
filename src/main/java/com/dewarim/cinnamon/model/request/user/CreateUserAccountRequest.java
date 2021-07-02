package com.dewarim.cinnamon.model.request.user;

import com.dewarim.cinnamon.api.ApiRequest;

import java.util.Optional;

public class CreateUserAccountRequest implements ApiRequest {

    private String name;
    private String password;
    private String fullname;
    private String email;
    private Long languageId;
    private String loginType;
    private Boolean changeTracking;

    public CreateUserAccountRequest() {
    }

    public CreateUserAccountRequest(String name, String password, String fullname, String email, Long languageId, String loginType, Boolean changeTracking) {
        this.name = name;
        this.password = password;
        this.fullname = fullname;
        this.email = email;
        this.languageId = languageId;
        this.loginType = loginType;
        this.changeTracking = changeTracking;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Long languageId) {
        this.languageId = languageId;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public boolean isChangeTracking() {
        return changeTracking;
    }

    public void setChangeTracking(boolean changeTracking) {
        this.changeTracking = changeTracking;
    }

    @Override
    public String toString() {
        return "CreateUserAccountRequest{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", fullname='" + fullname + '\'' +
                ", email='" + email + '\'' +
                ", languageId=" + languageId +
                ", loginType='" + loginType + '\'' +
                ", changeTracking=" + changeTracking +
                '}';
    }

    private boolean validated(){
        boolean nonNull =  name != null && password != null && fullname != null && email != null && languageId != null && loginType != null && changeTracking != null;
        if(!nonNull){
            return false;
        }
        boolean empty = name.isEmpty() || password.isEmpty() || fullname.isEmpty() || email.isEmpty() || loginType.isEmpty();
        if(empty){
            return false;
        }
        return languageId > 0;
    }

    public Optional<CreateUserAccountRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }
}
