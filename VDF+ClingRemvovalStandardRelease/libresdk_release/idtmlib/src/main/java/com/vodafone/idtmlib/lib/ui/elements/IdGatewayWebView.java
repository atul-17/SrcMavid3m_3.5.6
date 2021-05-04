package com.vodafone.idtmlib.lib.ui.elements;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.util.Map;

import javax.inject.Inject;

public class IdGatewayWebView extends WebView {
    // TODO: keyboard overlap text fields inside the web view
    private static final String REDIRECT_URI_PARAM = "redirect_uri";
    private static final String STATE_PARAM = "state";
    public static final String NONCE_PARAM = "nonce";
    private IdGatewayWebViewClient idGatewayWebViewClient;

    @Inject
    public IdGatewayWebView(Context context, ViewGroup.LayoutParams layoutParams,
                            WebChromeClient webChromeClient, IdGatewayWebViewClient idGatewayWebViewClient) {
        super(context);
        this.idGatewayWebViewClient = idGatewayWebViewClient;
        setLayoutParams(layoutParams);
        setWebChromeClient(webChromeClient);
        getSettings().setJavaScriptEnabled(true);
        getSettings().setDomStorageEnabled(true);
        setWebViewClient(idGatewayWebViewClient);
    }

    @Override
    public void loadUrl(String url) {
        throw new RuntimeException("Not supported, use loadUrl(String url, " +
                "Map<String, String> additionalHttpHeaders, IdGatewayWebviewClientCallback callback)");
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        throw new RuntimeException("Not supported, use loadUrl(String url, " +
                "Map<String, String> additionalHttpHeaders, IdGatewayWebviewClientCallback callback)");
    }

    public void loadUrl(String url, Map<String, String> additionalHttpHeaders,
                        IdGatewayWebviewClientCallback callback) {
        Uri uri = Uri.parse(url);
        String redirect = uri.getQueryParameter(REDIRECT_URI_PARAM);
        String nonce = uri.getQueryParameter(NONCE_PARAM);
        String state = uri.getQueryParameter(STATE_PARAM);
        if (TextUtils.isEmpty(redirect) || TextUtils.isEmpty(nonce) || TextUtils.isEmpty(state)) {
            throw new IllegalArgumentException("URL must contain the redirect, nonce and state query parameters");
        } else {
            Uri redirectUri = Uri.parse(uri.getQueryParameter(REDIRECT_URI_PARAM));
            idGatewayWebViewClient.setRedirectConditions(redirectUri, nonce, state, callback);
            if (additionalHttpHeaders == null) {
                super.loadUrl(url);
            } else {
                super.loadUrl(url, additionalHttpHeaders);
            }
        }
    }
}
