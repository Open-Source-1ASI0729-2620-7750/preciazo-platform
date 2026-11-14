package com.preciazo.platform.shared;

import com.preciazo.platform.shared.application.result.Result;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.assertj.core.api.Assertions.assertThat;

class ResultTest {
    @Test void composesSuccessfulCommandsAndMapsTheirValues() {
        Result<Integer, String> initial = Result.success(3);
        var result = initial.map(n -> n * 2).flatMap(n -> Result.success(n + 1));
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.toOptional()).contains(7);
    }
    @Test void failureShortCircuitsSuccessFunctionsAndCanRecover() {
        AtomicBoolean invoked = new AtomicBoolean();
        Result<Integer, String> initial = Result.failure("rejected");
        var result = initial.map(n -> { invoked.set(true); return n + 1; })
            .flatMap(n -> { invoked.set(true); return Result.success(n); });
        assertThat(invoked).isFalse();
        assertThat(result.isFailure()).isTrue();
        assertThat(result.toOptional()).isEmpty();
        assertThat(result.getOrElse(10)).isEqualTo(10);
        assertThat(result.mapError(String::length)).isEqualTo(Result.failure(8));
        assertThat(result.recover(error -> Result.success(42)).toOptional()).contains(42);
    }
}
