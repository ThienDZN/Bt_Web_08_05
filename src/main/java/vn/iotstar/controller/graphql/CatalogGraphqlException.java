package vn.iotstar.controller.graphql;

import org.springframework.graphql.execution.ErrorType;

final class CatalogGraphqlException extends RuntimeException {
    private final ErrorType errorType;

    CatalogGraphqlException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    ErrorType getErrorType() {
        return errorType;
    }
}
