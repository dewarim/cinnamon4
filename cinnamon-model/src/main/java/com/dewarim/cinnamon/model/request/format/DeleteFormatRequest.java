package com.dewarim.cinnamon.model.request.format;

import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;

import java.util.List;

public class DeleteFormatRequest extends DeleteByIdRequest<Format> {

    public DeleteFormatRequest() {
    }

    public DeleteFormatRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteFormatRequest(Long id) {
        super(id);
    }
}
