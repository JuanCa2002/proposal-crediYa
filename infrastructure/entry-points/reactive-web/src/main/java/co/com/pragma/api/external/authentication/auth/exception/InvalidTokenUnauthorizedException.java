package co.com.pragma.api.external.authentication.auth.exception;

import co.com.pragma.usecase.exception.UnauthorizedException;

public class InvalidTokenUnauthorizedException extends UnauthorizedException {

    public InvalidTokenUnauthorizedException(String message) {
        super(message);
    }
}
