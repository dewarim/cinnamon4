package com.dewarim.cinnamon.model.request.user;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.Optional;

@JacksonXmlRootElement(localName = "setUserConfigRequest")
public class SetUserConfigRequest implements ApiRequest<SetUserConfigRequest> {

    private Long   userId;
    private String config;

    public SetUserConfigRequest() {
    }

    public SetUserConfigRequest(Long userId, String config) {
        this.userId = userId;
        this.config = config;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public Optional<SetUserConfigRequest> validateRequest() {
        if (config != null && userId != null && userId > 0) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<SetUserConfigRequest>> examples() {
        return List.of(new SetUserConfigRequest(123L,
                "<config><lastSearches><search>foo</search></lastSearches></config>"));
    }
}
