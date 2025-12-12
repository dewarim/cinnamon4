package com.dewarim.cinnamon.client;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.model.response.BaseResponse;
import com.dewarim.cinnamon.model.response.CinnamonContentType;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.core5.http.ContentType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.EXPECTED_SIZE_ANY;
import static com.dewarim.cinnamon.client.CinnamonClient.changeTriggerResponseLocal;

public class Unwrapper<T, W extends Wrapper<T>> {

    private final Class<W> clazz;

    public Unwrapper(Class<W> clazz) {
        this.clazz = clazz;
    }

    private List<T> checkList(List<T> list, Integer expectedSize) {
        if (expectedSize != null && expectedSize > EXPECTED_SIZE_ANY) {
            if (list == null || list.isEmpty()) {
                throw new CinnamonClientException("No objects found in response", ErrorCode.OBJECT_NOT_FOUND);
            }
            if (!expectedSize.equals(list.size())) {
                String message = String.format("Unexpected number of objects found: %s instead of %s ", list.size(), expectedSize);
                throw new CinnamonClientException(message);
            }
        }
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    public List<T> unwrap(StandardResponse response, Integer expectedSize) throws IOException {
        return unwrap(response, expectedSize, false);
    }

    public List<T> unwrap(StandardResponse response, Integer expectedSize, boolean ignoreError) throws IOException {
        try (response) {
            ContentType  contentType = ContentType.parseLenient(response.getEntity().getContentType());
            ObjectMapper mapper      = CinnamonContentType.getByHttpContentType(contentType.getMimeType()).getObjectMapper();
            if (!ignoreError) {
                CinnamonClient.checkResponseForErrors(response, mapper);
            }
            String content = new String(response.getEntity().getContent().readAllBytes());
            W      wrapper = mapper.readValue(content, clazz);
            if (wrapper instanceof BaseResponse) {
                changeTriggerResponseLocal.get().addAll(((BaseResponse) wrapper).getChangeTriggerResponses());
            }
            List<T> items = wrapper.list();
            return checkList(items, expectedSize);
        }
    }

}
