package co.com.pragma.usecase.exception;

public class BusinessException extends RuntimeException {

    public BusinessException(String message){
        super(message);
    }
}
