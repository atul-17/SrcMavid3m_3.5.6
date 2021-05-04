package com.vodafone.idtmlib.lib.network.models.responses;

import android.text.TextUtils;

import com.vodafone.idtmlib.lib.network.Response;

public class ErrorResponse extends Response {
    private String error;
    private String description;
    private String transactionId;

    public String getError() {
        return error;
    }

    public String getDescription() {
        return description;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public boolean isSdkNotFound() {
        return TextUtils.equals(error, "SDK_NOT_FOUND");
    }
}
