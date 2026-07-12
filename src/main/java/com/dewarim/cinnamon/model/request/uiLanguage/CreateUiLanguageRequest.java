package com.dewarim.cinnamon.model.request.uiLanguage;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.UiLanguage;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.UiLanguageWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("createUiLanguageRequest")
public record CreateUiLanguageRequest(
        @JacksonXmlElementWrapper(localName = "uiLanguages")
        @JacksonXmlProperty(localName = "uiLanguage")
        List<UiLanguage> uiLanguages) implements CreateRequest<UiLanguage>, ApiRequest<CreateUiLanguageRequest> {

    public CreateUiLanguageRequest {
        if (uiLanguages == null) {
            uiLanguages = new ArrayList<>();
        }
    }

    @Override
    public List<UiLanguage> list() {
        return uiLanguages;
    }

    @Override
    public boolean validated() {
        return uiLanguages.stream().noneMatch(uiLanguage -> uiLanguage == null ||
                uiLanguage.getIsoCode() == null ||
                uiLanguage.getIsoCode().trim().isEmpty());
    }

    @Override
    public Wrapper<UiLanguage> fetchResponseWrapper() {
        return new UiLanguageWrapper();
    }

    @Override
    public List<ApiRequest<CreateUiLanguageRequest>> examples() {
        return List.of(new CreateUiLanguageRequest(List.of(
                new UiLanguage("en"), new UiLanguage("de"), new UiLanguage("fr"))));
    }
}
