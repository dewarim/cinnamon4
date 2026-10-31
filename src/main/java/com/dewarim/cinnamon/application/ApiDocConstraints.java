package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import jakarta.validation.groups.Default;
import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.ContainerElementTypeDescriptor;
import jakarta.validation.metadata.PropertyDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The constraint section of an api.md endpoint: what a client has to satisfy for a request to reach the
 * business logic. Read from the live constraint metadata of the validator that checks the requests, so
 * the generated documentation cannot describe a contract the server does not enforce.
 * <p>
 * Which constraints apply depends on the validation groups, so this works from a request instance
 * rather than from its class - see {@link ApiRequest#validationGroups()}.
 */
public final class ApiDocConstraints {

    /**
     * The three attributes every constraint annotation carries. They say how a violation is reported
     * and when it is checked, not what the client has to send, so they are left out.
     */
    private static final Set<String> COMMON_ATTRIBUTES = Set.of("message", "groups", "payload");

    /**
     * Guards against a model that cascades into itself. Nothing in the API nests nearly this deep, so
     * hitting the limit means a cycle the path-local visited set did not catch.
     */
    private static final int MAX_DEPTH = 5;

    private ApiDocConstraints() {
    }

    /**
     * The constraint section for one endpoint, as Markdown, or an empty String if the request has
     * nothing to check. Public for ApiDocConstraintsTest, which checks that two runs of the generator
     * produce the same document - the constraint metadata arrives in unordered sets.
     */
    public static String render(ApiRequest<?> request) {
        Set<Class<?>> groups = new HashSet<>();
        groups.add(Default.class);
        groups.addAll(Arrays.asList(request.validationGroups()));

        List<String> rows = new ArrayList<>();
        collect(request.getClass(), "", groups, new HashSet<>(), 0, rows);

        StringBuilder rendered = new StringBuilder();
        if (!rows.isEmpty()) {
            rendered.append("\n### Constraints\n\n")
                    .append("| field | constraints |\n|---|---|\n")
                    .append(String.join("\n", rows))
                    .append("\n");
        }
        String additionalRules = request.additionalRules();
        if (!additionalRules.isBlank()) {
            rendered.append("\n**Also:** ").append(additionalRules).append("\n");
        }
        return rendered.toString();
    }

    /**
     * One row per constrained field, depth first: a field before the fields of the object it carries.
     * Every set the metadata hands out is unordered, so each level is sorted - two runs of the
     * generator have to produce the same document.
     */
    private static void collect(Class<?> beanClass, String prefix, Set<Class<?>> groups,
                                Set<Class<?>> visited, int depth, List<String> rows) {
        if (depth > MAX_DEPTH || !visited.add(beanClass)) {
            return;
        }
        BeanDescriptor bean = RequestValidator.describe(beanClass);
        bean.getConstrainedProperties().stream()
                .sorted(Comparator.comparing(PropertyDescriptor::getPropertyName))
                .forEach(property -> {
                    String path = prefix + property.getPropertyName();
                    addRow(rows, path, property.getConstraintDescriptors(), groups);
                    // @Valid on the field itself: the constraints of the object it holds apply too
                    if (property.isCascaded()) {
                        collect(property.getElementClass(), path + ".", groups, visited, depth + 1, rows);
                    }
                    // and @Valid or a constraint on the element type of a List/Set/Map
                    property.getConstrainedContainerElementTypes().stream()
                            .sorted(Comparator.comparing(ContainerElementTypeDescriptor::getTypeArgumentIndex))
                            .forEach(element -> {
                                String elementPath = path + "[]";
                                addRow(rows, elementPath, element.getConstraintDescriptors(), groups);
                                if (element.isCascaded()) {
                                    collect(element.getElementClass(), elementPath + ".", groups, visited, depth + 1, rows);
                                }
                            });
                });
        // path-local, so a model carried by two different fields is documented under both
        visited.remove(beanClass);
    }

    private static void addRow(List<String> rows, String path, Collection<ConstraintDescriptor<?>> constraints,
                               Set<Class<?>> groups) {
        String applicable = constraints.stream()
                .filter(constraint -> appliesTo(constraint, groups))
                .map(ApiDocConstraints::describe)
                .sorted()
                .collect(Collectors.joining(", "));
        if (!applicable.isEmpty()) {
            rows.add("| `" + path + "` | " + applicable + " |");
        }
    }

    /**
     * A constraint without a groups attribute belongs to the Default group, which is always validated;
     * one scoped to CreateRequest or UpdateRequest only counts for the endpoints that declare it.
     */
    private static boolean appliesTo(ConstraintDescriptor<?> constraint, Set<Class<?>> groups) {
        return constraint.getGroups().stream().anyMatch(groups::contains);
    }

    private static String describe(ConstraintDescriptor<?> constraint) {
        Annotation annotation  = constraint.getAnnotation();
        String     attributes  = Arrays.stream(annotation.annotationType().getDeclaredMethods())
                .filter(method -> method.getParameterCount() == 0)
                .filter(method -> !COMMON_ATTRIBUTES.contains(method.getName()))
                .sorted(Comparator.comparing(Method::getName))
                .map(method -> attribute(method, constraint.getAttributes().get(method.getName())))
                .flatMap(Optional::stream)
                .collect(Collectors.joining(", "));
        String     name        = "@" + annotation.annotationType().getSimpleName();
        return attributes.isEmpty() ? "`" + name + "`" : "`" + name + "(" + attributes + ")`";
    }

    /**
     * An attribute of a constraint, left out while it still has its default value: the min of a
     * {@code @Size(min = 1)} is worth documenting, its max of Integer.MAX_VALUE is not.
     */
    private static Optional<String> attribute(Method attribute, Object value) {
        if (value == null || Objects.deepEquals(value, attribute.getDefaultValue())) {
            return Optional.empty();
        }
        return Optional.of(attribute.getName() + " = " + format(value));
    }

    private static String format(Object value) {
        if (value instanceof Object[] array) {
            return Arrays.stream(array).map(ApiDocConstraints::format)
                    .collect(Collectors.joining(", ", "[", "]"));
        }
        if (value instanceof Enum<?> constant) {
            return constant.name();
        }
        if (value instanceof String text) {
            // a pipe would end the table cell, even inside a code span
            return "\"" + text.replace("|", "\\|") + "\"";
        }
        return String.valueOf(value);
    }
}
