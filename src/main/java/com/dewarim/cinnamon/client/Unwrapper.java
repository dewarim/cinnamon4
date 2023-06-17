package com.dewarim.cinnamon.client;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.dewarim.cinnamon.api.Constants.EXPECTED_SIZE_ANY;
import static com.dewarim.cinnamon.api.Constants.XML_MAPPER;

public class Unwrapper<T, W extends Wrapper<T>> {

    private static final Logger log = LogManager.getLogger(Unwrapper.class);

    private final XmlMapper mapper = XML_MAPPER;

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
        if(list == null){
            return new ArrayList<>();
        }
        return list;
    }

    public List<T> unwrap(StandardResponse response, Integer expectedSize) throws IOException {
        try (response) {
            CinnamonClient.checkResponseForErrors(response, mapper);
            String  content = new String(response.getEntity().getContent().readAllBytes());
            List<T> items   = mapper.readValue(content, clazz).list();
            return checkList(items, expectedSize);
        }
    }

}
