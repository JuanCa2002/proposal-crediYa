package co.com.pragma.usecase.proposal.exception;

import co.com.pragma.usecase.exception.NotFoundException;
import co.com.pragma.usecase.proposal.constants.ProposalMessageConstants;

import java.text.MessageFormat;

public class StateByNameNotFoundException extends NotFoundException {

    public StateByNameNotFoundException(String name) {
        super(MessageFormat.format(ProposalMessageConstants.STATE_BY_NAME_NOT_FOUND_EXCEPTION,name));
    }
}
