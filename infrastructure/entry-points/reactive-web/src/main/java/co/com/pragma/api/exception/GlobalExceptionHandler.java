package co.com.pragma.api.exception;

import co.com.pragma.api.dto.errors.ErrorResponse;
import co.com.pragma.usecase.exception.BusinessException;
import co.com.pragma.usecase.exception.NotFoundException;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalExceptionHandler(ErrorAttributes errorAttributes, WebProperties.Resources resources,
                                  ApplicationContext applicationContext, ServerCodecConfigurer configurer) {
        super(errorAttributes, resources, applicationContext);
        this.setMessageReaders(configurer.getReaders());
        this.setMessageWriters(configurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request){
        Throwable error = getError(request);
        if(error instanceof FieldValidationException) {
            ErrorResponse response = ErrorResponse.builder()
                         .code(HttpStatus.BAD_REQUEST.value())
                         .mainMessage("Validation failed")
                         .messages(((FieldValidationException) error).getMessages())
                         .build();
            return ServerResponse.badRequest()
                    .bodyValue(response);
        } else if( error instanceof BusinessException){
            ErrorResponse response = ErrorResponse.builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .mainMessage("Business Rule Violation")
                    .messages(List.of(error.getMessage()))
                    .build();
            return ServerResponse.badRequest()
                    .bodyValue(response);
        } else if(error instanceof NotFoundException){
            ErrorResponse response = ErrorResponse.builder()
                    .code(HttpStatus.NOT_FOUND.value())
                    .mainMessage("Resource Not Found")
                    .messages(List.of(error.getMessage()))
                    .build();
            return ServerResponse.badRequest()
                    .bodyValue(response);
        } else {
            ErrorResponse response = ErrorResponse.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .mainMessage("Internal Server Error")
                    .messages(List.of(error.getMessage()))
                    .build();
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(response);
        }
    }
}
