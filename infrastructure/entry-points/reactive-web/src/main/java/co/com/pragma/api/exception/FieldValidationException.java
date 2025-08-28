package co.com.pragma.api.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FieldValidationException extends RuntimeException{

    private List<String> messages;

}
