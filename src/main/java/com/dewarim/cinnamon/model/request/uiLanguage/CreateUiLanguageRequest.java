package com.dewarim.cinnamon.model.request.uiLanguage;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.UiLanguage;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.UiLanguageWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "createUiLanguageRequest")
public class CreateUiLanguageRequest implements CreateRequest<UiLanguage>, ApiRequest {

    @JacksonXmlElementWrapper(localName = "uiLanguages")
    @JacksonXmlProperty(localName = "uiLanguage")
    private List<UiLanguage> uiLanguages = new ArrayList<>();

    @Override
    public List<UiLanguage> list() {
        return uiLanguages;
    }

    public CreateUiLanguageRequest() {
    }

    public CreateUiLanguageRequest(List<UiLanguage> uiLanguages) {
        this.uiLanguages = uiLanguages;
    }

    public List<UiLanguage> getUiLanguages() {
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
    public List<Object> examples() {
        return List.of(new CreateUiLanguageRequest(List.of(
                new UiLanguage("en"), new UiLanguage("de"), new UiLanguage("fr"))));
    }
}
