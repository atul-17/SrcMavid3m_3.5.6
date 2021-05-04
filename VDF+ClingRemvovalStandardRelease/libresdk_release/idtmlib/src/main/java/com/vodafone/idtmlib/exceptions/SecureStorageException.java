package com.vodafone.idtmlib.exceptions;


/**
 * Error generating secret keys or encrypting/decrypting data using the Android Keystore
 */
public class SecureStorageException extends IDTMException {
    public SecureStorageException(String errorDescription) {
        super(errorDescription);
    }
}
