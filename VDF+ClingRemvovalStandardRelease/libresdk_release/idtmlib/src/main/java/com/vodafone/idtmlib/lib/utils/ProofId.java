package com.vodafone.idtmlib.lib.utils;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

import javax.inject.Inject;

public class ProofId {
    String authorizedEntity = "332907037616";//value taken from firebase.sdk.android.authorized-entity configured on server application.properties file
    String scope = "FCM";
    private Printer printer;
    private Context context;

    @Inject
    public ProofId(Context context, Printer printer) {
        this.context = context;
        this.printer = printer;
    }

    public String getToken() throws IOException {
        FirebaseApp firebaseApp = FirebaseApp.initializeApp(this.context);
        if (firebaseApp != null) {
            return FirebaseInstanceId.getInstance().getToken(authorizedEntity, scope);
        } else {
            printer.e("Unable to initialize Firebase");
            return null;
        }
    }
}
