package com.dewarim.cinnamon.model.request.uiLanguage;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.UiLanguage;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.UiLanguageWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JacksonXmlRootElement(localName = "createUiLanguageRequest")
public class CreateUiLanguageRequest implements CreateRequest<UiLanguage>, ApiRequest {

    private List<String> isoCode = new ArrayList<>();

    @Override
    public List<UiLanguage> list() {
        return isoCode.stream().map(name -> new UiLanguage(null, name)).collect(Collectors.toList());
    }

    public CreateUiLanguageRequest() {
    }

    public CreateUiLanguageRequest(List<String> isoCode) {
        this.isoCode = isoCode;
    }

    public List<String> getIsoCode() {
        return isoCode;
    }

    @Override
    public boolean validated() {
        return isoCode.stream().noneMatch(name -> name == null || name.trim().isEmpty());
    }

    @Override
    public Wrapper<UiLanguage> fetchResponseWrapper() {
        return new UiLanguageWrapper();
    }
}
