package com.vodafone.idtmlib.lib;

import androidx.annotation.WorkerThread;
import android.text.TextUtils;

import com.vodafone.idtmlib.AccessToken;
import com.vodafone.idtmlib.exceptions.IdtmInProgressException;
import com.vodafone.idtmlib.lib.rxjava.AuthenticateObservable;
import com.vodafone.idtmlib.lib.rxjava.InitializeObservable;
import com.vodafone.idtmlib.lib.storage.Keys;
import com.vodafone.idtmlib.lib.storage.Prefs;
import com.vodafone.idtmlib.lib.storage.basic.AndroidKeyStore;
import com.vodafone.idtmlib.lib.storage.basic.Preferences;
import com.vodafone.idtmlib.observers.AuthenticateObserver;
import com.vodafone.idtmlib.observers.InitializeObserver;

import java.security.KeyStoreException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class IdtmLibImpl {
    private Preferences preferences;
    private InitializeObservable initializeObservable;
    private AuthenticateObservable authenticateObservable;

    @Inject
    public IdtmLibImpl(Preferences preferences, InitializeObservable initializeObservable,
                       AuthenticateObservable authenticateObservable) {
        this.preferences = preferences;
        this.initializeObservable = initializeObservable;
        this.authenticateObservable = authenticateObservable;
    }

    public void initialize(String clientId, String idGatewayRedirectUrl,
                           List<String> idGatewayMobileAcrValues, List<String> idGatewayWifiAcrValues,
                           List<String> idGatewayScope, InitializeObserver observer, String loginHint) {
        initializeObservable.start(clientId, idGatewayRedirectUrl, idGatewayMobileAcrValues,
                idGatewayWifiAcrValues, idGatewayScope, observer, loginHint);
    }

    @WorkerThread
    public void initialize(String clientId, String idGatewayRedirectUrl,
                           List<String> idGatewayMobileAcrValues, List<String> idGatewayWifiAcrValues,
                           List<String> idGatewayScope, String loginHint) throws Exception {
        initializeObservable.start(clientId, idGatewayRedirectUrl, idGatewayMobileAcrValues,
                idGatewayWifiAcrValues, idGatewayScope, loginHint);
    }

    public void initialize(String clientId, String idGatewayRedirectUrl,
                           List<String> idGatewayMobileAcrValues, List<String> idGatewayWifiAcrValues,
                           List<String> idGatewayScope, InitializeObserver observer, String loginHint,
                           boolean allowAutoRefresh) {
        initializeObservable.start(clientId, idGatewayRedirectUrl, idGatewayMobileAcrValues,
                idGatewayWifiAcrValues, idGatewayScope, observer, loginHint, allowAutoRefresh);
    }

    @WorkerThread
    public void initialize(String clientId, String idGatewayRedirectUrl,
                           List<String> idGatewayMobileAcrValues, List<String> idGatewayWifiAcrValues,
                           List<String> idGatewayScope, String loginHint, boolean allowAutoRefresh) throws Exception {
        initializeObservable.start(clientId, idGatewayRedirectUrl, idGatewayMobileAcrValues,
                idGatewayWifiAcrValues, idGatewayScope, loginHint, allowAutoRefresh);
    }

    public void authenticate(boolean allowIdGateway, String invalidAccessToken,
                             AuthenticateObserver observer) {
        authenticateObservable.start(allowIdGateway, invalidAccessToken, observer);
    }

    @WorkerThread
    public AccessToken authenticate(boolean allowIdGateway, String invalidAccessToken)
            throws Exception {
        return authenticateObservable.start(allowIdGateway, invalidAccessToken).getAccessToken();
    }

    public void logout() throws IdtmInProgressException {
        initializeObservable.startLogout();
    }

    public void eraseAll() {
        try {
            AndroidKeyStore.delete(Keys.SETUP, Keys.PREFERENCES);
            preferences.clearAllPreferences();
        } catch (KeyStoreException e2) {
            // nothing we can do
        }
    }

    public boolean isInitialized() {
        return !TextUtils.isEmpty(preferences.getString(Prefs.CLIENT_ID));
    }

    // debugging only
    public void clearHashes() {
        initializeObservable.clearHashes();
    }

    // debugging only
    public String getSdkId() {
        return initializeObservable.getSdkId();
    }
}
