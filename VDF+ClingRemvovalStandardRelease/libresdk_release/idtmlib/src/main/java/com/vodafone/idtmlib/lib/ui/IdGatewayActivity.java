package com.vodafone.idtmlib.lib.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.widget.FrameLayout;

import com.vodafone.idtmlib.IdtmLibInjector;
import com.vodafone.idtmlib.R;
import com.vodafone.idtmlib.lib.ui.dagger.IdGatewayActivityModule;
import com.vodafone.idtmlib.lib.ui.elements.IdGatewaySyncer;
import com.vodafone.idtmlib.lib.ui.elements.IdGatewayWebView;
import com.vodafone.idtmlib.lib.ui.elements.IdGatewayWebviewClientCallback;
import com.vodafone.idtmlib.lib.utils.Printer;

import javax.inject.Inject;

public class IdGatewayActivity extends Activity implements IdGatewayWebviewClientCallback {
    private static final String ARG_URL = "url";
    public static String authId = null;

    @Inject Printer printer;
    @Inject IdGatewayWebView idGatewayWebView;
    @Inject IdGatewaySyncer idGatewaySyncer;

    public IdGatewayActivity() { }

    public static void start(Context appContext, String url) {
        authId = null;
        Intent intent = new Intent(appContext, IdGatewayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ARG_URL, url);
        appContext.startActivity(intent);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_gateway);
        ((IdtmLibInjector)getApplicationContext()).getIdtmLib().getAppComponent()
                .plus(new IdGatewayActivityModule()).inject(this);
        ((FrameLayout) findViewById(R.id.id_gateway_root)).addView(idGatewayWebView);
        idGatewaySyncer.notifyStart();
        loadUrl();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        loadUrl();
    }

    protected void loadUrl() {
        String idGatewayUrl = getIntent().getStringExtra(ARG_URL);
        if (TextUtils.isEmpty(idGatewayUrl)) {
            printer.e("Must set the url for the Id Gateway");
            setResult(RESULT_CANCELED);
            finish();
        } else {
            try {
                idGatewayWebView.loadUrl(idGatewayUrl, null, this);
            } catch (IllegalArgumentException e) {
                printer.e(e);
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        idGatewayWebView.stopLoading();
        idGatewaySyncer.notifyFinishUserCanceled("User press device back button");
        super.onBackPressed();
    }


    @Override
    public void onAuthId(String newAuthId) {
        authId = newAuthId;
        idGatewaySyncer.notifyRedirectToAuth();
    }


    @Override
    public void onUserCanceled(String error) {
        printer.e("onUserCanceled");
        idGatewaySyncer.notifyFinishUserCanceled(error);
        super.onBackPressed();
    }

    @Override
    public void onUserFailedToLogin(String error) {
        printer.e("onUserFailedToLogin");
        idGatewaySyncer.notifyFinishUserFailedToLogin(error);
        super.onBackPressed();
    }

    @Override
    public void onUserFailedInvalidScope(String error) {
        printer.e("onUserFailedInvalidScope");
        idGatewaySyncer.notifyFinishInvalidScope(error);
        super.onBackPressed();
    }

    @Override
    public void onRedirectResult(String code) {
        printer.e("onRedirectResult");
        idGatewaySyncer.notifyFinish(code);
        authId = null;
        finish();
    }
}
