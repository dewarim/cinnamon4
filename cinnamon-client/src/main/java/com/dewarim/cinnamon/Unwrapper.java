package com.dewarim.cinnamon;

import com.dewarim.cinnamon.model.response.CinnamonError;
import com.dewarim.cinnamon.model.response.CinnamonErrorWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.api.Constants.*;
import static org.apache.http.HttpStatus.SC_OK;

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
                throw new CinnamonClientException("No objects found in response");
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

    public List<T> unwrap(HttpResponse response, Integer expectedSize) throws IOException {
        if (response.containsHeader(HEADER_FIELD_CINNAMON_ERROR)) {
            CinnamonErrorWrapper wrapper = mapper.readValue(response.getEntity().getContent(), CinnamonErrorWrapper.class);
            log.warn("Found errors: "+wrapper.getErrors().stream().map(CinnamonError::toString).collect(Collectors.joining(",")));
            throw new CinnamonClientException(wrapper);
        }
        if (response.getStatusLine().getStatusCode() != SC_OK) {
            StatusLine statusLine = response.getStatusLine();
            String     message    = statusLine.getStatusCode() + " " + statusLine.getReasonPhrase();
            log.warn("Failed to unwrap non-okay response with status: " + message);
            log.info("Response: " + new String(response.getEntity().getContent().readAllBytes()));
            throw new CinnamonClientException(message);
        }
        List<T> items = mapper.readValue(response.getEntity().getContent(), clazz).list();
        return checkList(items, expectedSize);
    }

}
