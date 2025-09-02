package co.com.pragma.usecase.proposal.exception;

import co.com.pragma.usecase.exception.NotFoundException;
import co.com.pragma.usecase.proposal.constants.ProposalMessageConstants;

import java.math.BigInteger;
import java.text.MessageFormat;

public class ProposalByIdNotFoundException extends NotFoundException {
    public ProposalByIdNotFoundException(BigInteger id) {
        super(MessageFormat.format(ProposalMessageConstants.PROPOSAL_BY_ID_NOT_FOUND_EXCEPTION, id));
    }
}
