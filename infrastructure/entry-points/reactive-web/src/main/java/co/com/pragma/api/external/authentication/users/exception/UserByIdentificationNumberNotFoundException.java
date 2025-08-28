package co.com.pragma.api.external.authentication.users.exception;

import co.com.pragma.usecase.exception.NotFoundException;

public class UserByIdentificationNumberNotFoundException extends NotFoundException {

    public UserByIdentificationNumberNotFoundException(String message){
        super(message);
    }
}
