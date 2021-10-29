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

@JacksonXmlRootElement(localName = "createLanguageRequest")
public class CreateLanguageRequest implements CreateRequest<Language>, ApiRequest {

    @JacksonXmlElementWrapper(localName = "languages")
    @JacksonXmlProperty(localName = "language")
    private List<Language> languages = new ArrayList<>();

    @Override
    public List<Language> list() {
        return languages;
    }

    public CreateLanguageRequest() {
    }

    public CreateLanguageRequest(String isoCode) {
        this.languages.add(new Language(isoCode));
    }

    public CreateLanguageRequest(List<Language> languages) {
        this.languages = languages;
    }

    public List<Language> getLanguages() {
        return languages;
    }

    @Override
    public boolean validated() {
        return languages.stream().noneMatch(language -> language == null ||
                language.getIsoCode() == null ||
                language.getIsoCode().trim().isEmpty());
    }

    @Override
    public Wrapper<Language> fetchResponseWrapper() {
        return new LanguageWrapper();
    }

    @Override
    public List<Object> examples() {
        return List.of(new CreateLanguageRequest(List.of(
                new Language("en"), new Language("de"), new Language("fr"))
        ));
    }
}
