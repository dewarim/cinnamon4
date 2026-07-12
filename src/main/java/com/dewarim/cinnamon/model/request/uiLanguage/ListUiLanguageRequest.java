package com.dewarim.cinnamon.model.request.uiLanguage;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.UiLanguage;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListType;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.UiLanguageWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("listUiLanguageRequest")
public record ListUiLanguageRequest(ListType type) implements DefaultListRequest, ListRequest<UiLanguage>, ApiRequest<ListUiLanguageRequest> {

    public ListUiLanguageRequest {
        if (type == null) {
            type = ListType.FULL;
        }
    }

    public ListUiLanguageRequest() {
        this(ListType.FULL);
    }

    @Override
    public Wrapper<UiLanguage> fetchResponseWrapper() {
        return new UiLanguageWrapper();
    }

    @Override
    public List<ApiRequest<ListUiLanguageRequest>> examples() {
        return List.of(new ListUiLanguageRequest());
    }
}
