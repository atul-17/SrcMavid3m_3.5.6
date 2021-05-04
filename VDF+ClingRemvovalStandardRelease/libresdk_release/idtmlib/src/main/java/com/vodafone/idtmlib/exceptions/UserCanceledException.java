package com.vodafone.idtmlib.exceptions;

/**
 * User has canceled the ID Gateway request (eg: pressed back, clicked on Cancel)
 */
public class UserCanceledException extends IDTMException {
    public UserCanceledException(String errorDescription) {
        super(errorDescription);
    }
}
