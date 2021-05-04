package com.vodafone.idtmlib;

/**
 * Must be implemented in the extended Application class
 */
public interface IdtmLibInjector {
    /**
     * This method is used internally by the lib.
     *
     * @return The IdtmLib instance which should be created in the onCreate() method of the
     * Application class
     */
    IdtmLib getIdtmLib();
}
