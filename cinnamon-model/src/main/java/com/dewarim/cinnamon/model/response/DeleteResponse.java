package com.dewarim.cinnamon.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Collections;
import java.util.List;

// not the most beautiful class, but there are advantages to adhere to the Wrapper-interface.
@JacksonXmlRootElement(localName = "cinnamon")
public class DeleteResponse implements Wrapper<DeleteResponse> {

    private boolean success;

    public DeleteResponse() {
    }

    @Override
    public List<DeleteResponse> list() {
        return Collections.singletonList(this);
    }

    @Override
    public Wrapper<DeleteResponse> setList(List<DeleteResponse> deleteResponses) {
        if(deleteResponses.size() != 1){
            throw new IllegalArgumentException("Invalid number of deleteResponses");
        }
        this.success = deleteResponses.get(0).success;
        return this;
    }

    public DeleteResponse(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "DeleteResponse{" +
                "success=" + success +
                '}';
    }

    public boolean isSuccess() {
        return success;
    }
}
