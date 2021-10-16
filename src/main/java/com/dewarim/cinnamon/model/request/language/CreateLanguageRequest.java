package com.dewarim.cinnamon.model.request.language;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.LanguageWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JacksonXmlRootElement(localName = "createLanguageRequest")
public class CreateLanguageRequest implements CreateRequest<Language>, ApiRequest {

    private List<String> isoCode = new ArrayList<>();

    @Override
    public List<Language> list() {
        return isoCode.stream().map(name -> new Language(null, name)).collect(Collectors.toList());
    }

    public CreateLanguageRequest() {
    }

    public CreateLanguageRequest(List<String> isoCode) {
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
    public Wrapper<Language> fetchResponseWrapper() {
        return new LanguageWrapper();
    }
}
