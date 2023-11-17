package com.dewarim.cinnamon.model.request.metasetType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "deleteMetasetTypeRequest")
public class DeleteMetasetTypeRequest extends DeleteByIdRequest<MetasetType> implements ApiRequest<DeleteMetasetTypeRequest> {

    public DeleteMetasetTypeRequest() {
    }

    public DeleteMetasetTypeRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteMetasetTypeRequest(Long id) {
        super(id);
    }

    @Override
    public List<ApiRequest<DeleteMetasetTypeRequest>> examples() {
        return List.of(new DeleteMetasetTypeRequest(7L));
    }
}
