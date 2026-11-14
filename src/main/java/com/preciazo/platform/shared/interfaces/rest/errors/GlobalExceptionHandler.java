package com.preciazo.platform.shared.interfaces.rest.errors;

import com.preciazo.platform.shared.application.result.ApplicationError;
import com.preciazo.platform.shared.application.result.ApplicationErrorException;
import com.preciazo.platform.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.net.URI;

/** Keeps request bodies, validation values and internal exception messages out of error responses. */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ProblemDetailFromApplicationErrorAssembler errors;
    private final MessageSource messages;

    public GlobalExceptionHandler(ProblemDetailFromApplicationErrorAssembler errors, MessageSource messages) {
        this.errors = errors;
        this.messages = messages;
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, @Nullable Object body,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ApplicationError error = switch (status.value()) {
            case 400 -> new ApplicationError("VALIDATION_ERROR", "Invalid request");
            case 404 -> new ApplicationError("RESOURCE_NOT_FOUND", "Resource not found");
            case 409 -> new ApplicationError("RESOURCE_CONFLICT", "Resource conflict");
            case 422 -> new ApplicationError("BUSINESS_RULE_VIOLATION", "Invalid operation");
            default -> new ApplicationError("UNEXPECTED_ERROR", "Request failure");
        };
        ProblemDetail problem = errors.toProblemDetailFromApplicationError(error, request.getLocale());
        // Preserve standard MVC statuses such as 405/415 without exposing framework exception details.
        if (!status.is5xxServerError() && problem.getStatus() != status.value()) {
            problem.setStatus(status.value());
            problem.setTitle(HttpStatus.valueOf(status.value()).getReasonPhrase());
            problem.setDetail(messages.getMessage("error.request.message", null, request.getLocale()));
            problem.setType(URI.create("/api/v1/errors/http-" + status.value()));
            problem.setProperty("code", "HTTP_" + status.value());
        }
        return new ResponseEntity<>(problem, headers, status);
    }

    @ExceptionHandler(ApplicationErrorException.class)
    public ResponseEntity<ProblemDetail> handleApplicationFailure(ApplicationErrorException exception, WebRequest request) {
        var problem = errors.toProblemDetailFromApplicationError(exception.error(), request.getLocale());
        var response = ResponseEntity.status(problem.getStatus());
        if (problem.getStatus() == 401) response.header(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        return response.body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedException(Exception exception, WebRequest request) {
        LOG.error("Unhandled request failure of type {}", exception.getClass().getSimpleName());
        var problem = errors.toProblemDetailFromApplicationError(
            new ApplicationError("UNEXPECTED_ERROR", "Internal error"), request.getLocale());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
