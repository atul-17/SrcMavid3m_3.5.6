package com.vodafone.idtmlib.exceptions;

/**
 * Generic IDTM/IDGW server error, should retry
 */
public class IdtmServerException extends IDTMException {

    public IdtmServerException(String errorDescription) {
        super(errorDescription);
    }
}
