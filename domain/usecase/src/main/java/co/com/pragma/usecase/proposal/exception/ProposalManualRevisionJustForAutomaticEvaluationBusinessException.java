package co.com.pragma.usecase.proposal.exception;

import co.com.pragma.usecase.exception.BusinessException;
import co.com.pragma.usecase.proposal.constants.ProposalMessageConstants;

public class ProposalManualRevisionJustForAutomaticEvaluationBusinessException extends BusinessException {

    public ProposalManualRevisionJustForAutomaticEvaluationBusinessException() {
        super(ProposalMessageConstants.PROPOSAL_MANUAL_REVISION_JUST_FOR_AUTOMATIC_VALIDATION);
    }
}
