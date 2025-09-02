package co.com.pragma.usecase.proposal.exception;

import co.com.pragma.usecase.exception.UnauthorizedException;
import co.com.pragma.usecase.proposal.constants.ProposalMessageConstants;

public class UserLoginNotMatchUserRequestUnauthorizedException extends UnauthorizedException {
    public UserLoginNotMatchUserRequestUnauthorizedException() {
        super(ProposalMessageConstants.USER_NOT_MATCH_LOGIN_USER);
    }
}
