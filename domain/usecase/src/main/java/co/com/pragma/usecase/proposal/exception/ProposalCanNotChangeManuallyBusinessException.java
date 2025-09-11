package co.com.pragma.usecase.proposal.exception;

import co.com.pragma.usecase.exception.BusinessException;
import co.com.pragma.usecase.proposal.constants.ProposalMessageConstants;

public class ProposalCanNotChangeManuallyBusinessException extends BusinessException {
    public ProposalCanNotChangeManuallyBusinessException() {
        super(ProposalMessageConstants.PROPOSAL_CAN_CHANGE_STATE_MANUALLY);
    }
}
