package co.com.pragma.usecase.proposal.exception;

import co.com.pragma.usecase.exception.NotFoundException;
import co.com.pragma.usecase.proposal.constants.ProposalMessageConstants;

import java.text.MessageFormat;

public class StateByIdNotFoundException extends NotFoundException {

    public StateByIdNotFoundException(Integer id) {
        super(MessageFormat.format(ProposalMessageConstants.STATE_NOT_FOUND, id));
    }
}
