package com.dewarim.cinnamon.model.request.language;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.LanguageWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JacksonXmlRootElement(localName = "createLanguageRequest")
public class CreateLanguageRequest implements CreateRequest<Language>, ApiRequest {

    @JacksonXmlElementWrapper(localName = "isoCodes")
    @JacksonXmlProperty(localName = "isoCode")
    private List<String> isoCodes = new ArrayList<>();

    @Override
    public List<Language> list() {
        return isoCodes.stream().map(name -> new Language(null, name)).collect(Collectors.toList());
    }

    public CreateLanguageRequest() {
    }

    public CreateLanguageRequest(List<String> isoCodes) {
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
    public Wrapper<Language> fetchResponseWrapper() {
        return new LanguageWrapper();
    }

    @Override
    public List<Object> examples() {
        return List.of(new CreateLanguageRequest(List.of("en", "de", "fr")));
    }
}
