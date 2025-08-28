package co.com.pragma.usecase.proposaltype.exception;

import co.com.pragma.usecase.exception.BusinessException;
import co.com.pragma.usecase.proposaltype.constants.ProposalTypeMessageConstants;

public class MaximumHasToBeGreaterThanMinimumBusinessException extends BusinessException {

    public MaximumHasToBeGreaterThanMinimumBusinessException() {
        super(ProposalTypeMessageConstants.MAXIMUM_AMOUNT_INVALID);
    }
}
