package com.dewarim.cinnamon;

import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.List;

public class Unwrapper<T, W extends Wrapper<T>> {

    private XmlMapper mapper = new XmlMapper();

    private List<T> checkList(List<T> list, Integer expectedSize){
        if (expectedSize != null) {
            if (list == null || list.isEmpty()) {
                throw new CinnamonClientException("No objects found in response");
            }
            if (!expectedSize.equals(list.size())) {
                String message = String.format("Unexpected number of objects found: %s instead of %s ", list.size(), expectedSize);
                throw new CinnamonClientException(message);
            }
        }
        return list;
    }

    public List<T> unwrap(HttpResponse response, Integer expectedSize) throws IOException {
        List<T> items = mapper.readValue(response.getEntity().getContent(), new TypeReference<W>() {
        }).get();
        return checkList(items, expectedSize);
    }
}
