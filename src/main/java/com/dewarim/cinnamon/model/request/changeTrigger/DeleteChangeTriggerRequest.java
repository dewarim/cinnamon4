package com.dewarim.cinnamon.model.request.changeTrigger;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ChangeTrigger;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "deleteChangeTriggerRequest")
public class DeleteChangeTriggerRequest extends DeleteByIdRequest<ChangeTrigger> implements ApiRequest {

    public DeleteChangeTriggerRequest() {
    }

    public DeleteChangeTriggerRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteChangeTriggerRequest(Long id) {
        super(id);
    }
}
