package co.com.pragma.api.exception;

import co.com.pragma.api.dto.errors.ErrorResponse;
import co.com.pragma.usecase.exception.BusinessException;
import co.com.pragma.usecase.exception.NotFoundException;
import co.com.pragma.usecase.exception.UnauthorizedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler implements ServerAuthenticationEntryPoint, ServerAccessDeniedHandler {

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

        List<String> messages = Collections.emptyList();
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        String mainMessage = "Internal Server Error";

        if(error instanceof FieldValidationException) {
            messages = ((FieldValidationException) error).getMessages();
            httpStatus = HttpStatus.BAD_REQUEST;
            mainMessage = "Validation failed";
        } else if( error instanceof BusinessException){
            messages = List.of(error.getMessage());
            httpStatus = HttpStatus.BAD_REQUEST;
            mainMessage = "Business Rule Violation";
        } else if(error instanceof NotFoundException){
            messages = List.of(error.getMessage());
            httpStatus = HttpStatus.NOT_FOUND;
            mainMessage = "Resource Not Found";
        } else if (error instanceof UnauthorizedException) {
            messages = List.of(error.getMessage());
            httpStatus = HttpStatus.UNAUTHORIZED;
            mainMessage = "Not Authorized";
        }

        ErrorResponse response = ErrorResponse.builder()
                .code(httpStatus.value())
                .mainMessage(mainMessage)
                .messages(messages)
                .build();
        return ServerResponse.status(httpStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(response);

    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .mainMessage("Not Authorized")
                .messages(List.of("No se encuentra logueado"))
                .build();

        byte[] bytes = null;
        try {
            bytes = new ObjectMapper().writeValueAsBytes(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        ErrorResponse response = ErrorResponse.builder()
                .code(HttpStatus.FORBIDDEN.value())
                .mainMessage("Forbidden")
                .messages(List.of("No tiene permisos suficientes para llevar a cabo esta petición"))
                .build();

        byte[] bytes = null;
        try {
            bytes = new ObjectMapper().writeValueAsBytes(response);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }

        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
        );
    }
}
