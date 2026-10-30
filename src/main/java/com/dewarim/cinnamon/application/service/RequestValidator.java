package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.application.RequestScope;
import com.dewarim.cinnamon.dao.UiLanguageDao;
import com.dewarim.cinnamon.model.UiLanguage;
import com.dewarim.cinnamon.model.UserAccount;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.groups.Default;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.hibernate.validator.spi.messageinterpolation.LocaleResolver;
import org.hibernate.validator.spi.messageinterpolation.LocaleResolverContext;

import java.util.Locale;
import java.util.Map;
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
 * Messages are rendered in the current user's uiLanguage when that language has default constraint
 * messages, falling back to English.
 */
public final class RequestValidator {

    private static final Logger log = LogManager.getLogger(RequestValidator.class);

    /**
     * Locales to render constraint messages in. Hibernate Validator ships default messages for these,
     * and declaring them lets it pre-load the bundles.
     * <p>
     * SPIKE: hardcoded. Should be derived from the iso codes in the ui_languages table, which cannot
     * be read while this class initializes.
     */
    private static final Set<Locale> SUPPORTED_LOCALES = Set.of(Locale.ENGLISH, Locale.GERMAN);

    private static final Map<Long, Locale> LOCALE_BY_UI_LANGUAGE_ID = new ConcurrentHashMap<>();

    private RequestValidator() {
    }

    /**
     * A ValidatorFactory is expensive to build and thread-safe once built, so it is built once and
     * lazily - a request record must not pay for it during class initialization.
     */
    private static final class Holder {
        private static final ValidatorFactory FACTORY = Validation.byProvider(HibernateValidator.class)
                .configure()
                /*
                 * No Expression Language implementation is on the classpath, on purpose: this
                 * interpolator substitutes {parameters} and never evaluates EL, which keeps the EL
                 * injection class (CVE-2020-10693, CVE-2025-35036) out of the request path. Leaving
                 * the default interpolator in place would fail at this line with
                 * NoClassDefFoundError: jakarta/el/ELManager rather than silently.
                 */
                .messageInterpolator(new ParameterMessageInterpolator())
                .localeResolver(new CurrentUserLocaleResolver())
                .locales(SUPPORTED_LOCALES)
                .defaultLocale(Locale.ENGLISH)
                .buildValidatorFactory();
    }

    /**
     * Validate the annotations of a request. The Default group is always included, so constraints
     * without a group are always checked; pass CreateRequest.class or UpdateRequest.class to select
     * the constraints that apply only when creating or only when updating.
     *
     * @throws FailedRequestException INVALID_REQUEST, listing every violation, if there is one
     */
    public static void validate(Object request, Class<?>... groups) {
        Class<?>[] allGroups = new Class<?>[groups.length + 1];
        allGroups[0] = Default.class;
        System.arraycopy(groups, 0, allGroups, 1, groups.length);

        Set<ConstraintViolation<Object>> violations = Holder.FACTORY.getValidator().validate(request, allGroups);
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
     * Render messages in the language the current user chose, so a validation error is readable for
     * the person who caused it. Falls back to the default locale when there is no user (the request
     * failed before authentication, or validation is being called from a test), when the user has no
     * uiLanguage, or when that language has no default constraint messages.
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
                    .map(isoCode -> Locale.forLanguageTag(isoCode.toLowerCase(Locale.ROOT)))
                    .filter(locale -> context.getSupportedLocales().contains(locale))
                    .orElse(context.getDefaultLocale());
            LOCALE_BY_UI_LANGUAGE_ID.put(uiLanguageId, resolved);
            return resolved;
        }
    }
}
