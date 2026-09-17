package vn.iotstar.controller.graphql;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
class CatalogGraphqlExceptionHandler {
    @GraphQlExceptionHandler
    GraphQLError handleCatalogException(CatalogGraphqlException exception,
                                         DataFetchingEnvironment environment) {
        return GraphqlErrorBuilder.newError(environment)
                .errorType(exception.getErrorType())
                .message(exception.getMessage())
                .build();
    }
}
