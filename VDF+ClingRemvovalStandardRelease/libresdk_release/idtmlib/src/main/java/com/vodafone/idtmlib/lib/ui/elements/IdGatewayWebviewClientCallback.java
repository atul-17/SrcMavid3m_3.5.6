package com.vodafone.idtmlib.lib.ui.elements;

public interface IdGatewayWebviewClientCallback {
    void onRedirectResult(String code);
    void onUserCanceled(String error);
    void onUserFailedToLogin(String error);
    void onUserFailedInvalidScope(String error);
    void onAuthId(String authId);
}
