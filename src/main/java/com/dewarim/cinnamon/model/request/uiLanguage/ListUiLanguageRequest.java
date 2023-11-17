package com.dewarim.cinnamon.model.request.uiLanguage;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.UiLanguage;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.UiLanguageWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "listUiLanguageRequest")
public class ListUiLanguageRequest extends DefaultListRequest implements ListRequest<UiLanguage>, ApiRequest<ListUiLanguageRequest> {

    @Override
    public Wrapper<UiLanguage> fetchResponseWrapper() {
        return new UiLanguageWrapper();
    }

    @Override
    public List<ApiRequest<ListUiLanguageRequest>> examples() {
        return List.of(new ListUiLanguageRequest());
    }
}
