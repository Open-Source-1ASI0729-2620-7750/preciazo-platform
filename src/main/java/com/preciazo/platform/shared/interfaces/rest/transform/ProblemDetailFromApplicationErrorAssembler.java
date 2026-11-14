package com.preciazo.platform.shared.interfaces.rest.transform;

import com.preciazo.platform.shared.application.result.ApplicationError;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.util.Locale;

/** Maps application outcomes to localized RFC 9457 responses. */
@Component
public final class ProblemDetailFromApplicationErrorAssembler {
    private final MessageSource messages;

    public ProblemDetailFromApplicationErrorAssembler(MessageSource messages) {
        this.messages = messages;
    }

    public ProblemDetail toProblemDetailFromApplicationError(ApplicationError error, Locale locale) {
        HttpStatus status = statusFor(error.code());
        String family = switch (status) {
            case BAD_REQUEST -> "validation";
            case UNAUTHORIZED -> "authentication";
            case FORBIDDEN -> "forbidden";
            case NOT_FOUND -> "not-found";
            case CONFLICT -> "conflict";
            case UNPROCESSABLE_ENTITY -> "business-rule";
            default -> "unexpected";
        };
        String fallback = messages.getMessage("error." + family + ".message", null, locale);
        String detail = status.is5xxServerError() ? fallback
            : messages.getMessage("error." + error.code() + ".message", null, fallback, locale);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        problem.setType(URI.create("/api/v1/errors/" + family));
        problem.setProperty("code", error.code());
        // Only application-controlled details may be returned; infrastructure errors remain generic.
        if (!status.is5xxServerError() && error.details() != null && !error.details().isBlank()) {
            problem.setProperty("details", error.details());
        }
        return problem;
    }

    private HttpStatus statusFor(String code) {
        if (code.equals("AUTHENTICATION_REQUIRED")) return HttpStatus.UNAUTHORIZED;
        if (code.equals("SHOPPING_FORBIDDEN")) return HttpStatus.FORBIDDEN;
        if (code.equals("VALIDATION_ERROR")) return HttpStatus.BAD_REQUEST;
        if (code.endsWith("_NOT_FOUND")) return HttpStatus.NOT_FOUND;
        if (code.endsWith("_CONFLICT")) return HttpStatus.CONFLICT;
        if (code.equals("BUSINESS_RULE_VIOLATION")) return HttpStatus.UNPROCESSABLE_ENTITY;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
