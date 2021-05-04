package com.vodafone.idtmlib.lib.network;

public class BadStatusCodeException extends Exception {
    private Response response;

    public BadStatusCodeException(Response response) {
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }
}
