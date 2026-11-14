package com.preciazo.platform.shared.application.result;

/** Typed application failure for query contracts that return Optional/List rather than Result. */
public final class ApplicationErrorException extends RuntimeException {
    private final ApplicationError error;
    public ApplicationErrorException(ApplicationError error) { super(error.code()); this.error = error; }
    public ApplicationError error() { return error; }
}
