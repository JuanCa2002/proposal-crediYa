package co.com.pragma.api.external.authentication.auth.exception;

import co.com.pragma.api.exception.UnauthorizedException;

public class InvalidTokenUnauthorizedException extends UnauthorizedException {

    public InvalidTokenUnauthorizedException(String message) {
        super(message);
    }
}
