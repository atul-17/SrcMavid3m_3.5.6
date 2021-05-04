package com.vodafone.idtmlib.exceptions;

/**
 * Error retrieving the Google instance ID, could be a network error or an issue with
 * Google Play Services (should be enabled and updated)
 */
public class GoogleInstanceIdException extends IDTMException {
    public GoogleInstanceIdException(String errorDescription) {
        super(errorDescription);
    }
}
