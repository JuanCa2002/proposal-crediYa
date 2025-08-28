package co.com.pragma.usecase.proposal.exception;

import co.com.pragma.usecase.exception.NotFoundException;
import co.com.pragma.usecase.proposal.constants.ProposalMessageConstants;

import java.text.MessageFormat;

public class ProposalTypeByIdNotFoundException extends NotFoundException {

    public ProposalTypeByIdNotFoundException(Long id) {
        super(MessageFormat.format(ProposalMessageConstants.PROPOSAL_TYPE_NOT_FOUND, id));
    }
}
