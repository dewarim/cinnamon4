package com.dewarim.cinnamon.model.request.language;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.LanguageWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "updateLanguageRequest")
public class UpdateLanguageRequest implements UpdateRequest<Language>, ApiRequest {

    private List<Language> languages = new ArrayList<>();

    @Override
    public List<Language> list() {
        return languages;
    }

    public UpdateLanguageRequest() {
    }

    public UpdateLanguageRequest(Long id, String name) {
        languages.add(new Language(id,name));
    }

    public UpdateLanguageRequest(List<Language> Languages) {
        this.languages = Languages;
    }

    public List<Language> getLanguages() {
        return languages;
    }

    @Override
    public boolean validated() {
        return languages.stream().allMatch(Language ->
            Language != null && Language.getIsoCode() != null && !Language.getIsoCode().trim().isEmpty()
                    && Language.getId() != null && Language.getId() > 0);
    }

    @Override
    public Wrapper<Language> fetchResponseWrapper() {
        return new LanguageWrapper();
    }
}
