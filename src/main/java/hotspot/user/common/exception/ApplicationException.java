package hotspot.user.common.exception;

import hotspot.user.common.exception.code.BaseErrorCode;

public class ApplicationException extends BaseException {

    public ApplicationException(BaseErrorCode code) {
        super(code);
    }
}
