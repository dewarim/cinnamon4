package com.dewarim.cinnamon.model.request.uiLanguage;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.UiLanguage;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "deleteUiLanguageRequest")
public class DeleteUiLanguageRequest extends DeleteByIdRequest<UiLanguage> implements ApiRequest<DeleteUiLanguageRequest> {

    public DeleteUiLanguageRequest() {
    }

    public DeleteUiLanguageRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteUiLanguageRequest(Long id) {
        super(id);
    }

    @Override
    public List<ApiRequest<DeleteUiLanguageRequest>> examples() {
        return List.of(new DeleteUiLanguageRequest(90L));
    }
}
