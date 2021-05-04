package com.vodafone.idtmlib.lib.network;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.ResponseBody;

public abstract class Response {
    private int responseCode;
    private String responseMessage;
    private Headers responseHeaders;
    private ResponseBody responseErrorBody;

    public Response() { }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public Headers getResponseHeaders() {
        return responseHeaders;
    }

    public ResponseBody getResponseErrorBody() {
        return responseErrorBody;
    }

    public void setBaseResponse(final retrofit2.Response response) {
        this.responseCode = response.code();
        this.responseMessage = response.message();
        setResponseHeaders(response.headers());
        this.responseErrorBody = response.errorBody();
    }

    public boolean isSuccess() {
        return getResponseCode() >= 200 && getResponseCode() < 400;
    }

    public String getResponseErrorBodyString() {
        String responseErrorBodyString = null;
        if (responseErrorBody != null) {
            try {
                return responseErrorBody.string();
            } catch (IOException e) {
                // just return null on error
            }
        }
        return responseErrorBodyString;
    }

    public String getJsonString() {
        return new Gson().toJson(this);
    }

    public static <T extends Response> T retrieve(final Class<T> cls, retrofit2.Call<T> call)
            throws IOException, BadStatusCodeException {
        T responseData;
        retrofit2.Response<T> response = call.execute();
        if (response.isSuccessful()) {
            responseData = response.body();
        } else {
            try {
                responseData = cls.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException("Cannot create the class", e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot create the class", e);
            }
        }
        responseData.setBaseResponse(response);
        if (response.isSuccessful()) {
            return responseData;
        } else {
            throw new BadStatusCodeException(responseData);
        }
    }

    private void setResponseHeaders(Headers responseHeaders) {
        this.responseHeaders = responseHeaders;
    }
}
