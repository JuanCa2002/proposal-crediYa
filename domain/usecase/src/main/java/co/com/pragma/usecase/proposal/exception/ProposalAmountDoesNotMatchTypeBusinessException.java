package co.com.pragma.usecase.proposal.exception;

import co.com.pragma.usecase.exception.BusinessException;
import co.com.pragma.usecase.proposal.constants.ProposalMessageConstants;

import java.text.MessageFormat;

public class ProposalAmountDoesNotMatchTypeBusinessException extends BusinessException {

    public ProposalAmountDoesNotMatchTypeBusinessException(String proposalName, Double minimum,
                                                           Double maximum) {
        super(MessageFormat.format(ProposalMessageConstants.PROPOSAL_TYPE_NOT_MATCH, proposalName, minimum, maximum));
    }
}
