package com.dewarim.cinnamon.model.request.language;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "deleteLanguageRequest")
public class DeleteLanguageRequest extends DeleteByIdRequest<Language> implements ApiRequest<DeleteLanguageRequest> {

    public DeleteLanguageRequest() {
    }

    public DeleteLanguageRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteLanguageRequest(Long id) {
        super(id);
    }

    @Override
    public List<ApiRequest<DeleteLanguageRequest>> examples() {
        return List.of(new DeleteLanguageRequest(999L));
    }
}
