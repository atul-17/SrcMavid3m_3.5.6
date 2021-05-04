package com.vodafone.idtmlib.lib.ui.elements;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;

import com.vodafone.idtmlib.lib.network.IdtmApi;
import com.vodafone.idtmlib.lib.utils.Printer;

import javax.inject.Inject;

import okhttp3.CertificatePinner;

public class IdGatewayWebViewClient extends CertificatePinnerWebViewClient {
    private static final String STATE_PARAM = "state";
    private static final String CODE_PARAM = "code";
    private static final String ERROR_PARAM = "error";
    private static final String ERROR__DESCRIPTION_PARAM = "error_description";
    private static final String ERROR_PARAM_ACCESS_DENIED = "access_denied";
    private static final String ERROR_USER_FAILED_TO_AUTHENTICATE_LOGIN_REQUIRED = "login_required";
    private static final String ERROR_USER_FAILED_TO_AUTHENTICATE_INVALID_SCOPE = "invalid_scope";
    private Printer printer;
    private Uri redirectUri;
    private String nonce;
    private String state;
    private IdGatewayWebviewClientCallback callback;

    @Inject
    public IdGatewayWebViewClient(Printer printer, CertificatePinner certificatePinner, IdtmApi idtmApi, Context c) {
        super(printer, certificatePinner, idtmApi, c);
        this.printer = printer;
    }

    public void setRedirectConditions(Uri redirectUri, String nonce, String state,
                                      IdGatewayWebviewClientCallback callback) {
        this.redirectUri = redirectUri;
        this.nonce = nonce;
        this.state = state;
        this.callback = callback;
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description,
                                String failingUrl) {
        printer.e("Error [dep] Description: ", description, ", ErrorCode: ", errorCode);
        callback.onRedirectResult(null);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request,
                                WebResourceError error) {
        printer.e("Error Description: ", error.getDescription(), ", ErrorCode: ", error.getErrorCode());
        if (request.isForMainFrame()) {
            callback.onRedirectResult(null);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request,
                                    WebResourceResponse errorResponse) {
        printer.e("HTTP Error status code: ", errorResponse.getStatusCode());
        if (request.isForMainFrame()) {
            callback.onRedirectResult(null);
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        printer.i("Loading url [dep]: ", url);
        return checkRedirectAuthorized(Uri.parse(Uri.decode(url)));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        printer.i("Loading url: ", request.getUrl());
        return checkRedirectAuthorized(request.getUrl());
    }

    private boolean checkRedirectAuthorized(Uri uri) {

        printer.d("Method called: checkRedirectAuthorized with URI ", uri.toString());

        // extract authId if possible
        if (uri != null) {
            String uriString = uri.toString();
            if (uriString != null && !uriString.isEmpty()) {
                int idx = uriString.indexOf("authorize#/trx/");
                if (idx > 0) {
                    String authId = uriString.substring(idx + "authorize#/trx/".length());
                    printer.d("AuthorizationId =  ", authId);
                    callback.onAuthId(authId);
                }
            }

        }

        if (redirectUri != null && !TextUtils.isEmpty(state) && !TextUtils.isEmpty(nonce) &&
                TextUtils.equals(redirectUri.getScheme(), uri.getScheme()) &&
                redirectUri.getPort() == uri.getPort() &&
                TextUtils.equals(redirectUri.getHost(), uri.getHost()) &&
                TextUtils.equals(redirectUri.getPath(), uri.getPath())) {
            String error = uri.getQueryParameter(ERROR_PARAM);
            String errorDescription = uri.getQueryParameter(ERROR__DESCRIPTION_PARAM);
            if (TextUtils.isEmpty(error)) {
                String uriState = uri.getQueryParameter(STATE_PARAM);
                String uriCode = uri.getQueryParameter(CODE_PARAM);
                if (TextUtils.equals(state, uriState)) {
                    if (TextUtils.isEmpty(uriCode)) {
                        printer.e("Redirect error: Missing Code parameter");
                        callback.onRedirectResult(null);
                    } else {
                        printer.i("Redirect successful with code: ", uriCode);
                        callback.onRedirectResult(uriCode);
                    }
                } else {
                    printer.e("Redirect error: State parameter not matching (expected: ", state,
                            ", received: " + uriState);
                    callback.onRedirectResult(null);
                }
            } else if (error.equalsIgnoreCase(ERROR_PARAM_ACCESS_DENIED)) {
                printer.e("Redirect error: ", error, ", Description 1: " + errorDescription);
                callback.onUserCanceled(error + ", " + errorDescription);
            } else if (error.equalsIgnoreCase(ERROR_USER_FAILED_TO_AUTHENTICATE_LOGIN_REQUIRED)) {
                printer.e("Redirect error: ", error, ", Description 2: " + errorDescription);
                callback.onUserFailedToLogin(error + ", " + errorDescription);
            } else if (error.equalsIgnoreCase(ERROR_USER_FAILED_TO_AUTHENTICATE_INVALID_SCOPE)) {
                printer.e("Redirect error: ", error, ", Description 3: " + errorDescription);
                callback.onUserFailedInvalidScope(error + ", " + errorDescription);
            } else {
                printer.e("Redirect error: ", error, ", Description 4 : " + errorDescription);
                callback.onRedirectResult(error + ", " + errorDescription);
            }
            return true;
        }
        return false;
    }
}