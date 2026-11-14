package com.preciazo.platform.shared;

import com.preciazo.platform.shared.application.result.ApplicationError;
import com.preciazo.platform.shared.application.result.Result;
import com.preciazo.platform.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;
import com.preciazo.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.assertj.core.api.Assertions.assertThat;

class ProblemDetailTest {
    private ProblemDetailFromApplicationErrorAssembler assembler() {
        var source = new ResourceBundleMessageSource(); source.setBasename("messages"); source.setDefaultEncoding("UTF-8");
        return new ProblemDetailFromApplicationErrorAssembler(source);
    }
    @ParameterizedTest @CsvSource({"VALIDATION_ERROR,400", "LIST_NOT_FOUND,404", "LIST_CONFLICT,409", "BUSINESS_RULE_VIOLATION,422", "UNEXPECTED_ERROR,500", "UNKNOWN_CODE,500"})
    void mapsApplicationErrorFamilies(String code, int status) {
        var problem = assembler().toProblemDetailFromApplicationError(new ApplicationError(code, "fallback"), Locale.ENGLISH);
        assertThat(problem.getStatus()).isEqualTo(status);
        assertThat(problem.getProperties()).containsEntry("code", code);
        assertThat(problem.getType().toString()).startsWith("/api/v1/errors/");
    }
    @Test void localizesErrorsAndDoesNotExposeInternalFailures() {
        var assembler = assembler();
        var error = new ApplicationError("UNEXPECTED_ERROR", "database-password=secret", "SQL credentials");
        var problem = assembler.toProblemDetailFromApplicationError(error, Locale.forLanguageTag("es"));
        assertThat(problem.getDetail()).isEqualTo("Ocurrió un error interno.");
        assertThat(problem.getProperties()).doesNotContainKey("details");
        var invalid = assembler.toProblemDetailFromApplicationError(ApplicationError.validationError("email", "Invalid format"), Locale.ENGLISH);
        assertThat(invalid.getStatus()).isEqualTo(400);
        assertThat(invalid.getDetail()).isEqualTo("The request is invalid.");
    }
    @Test void failureResponseNeverInvokesTheResourceMapper() {
        var responses = new ResponseEntityAssembler(assembler());
        AtomicBoolean invoked = new AtomicBoolean();
        Result<String, ApplicationError> failed = Result.failure(ApplicationError.notFound("list", "7"));
        var response = responses.toResponseFromResult(failed, value -> { invoked.set(true); return value; }, HttpStatus.CREATED, Locale.ENGLISH);
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(invoked).isFalse();
        Result<String, ApplicationError> success = Result.success("domain value");
        var created = responses.toResponseFromResult(success, value -> new Resource(value.length()), HttpStatus.CREATED, Locale.ENGLISH);
        assertThat(created.getBody()).isEqualTo(new Resource(12));
    }
    record Resource(int length) { }
}
