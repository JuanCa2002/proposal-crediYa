package co.com.pragma.usecase.state.exception;

import co.com.pragma.usecase.exception.BusinessException;
import co.com.pragma.usecase.state.constants.StateMessageConstants;

import java.text.MessageFormat;

public class StateByNameAlreadyExistsBusinessException extends BusinessException {

    public StateByNameAlreadyExistsBusinessException(String name) {
        super(MessageFormat.format(StateMessageConstants.STATE_BY_NAME_ALREADY_EXISTS, name));
    }
}
