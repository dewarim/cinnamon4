package com.dewarim.cinnamon.model.request.configEntry;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ConfigEntry;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "deleteConfigRequest")
public class DeleteConfigEntryRequest extends DeleteByIdRequest<ConfigEntry> implements ApiRequest<DeleteByIdRequest<ConfigEntry>> {

    public DeleteConfigEntryRequest() {
    }

    public DeleteConfigEntryRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteConfigEntryRequest(Long id) {
        super(id);
    }

    @Override
    public List<ApiRequest<DeleteByIdRequest<ConfigEntry>>> examples() {
        return List.of(new DeleteConfigEntryRequest());
    }
}
