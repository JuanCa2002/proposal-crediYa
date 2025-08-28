package co.com.pragma.usecase.proposal.exception;

import co.com.pragma.usecase.exception.BusinessException;
import co.com.pragma.usecase.proposal.constants.ProposalMessageConstants;

import java.text.MessageFormat;

public class InitialStateNotFound extends BusinessException {

    public InitialStateNotFound(String name) {
        super(MessageFormat.format(ProposalMessageConstants.INITIAL_STATE_NOT_FOUND, name));
    }
}
