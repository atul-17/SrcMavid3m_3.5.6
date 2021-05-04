package com.vodafone.idtmlib.exceptions;

/**
 * ID Gateway is required to get a new access token, but was not allowed to open the webview
 */
public class IdGatewayRequiredException extends IDTMException {
    public IdGatewayRequiredException(String errorDescription) {
        super(errorDescription);
    }
}
