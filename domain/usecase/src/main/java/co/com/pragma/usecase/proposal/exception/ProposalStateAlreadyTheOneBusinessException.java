package co.com.pragma.usecase.proposal.exception;

import co.com.pragma.usecase.exception.BusinessException;
import co.com.pragma.usecase.proposal.constants.ProposalMessageConstants;

import java.text.MessageFormat;

public class ProposalStateAlreadyTheOneBusinessException extends BusinessException {

    public ProposalStateAlreadyTheOneBusinessException(String stateName) {
        super(MessageFormat.format(ProposalMessageConstants.PROPOSAL_STATE_ALREADY_IS_THE_SELECTED_ONE, stateName));
    }
}
