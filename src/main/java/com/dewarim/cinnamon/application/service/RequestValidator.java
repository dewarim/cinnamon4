package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.RequestScope;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.dao.UiLanguageDao;
import com.dewarim.cinnamon.model.UiLanguage;
import com.dewarim.cinnamon.model.UserAccount;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.groups.Default;
import jakarta.validation.metadata.BeanDescriptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.hibernate.validator.spi.messageinterpolation.LocaleResolver;
import org.hibernate.validator.spi.messageinterpolation.LocaleResolverContext;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Bean Validation for request objects: the annotations on a request record replace a hand-written
 * validated() method, and a failure reports which field was wrong instead of only that something was.
 * <p>
 * A violation is reported as ErrorCode.INVALID_REQUEST - the same code as before - with the list of
 * violations as the error message, which RequestResponseFilter already prefers over the error code's
 * generic description. Existing clients therefore see the same code and a more useful message.
 * <p>
 * Messages are rendered in the current user's uiLanguage. Hibernate Validator ships default messages
 * for 22 languages; a uiLanguage it has no messages for falls back to English.
 */
public final class RequestValidator {

    private static final Logger log = LogManager.getLogger(RequestValidator.class);

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    /**
     * uiLanguage id to the locale its messages are rendered in. Held until the server restarts or a
     * ui language is created, changed or deleted through the API - see invalidateUiLanguageCache.
     */
    private static final Map<Long, Locale> LOCALE_BY_UI_LANGUAGE_ID = new ConcurrentHashMap<>();

    /**
     * Built lazily, because the locales it has to support are read from the ui_languages table, and
     * dropped by invalidateUiLanguageCache so a newly added language is picked up. Volatile rather
     * than final: a validation on another thread may still be using the previous factory.
     */
    private static volatile ValidatorFactory factory;

    private RequestValidator() {
    }

    /**
     * Validate the annotations of a request. The Default group is always included, so constraints
     * without a group are always checked; which further groups apply is declared by the request itself
     * in {@link ApiRequest#validationGroups()} rather than passed in here, so that the api.md generator
     * documents the same set of constraints this method enforces.
     *
     * @throws FailedRequestException INVALID_REQUEST, listing every violation, if there is one
     */
    public static void validate(Object request) {
        validate(request, groupsOf(request));
    }

    /**
     * The groups a request declares. Anything that is not an ApiRequest - a model class validated on
     * its own, or a test calling in directly - is checked against the Default group only.
     */
    private static Class<?>[] groupsOf(Object request) {
        return request instanceof ApiRequest<?> apiRequest ? apiRequest.validationGroups() : new Class<?>[0];
    }

    /**
     * Constraint metadata for the api.md generator, read from the same validator that checks the
     * requests - so the generated documentation cannot describe a contract the server does not enforce.
     */
    public static BeanDescriptor describe(Class<?> beanClass) {
        return validator().getConstraintsForClass(beanClass);
    }

    private static void validate(Object request, Class<?>[] groups) {
        Class<?>[] allGroups = new Class<?>[groups.length + 1];
        allGroups[0] = Default.class;
        System.arraycopy(groups, 0, allGroups, 1, groups.length);

        Set<ConstraintViolation<Object>> violations = validator().validate(request, allGroups);
        if (violations.isEmpty()) {
            return;
        }
        // sorted so the message is stable: the violation set has no meaningful order
        String message = violations.stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        log.debug("{} failed validation: {}", request.getClass().getSimpleName(), message);
        throw new FailedRequestException(ErrorCode.INVALID_REQUEST, message);
    }

    /**
     * Forget which locale each ui language maps to, and rebuild the validator with the current set of
     * ui languages on next use. Must be called whenever a ui language is created, changed or deleted:
     * the locales a message can be rendered in are fixed when the validator is built, and asking it
     * for one it was not built with fails the validation with HV000250 "Uninitialized locale" instead
     * of reporting the actual constraint violation.
     */
    public static void invalidateUiLanguageCache() {
        synchronized (RequestValidator.class) {
            LOCALE_BY_UI_LANGUAGE_ID.clear();
            // Not closed: another thread may still hold a Validator from it. Dropping the reference
            // is enough - the factory only holds caches.
            factory = null;
        }
        log.debug("ui language cache invalidated, validator will be rebuilt on next use");
    }

    private static Validator validator() {
        ValidatorFactory current = factory;
        if (current == null) {
            synchronized (RequestValidator.class) {
                current = factory;
                if (current == null) {
                    current = buildFactory();
                    factory = current;
                }
            }
        }
        return current.getValidator();
    }

    private static ValidatorFactory buildFactory() {
        Set<Locale> locales = supportedLocales();
        log.debug("building request validator for locales {}", locales);
        return Validation.byProvider(HibernateValidator.class)
                .configure()
                /*
                 * The locale resolver has to be handed to the interpolator, not only to
                 * configure().localeResolver(): a custom interpolator does its own locale resolution,
                 * and the one configured on the factory is then never consulted - messages come out in
                 * the server's default locale instead of the user's.
                 *
                 * ParameterMessageInterpolator substitutes {parameters} without evaluating Expression
                 * Language. No EL implementation is on the classpath on purpose: that keeps the EL
                 * injection class (CVE-2020-10693, CVE-2025-35036) out of the request path and avoids a
                 * second EL implementation next to the one jetty-ee10-webapp can bring. The default
                 * interpolator would fail here with NoClassDefFoundError: jakarta/el/ELManager.
                 */
                .messageInterpolator(new ParameterMessageInterpolator(locales, DEFAULT_LOCALE,
                        new CurrentUserLocaleResolver(), true))
                .buildValidatorFactory();
    }

    /**
     * Every locale a message may have to be rendered in: English plus whatever the ui_languages table
     * offers. A language Hibernate Validator ships no messages for is harmless here - it resolves to
     * the English bundle - but a locale missing from this set is not, hence invalidateUiLanguageCache.
     */
    private static Set<Locale> supportedLocales() {
        Set<Locale> locales = new HashSet<>();
        locales.add(DEFAULT_LOCALE);
        if (!ThreadLocalSqlSession.hasSessionFactory()) {
            // --write-api builds a validator to read constraint metadata, which needs no messages at
            // all. Nothing to warn about: there is no database because none was asked for.
            log.debug("No database configured, constraint messages will be English only");
            return locales;
        }
        try {
            new UiLanguageDao().list().stream()
                    .map(UiLanguage::getIsoCode)
                    .map(RequestValidator::toLocale)
                    .flatMap(Optional::stream)
                    .forEach(locales::add);
        } catch (Exception failure) {
            // a configured database that cannot be reached yet: the first validation failure can happen
            // before it is up. English-only messages are a better outcome than losing the validation
            // error itself
            log.warn("Could not read ui_languages, constraint messages will be English only: {}", failure.toString());
        }
        return locales;
    }

    private static Optional<Locale> toLocale(String isoCode) {
        if (isoCode == null || isoCode.isBlank()) {
            return Optional.empty();
        }
        Locale locale = Locale.forLanguageTag(isoCode.trim().toLowerCase(Locale.ROOT));
        return locale.getLanguage().isEmpty() ? Optional.empty() : Optional.of(locale);
    }

    /**
     * Render messages in the language the current user chose, so a validation error is readable for
     * the person who caused it. Falls back to the default locale when there is no user (the request
     * failed before authentication, or validation is called outside a request), when the user has no
     * uiLanguage, or when the validator was not built for that locale.
     */
    private static class CurrentUserLocaleResolver implements LocaleResolver {

        @Override
        public Locale resolve(LocaleResolverContext context) {
            UserAccount user = RequestScope.getCurrentUser();
            if (user == null || user.getUiLanguageId() == null) {
                return context.getDefaultLocale();
            }
            Long   uiLanguageId = user.getUiLanguageId();
            Locale cached       = LOCALE_BY_UI_LANGUAGE_ID.get(uiLanguageId);
            if (cached != null) {
                return cached;
            }
            Locale resolved = new UiLanguageDao().getObjectById(uiLanguageId)
                    .map(UiLanguage::getIsoCode)
                    .flatMap(RequestValidator::toLocale)
                    // a locale this validator was not built for would fail interpolation with
                    // HV000250 and lose the real error, so fall back instead
                    .filter(locale -> context.getSupportedLocales().contains(locale))
                    .orElse(context.getDefaultLocale());
            LOCALE_BY_UI_LANGUAGE_ID.put(uiLanguageId, resolved);
            return resolved;
        }
    }
}
