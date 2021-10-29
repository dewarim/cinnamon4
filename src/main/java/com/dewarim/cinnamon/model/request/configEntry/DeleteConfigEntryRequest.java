package com.dewarim.cinnamon.model.request.configEntry;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ConfigEntry;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;

import java.util.List;

public class DeleteConfigEntryRequest extends DeleteByIdRequest<ConfigEntry> implements ApiRequest {

    public DeleteConfigEntryRequest() {
    }

    public DeleteConfigEntryRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteConfigEntryRequest(Long id) {
        super(id);
    }
}
