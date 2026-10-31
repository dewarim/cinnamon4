package com.dewarim.cinnamon.test.unit;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.ApiDocConstraints;
import com.dewarim.cinnamon.model.request.folder.UpdateFolderRequest;
import com.dewarim.cinnamon.model.request.user.CreateUserAccountRequest;
import com.dewarim.cinnamon.model.request.user.UpdateUserAccountRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The validation contract as docs/api.md states it. The document is generated from the constraint
 * metadata of the validator that checks the requests, so it cannot describe a contract the server does
 * not enforce - but it can still leave part of the contract out, which is what this test guards.
 */
public class ApiDocConstraintsTest {

    private static final Map<Class<?>, Object> PRIMITIVE_DEFAULTS = Map.of(
            boolean.class, false, long.class, 0L, int.class, 0, double.class, 0d,
            float.class, 0f, short.class, (short) 0, byte.class, (byte) 0, char.class, '\0');

    private Set<Class<? extends ApiRequest<?>>> requestClasses() {
        Set<Class<? extends ApiRequest<?>>> classes = new LinkedHashSet<>();
        for (UrlMapping mapping : UrlMapping.values()) {
            if (mapping.getRequestClass() != null) {
                classes.add(mapping.getRequestClass());
            }
        }
        return classes;
    }

    /**
     * Same as the generator does it: neither validationGroups() nor additionalRules() depends on
     * instance state, so the component values do not matter.
     */
    private ApiRequest<?> newRequest(Class<? extends ApiRequest<?>> requestClass) throws Exception {
        try {
            return requestClass.getConstructor().newInstance();
        } catch (NoSuchMethodException noNoArgCtor) {
            Class<?>[] types = Arrays.stream(requestClass.getRecordComponents())
                    .map(RecordComponent::getType).toArray(Class<?>[]::new);
            Object[] args = Arrays.stream(types).map(PRIMITIVE_DEFAULTS::get).toArray();
            return requestClass.getDeclaredConstructor(types).newInstance(args);
        }
    }

    private boolean declaresValidated(Class<?> requestClass) {
        return Arrays.stream(requestClass.getDeclaredMethods())
                .anyMatch(method -> method.getName().equals("validated") && method.getParameterCount() == 0);
    }

    /**
     * A rule no annotation expresses is invisible to the generator, so the request has to state it in
     * prose. Without this, the next hand-written validated() method is a rule clients find out about by
     * being rejected.
     */
    @Test
    public void everyHandWrittenRuleIsDocumented() throws Exception {
        List<String> undocumented = new ArrayList<>();
        int          handWritten  = 0;
        for (Class<? extends ApiRequest<?>> requestClass : requestClasses()) {
            if (!declaresValidated(requestClass)) {
                continue;
            }
            handWritten++;
            if (newRequest(requestClass).additionalRules().isBlank()) {
                undocumented.add(requestClass.getSimpleName());
            }
        }
        assertTrue(handWritten >= 5, "expected the requests with hand-written rules, found " + handWritten);
        assertTrue(undocumented.isEmpty(),
                "these requests have a validated() rule that api.md does not mention: " + undocumented);
    }

    /**
     * The constraint metadata arrives in unordered sets, and the generated api.md is checked into the
     * repository - so an unsorted traversal would show up as a diff on every build.
     */
    @Test
    public void theConstraintSectionIsDeterministic() throws Exception {
        int documented = 0;
        for (Class<? extends ApiRequest<?>> requestClass : requestClasses()) {
            String first  = ApiDocConstraints.render(newRequest(requestClass));
            String second = ApiDocConstraints.render(newRequest(requestClass));
            assertEquals(first, second, "two runs disagree for " + requestClass.getSimpleName());
            if (!first.isEmpty()) {
                documented++;
            }
        }
        assertTrue(documented > 60, "expected most endpoints to document constraints, found " + documented);
    }

    /**
     * The point of validationGroups(): a create and an update of the same model do not demand the same
     * fields, and the documentation has to show the difference rather than the union.
     */
    @Test
    public void theDocumentedConstraintsAreScopedByGroup() {
        String create = ApiDocConstraints.render(new CreateUserAccountRequest());
        String update = ApiDocConstraints.render(new UpdateUserAccountRequest(null));

        // only a create has to carry a password, and UserAccountServlet keeps the stored one on update
        assertTrue(create.contains("| `userAccounts[].password` | `@NotEmpty` |"), create);
        assertFalse(update.contains("password"), update);
        // and only an update has to name the account it changes
        assertTrue(update.contains("| `userAccounts[].id` | `@NotNull`, `@Positive` |"), update);
        assertFalse(create.contains("userAccounts[].id"), create);
    }

    /**
     * The nested paths are the ones a violation is reported under, so a client can match the message
     * against the table.
     */
    @Test
    public void constraintsOfCarriedModelsAreDocumentedWithTheirPath() {
        String update = ApiDocConstraints.render(new UpdateFolderRequest());
        assertTrue(update.contains("| `folders` | `@NotEmpty` |"), update);
        assertTrue(update.contains("| `folders[].metasets[].typeId` |"), update);
    }
}
