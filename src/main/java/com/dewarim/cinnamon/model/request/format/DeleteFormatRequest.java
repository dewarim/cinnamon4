package com.dewarim.cinnamon.model.request.format;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "deleteFormatRequest")
public class DeleteFormatRequest extends DeleteByIdRequest<Format> implements ApiRequest<DeleteFormatRequest> {

    public DeleteFormatRequest() {
    }

    public DeleteFormatRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteFormatRequest(Long id) {
        super(id);
    }

    @Override
    public List<ApiRequest<DeleteFormatRequest>> examples() {
        return List.of(new DeleteFormatRequest(999L));
    }
}
