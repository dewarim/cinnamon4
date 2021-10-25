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
import java.util.stream.Collectors;

@JacksonXmlRootElement(localName = "createUiLanguageRequest")
public class CreateUiLanguageRequest implements CreateRequest<UiLanguage>, ApiRequest {

    @JacksonXmlElementWrapper(localName = "isoCodes")
    @JacksonXmlProperty(localName = "isoCode")
    private List<String> isoCodes = new ArrayList<>();

    @Override
    public List<UiLanguage> list() {
        return isoCodes.stream().map(name -> new UiLanguage(null, name)).collect(Collectors.toList());
    }

    public CreateUiLanguageRequest() {
    }

    public CreateUiLanguageRequest(List<String> isoCodes) {
        this.isoCodes = isoCodes;
    }

    public List<String> getIsoCodes() {
        return isoCodes;
    }

    @Override
    public boolean validated() {
        return isoCodes.stream().noneMatch(name -> name == null || name.trim().isEmpty());
    }

    @Override
    public Wrapper<UiLanguage> fetchResponseWrapper() {
        return new UiLanguageWrapper();
    }

    @Override
    public List<Object> examples() {
        return List.of(new CreateUiLanguageRequest(List.of("en", "de", "fr")));
    }
}
