package com.preciazo.platform.shared.interfaces.rest.transform;

import com.preciazo.platform.shared.application.result.ApplicationError;
import com.preciazo.platform.shared.application.result.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import java.util.Locale;
import java.util.function.Function;

/** An explicit Resource mapper prevents accidental serialization of domain internals. */
@Component
public final class ResponseEntityAssembler {
    private final ProblemDetailFromApplicationErrorAssembler errors;

    public ResponseEntityAssembler(ProblemDetailFromApplicationErrorAssembler errors) {
        this.errors = errors;
    }

    public <T, R> ResponseEntity<?> toResponseFromResult(Result<T, ApplicationError> result,
            Function<T, R> resourceMapper, HttpStatus successStatus, Locale locale) {
        return switch (result) {
            case Result.Success<T, ApplicationError> success ->
                ResponseEntity.status(successStatus).body(resourceMapper.apply(success.value()));
            case Result.Failure<T, ApplicationError> failure -> {
                var problem = errors.toProblemDetailFromApplicationError(failure.error(), locale);
                yield ResponseEntity.status(problem.getStatus()).body(problem);
            }
        };
    }
}
