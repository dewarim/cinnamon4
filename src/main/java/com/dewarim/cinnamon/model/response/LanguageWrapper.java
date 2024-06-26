package com.dewarim.cinnamon.model.response;


import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.Language;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class LanguageWrapper extends BaseResponse implements Wrapper<Language>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "languages")
    @JacksonXmlProperty(localName = "language")
    List<Language> languages = new ArrayList<>();

    public List<Language> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Language> Languages) {
        this.languages = Languages;
    }

    @Override
    public List<Language> list() {
        return getLanguages();
    }

    @Override
    public Wrapper<Language> setList(List<Language> languages) {
        setLanguages(languages);
        return this;
    }

    @Override
    public List<Object> examples() {
        LanguageWrapper languageWrapper = new LanguageWrapper();
        languageWrapper.getLanguages().add(new Language(54L, "DOG"));
        return List.of(languageWrapper);
    }
}
