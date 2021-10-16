package com.dewarim.cinnamon.model.request.uiLanguage;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.UiLanguage;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.UiLanguageWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "updateUiLanguageRequest")
public class UpdateUiLanguageRequest implements UpdateRequest<UiLanguage>, ApiRequest {

    private List<UiLanguage> languages = new ArrayList<>();

    @Override
    public List<UiLanguage> list() {
        return languages;
    }

    public UpdateUiLanguageRequest() {
    }

    public UpdateUiLanguageRequest(Long id, String name) {
        languages.add(new UiLanguage(id,name));
    }

    public UpdateUiLanguageRequest(List<UiLanguage> UiLanguages) {
        this.languages = UiLanguages;
    }

    public List<UiLanguage> getLanguages() {
        return languages;
    }

    @Override
    public boolean validated() {
        return languages.stream().allMatch(UiLanguage ->
            UiLanguage != null && UiLanguage.getIsoCode() != null && !UiLanguage.getIsoCode().trim().isEmpty()
                    && UiLanguage.getId() != null && UiLanguage.getId() > 0);
    }

    @Override
    public Wrapper<UiLanguage> fetchResponseWrapper() {
        return new UiLanguageWrapper();
    }
}
