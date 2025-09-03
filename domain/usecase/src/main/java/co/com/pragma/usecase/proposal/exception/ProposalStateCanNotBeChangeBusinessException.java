package co.com.pragma.usecase.proposal.exception;

import co.com.pragma.usecase.exception.BusinessException;
import co.com.pragma.usecase.proposal.constants.ProposalMessageConstants;

public class ProposalStateCanNotBeChangeBusinessException extends BusinessException {

    public ProposalStateCanNotBeChangeBusinessException() {
        super(ProposalMessageConstants.PROPOSAL_STATE_CAN_NOT_BE_CHANGE);
    }
}
