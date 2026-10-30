package com.dewarim.cinnamon.test.unit;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.request.DeleteRequest;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every create, update and delete request has to reject an empty payload.
 * <p>
 * The hand-written validated() methods mostly checked the payload with allMatch or noneMatch, and
 * both are vacuously true for an empty collection - so a request with an empty body passed
 * validation and was handed to the servlet. The {@code @NotEmpty} annotations say what was meant.
 * This test walks UrlMapping rather than a hand-kept list, so a request added later cannot quietly
 * reopen the hole.
 */
public class EmptyWriteRequestTest {

    private static final Map<Class<?>, Object> PRIMITIVE_DEFAULTS = Map.of(
            boolean.class, false, long.class, 0L, int.class, 0, double.class, 0d,
            float.class, 0f, short.class, (short) 0, byte.class, (byte) 0, char.class, '\0');

    private Set<Class<? extends ApiRequest<?>>> writeRequests() {
        Set<Class<? extends ApiRequest<?>>> classes = new LinkedHashSet<>();
        for (UrlMapping mapping : UrlMapping.values()) {
            Class<? extends ApiRequest<?>> requestClass = mapping.getRequestClass();
            if (requestClass != null && requestClass.isRecord()
                    && (CreateRequest.class.isAssignableFrom(requestClass)
                    || UpdateRequest.class.isAssignableFrom(requestClass)
                    || DeleteRequest.class.isAssignableFrom(requestClass))) {
                classes.add(requestClass);
            }
        }
        return classes;
    }

    /**
     * A request built from its canonical constructor with no values at all. Compact constructors
     * turn a null payload into an empty collection, so this is the empty request a client would
     * send; where they do not, the payload stays null, which the same annotation rejects.
     */
    private Object emptyInstance(Class<?> requestClass) throws Exception {
        Class<?>[] types = Arrays.stream(requestClass.getRecordComponents())
                .map(RecordComponent::getType).toArray(Class<?>[]::new);
        Object[] args = Arrays.stream(types).map(PRIMITIVE_DEFAULTS::get).toArray();
        return requestClass.getDeclaredConstructor(types).newInstance(args);
    }

    @Test
    public void anEmptyWriteRequestIsRejected() throws Exception {
        Set<Class<? extends ApiRequest<?>>> requests = writeRequests();
        assertTrue(requests.size() > 40, "expected the create/update/delete requests, found " + requests.size());

        List<String> accepted = new ArrayList<>();
        for (Class<? extends ApiRequest<?>> requestClass : requests) {
            Object   request        = emptyInstance(requestClass);
            Method   validateMethod = requestClass.getMethod("validateRequest");
            Object   result;
            try {
                result = validateMethod.invoke(request);
            } catch (InvocationTargetException invocation) {
                if (invocation.getCause() instanceof FailedRequestException failure
                        && failure.getErrorCode() == ErrorCode.INVALID_REQUEST) {
                    continue;
                }
                throw invocation;
            }
            if (((Optional<?>) result).isPresent()) {
                accepted.add(requestClass.getSimpleName());
            }
        }
        assertTrue(accepted.isEmpty(), "these requests accept an empty payload: " + accepted);
    }
}
