package com.dewarim.cinnamon.model.request.uiLanguage;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.UiLanguage;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.UiLanguageWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "updateUiLanguageRequest")
public class UpdateUiLanguageRequest implements UpdateRequest<UiLanguage>, ApiRequest<UpdateUiLanguageRequest> {

    @JacksonXmlElementWrapper(localName = "uiLanguages")
    @JacksonXmlProperty(localName = "uiLanguage")
    private List<UiLanguage> uiLanguages = new ArrayList<>();

    @Override
    public List<UiLanguage> list() {
        return uiLanguages;
    }

    public UpdateUiLanguageRequest() {
    }

    public UpdateUiLanguageRequest(Long id, String name) {
        uiLanguages.add(new UiLanguage(id,name));
    }

    public UpdateUiLanguageRequest(List<UiLanguage> UiLanguages) {
        this.uiLanguages = UiLanguages;
    }

    public List<UiLanguage> getUiLanguages() {
        return uiLanguages;
    }

    @Override
    public boolean validated() {
        return uiLanguages.stream().allMatch(UiLanguage ->
            UiLanguage != null && UiLanguage.getIsoCode() != null && !UiLanguage.getIsoCode().trim().isEmpty()
                    && UiLanguage.getId() != null && UiLanguage.getId() > 0);
    }

    @Override
    public Wrapper<UiLanguage> fetchResponseWrapper() {
        return new UiLanguageWrapper();
    }

    @Override
    public List<ApiRequest<UpdateUiLanguageRequest>> examples() {
        return List.of(new UpdateUiLanguageRequest(List.of(new UiLanguage(69L, "FR"), new UiLanguage(96L, "GR"))));
    }
}
