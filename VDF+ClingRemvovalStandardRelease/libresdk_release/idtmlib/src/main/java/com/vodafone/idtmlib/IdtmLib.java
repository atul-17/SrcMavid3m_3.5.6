package com.vodafone.idtmlib;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.vodafone.idtmlib.exceptions.GoogleInstanceIdException;
import com.vodafone.idtmlib.exceptions.IdGatewayRequiredException;
import com.vodafone.idtmlib.exceptions.IdtmInProgressException;
import com.vodafone.idtmlib.exceptions.IdtmServerException;
import com.vodafone.idtmlib.exceptions.IdtmTemporaryIssueException;
import com.vodafone.idtmlib.exceptions.NotInitializedException;
import com.vodafone.idtmlib.exceptions.SecureStorageException;
import com.vodafone.idtmlib.exceptions.UserCanceledException;
import com.vodafone.idtmlib.lib.IdtmLibImpl;
import com.vodafone.idtmlib.lib.dagger.AppComponent;
import com.vodafone.idtmlib.lib.dagger.AppModule;
import com.vodafone.idtmlib.lib.dagger.DaggerAppComponent;
import com.vodafone.idtmlib.lib.dagger.EnvironmentModule;
import com.vodafone.idtmlib.lib.dagger.NetworkModule;
import com.vodafone.idtmlib.lib.dagger.PrinterModule;
import com.vodafone.idtmlib.lib.storage.DataCrypt;
import com.vodafone.idtmlib.lib.storage.Prefs;
import com.vodafone.idtmlib.lib.storage.basic.AesException;
import com.vodafone.idtmlib.lib.utils.Printer;
import com.vodafone.idtmlib.observers.AuthenticateObserver;
import com.vodafone.idtmlib.observers.InitializeObserver;
import com.vodafone.lib.seclibng.SecLib;

import java.util.List;

import javax.inject.Inject;

/**
 * IdtmLib allows the user to login by simply using this class and remove the burden from the main app.
 */
public class IdtmLib {
    @Inject
    IdtmLibImpl idtmLibImpl;
    @Inject
    DataCrypt dataCrypt;
    @Inject
    Printer printer;
    private DaggerAppComponent.Builder appComponentBuilder;
    private AppComponent appComponent;
    private String certPinningHashes;

    /**
     * Create an instance of IdtmLib. Must be set inside the extended Application class in the
     * onCreate() method.
     *
     * @param application Extended application class
     * @param type        Select the environment as development, pre-production or production
     * @param enableLogs  Enable the lib logs. They will be visible on Logcat and a file will be
     *                    created on path /sdcard/Android/data/<i>APP_PACKAGE_NAME</i>/cache/idtmlib.log
     */
    public IdtmLib(Application application, EnvironmentType type, boolean enableLogs) {
        appComponentBuilder = DaggerAppComponent.builder()
                .appModule(new AppModule(application))
                .environmentModule(new EnvironmentModule(type))
                .networkModule(new NetworkModule(null))
                .printerModule(new PrinterModule(enableLogs));
        appComponent = appComponentBuilder.build();
        appComponent.inject(this);
        try {

            /*SecLibNG.getInstance().setEnvironment(SecLibNG.ENVIRONMENT_PRE, application);
            SecLibNG.getInstance().setVerbose(true);
            SecLibNG.getInstance().setContext(application);*/

            certPinningHashes = dataCrypt.getString(Prefs.CERT_PINNING_HASHES);
        } catch (AesException e) {
            // data crypt not initialized yet
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkRebuildAppComponent() {
        try {
            String currentCertPinningHashes = dataCrypt.getString(Prefs.CERT_PINNING_HASHES);
            if (!TextUtils.equals(certPinningHashes, currentCertPinningHashes)) {
                printer.i("Applying new set of certificates: ", currentCertPinningHashes);
                certPinningHashes = currentCertPinningHashes;
                appComponent = appComponentBuilder
                        .networkModule(new NetworkModule(certPinningHashes))
                        .build();
                appComponent.inject(this);
            }
        } catch (AesException e) {
            // data crypt not initialized yet
        }
    }

    /**
     * Used internally by the lib.
     *
     * @return
     */
    public AppComponent getAppComponent() {
        return appComponent;
    }

    /**
     * Return the lib version name
     *
     * @return version
     */
    public String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * Return the lib version code
     *
     * @return version
     */
    public int getVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    /**
     * Initialize the lib by performing the secure setup with the server and starting automatic
     * access token refreshes.<br/>
     * <br/>
     * This should be called at the start of the application class after instantiating IDTMLib.<br/>
     * Other invocations allows the main app to change the basic values (like ACR values, etc).<br/>
     * This doesn't logout an already logined user.<br/>
     * <br/>
     * This method is async and can be called multiple times from different threads.<br/>
     * The process will be executed only once, but the result is posted to every observer.<br/>
     *
     * @param clientId                 Client ID related to the app
     * @param idGatewayRedirectUrl     Redirect URL related to the webview showing the ID Gateway
     * @param idGatewayMobileAcrValues ID Gateway ACR values to use on mobile connection
     * @param idGatewayWifiAcrValues   ID Gateway ACR values to use on wifi connection
     * @param idGatewayScope           Scope of the ID Gateway
     * @param observer                 RxJava observer which receive the results.
     */
    public void initialize(@NonNull String clientId, @NonNull String idGatewayRedirectUrl,
                           @NonNull List<String> idGatewayMobileAcrValues,
                           @NonNull List<String> idGatewayWifiAcrValues,
                           @NonNull List<String> idGatewayScope,
                           @NonNull InitializeObserver observer,
                           String loginHint) {
        checkRebuildAppComponent();
        idtmLibImpl.initialize(clientId, idGatewayRedirectUrl, idGatewayMobileAcrValues,
                idGatewayWifiAcrValues, idGatewayScope, observer, loginHint);
    }

    /**
     * Initialize the lib by performing the secure setup with the server.<br/>
     * <br/>
     * This should be called only one time in the lifetime of the app, but other invocations
     * allows the main app to change the basic values (like ACR values, etc).<br/>
     * This doesn't logout an already logined user.<br/>
     * <br/>
     * This method can be called multiple times from different threads and will stop code execution
     * until the process is finished. It must be used on a worker thread.<br/>
     *
     * @param clientId                 Client ID related to the app
     * @param idGatewayRedirectUrl     Redirect URL related to the webview showing the ID Gateway
     * @param idGatewayMobileAcrValues ID Gateway ACR values to use on mobile connection
     * @param idGatewayWifiAcrValues   ID Gateway ACR values to use on wifi connection
     * @param idGatewayScope           Scope of the ID Gateway
     * @throws IdtmServerException         Generic IDTM server error, should retry
     * @throws IdtmTemporaryIssueException IDTM server has temporary issues, should retry later
     * @throws GoogleInstanceIdException   Error retrieving the Google instance ID, could be a network
     *                                     error or an issue with Google Play Services (should be enabled and updated)
     * @throws SecureStorageException      Critical error generating keys using the Android Keystore,
     *                                     maybe the device/firmware has a bug or must be updated<br/>
     * @throws Exception                   Generic exceptions shouldn't be triggered
     */
    @WorkerThread
    public void initialize(@NonNull String clientId, @NonNull String idGatewayRedirectUrl,
                           @NonNull List<String> idGatewayMobileAcrValues,
                           @NonNull List<String> idGatewayWifiAcrValues,
                           @NonNull List<String> idGatewayScope, String loginHint) throws Exception {
        checkRebuildAppComponent();
        idtmLibImpl.initialize(clientId, idGatewayRedirectUrl, idGatewayMobileAcrValues,
                idGatewayWifiAcrValues, idGatewayScope, loginHint);
    }

    /**
     * Initialize the lib by performing the secure setup with the server and starting automatic
     * access token refreshes.<br/>
     * <br/>
     * This should be called at the start of the application class after instantiating IDTMLib.<br/>
     * Other invocations allows the main app to change the basic values (like ACR values, etc).<br/>
     * This doesn't logout an already logined user.<br/>
     * <br/>
     * This method is async and can be called multiple times from different threads.<br/>
     * The process will be executed only once, but the result is posted to every observer.<br/>
     *
     * @param clientId                 Client ID related to the app
     * @param idGatewayRedirectUrl     Redirect URL related to the webview showing the ID Gateway
     * @param idGatewayMobileAcrValues ID Gateway ACR values to use on mobile connection
     * @param idGatewayWifiAcrValues   ID Gateway ACR values to use on wifi connection
     * @param idGatewayScope           Scope of the ID Gateway
     * @param observer                 RxJava observer which receive the results.
     */
    public void initialize(@NonNull String clientId, @NonNull String idGatewayRedirectUrl,
                           @NonNull List<String> idGatewayMobileAcrValues,
                           @NonNull List<String> idGatewayWifiAcrValues,
                           @NonNull List<String> idGatewayScope,
                           @NonNull InitializeObserver observer,
                           String loginHint,
                           boolean allowAutoRefresh) {
        checkRebuildAppComponent();
        idtmLibImpl.initialize(clientId, idGatewayRedirectUrl, idGatewayMobileAcrValues,
                idGatewayWifiAcrValues, idGatewayScope, observer, loginHint, allowAutoRefresh);
    }

    /**
     * Initialize the lib by performing the secure setup with the server.<br/>
     * <br/>
     * This should be called only one time in the lifetime of the app, but other invocations
     * allows the main app to change the basic values (like ACR values, etc).<br/>
     * This doesn't logout an already logined user.<br/>
     * <br/>
     * This method can be called multiple times from different threads and will stop code execution
     * until the process is finished. It must be used on a worker thread.<br/>
     *
     * @param clientId                 Client ID related to the app
     * @param idGatewayRedirectUrl     Redirect URL related to the webview showing the ID Gateway
     * @param idGatewayMobileAcrValues ID Gateway ACR values to use on mobile connection
     * @param idGatewayWifiAcrValues   ID Gateway ACR values to use on wifi connection
     * @param idGatewayScope           Scope of the ID Gateway
     * @throws IdtmServerException         Generic IDTM server error, should retry
     * @throws IdtmTemporaryIssueException IDTM server has temporary issues, should retry later
     * @throws GoogleInstanceIdException   Error retrieving the Google instance ID, could be a network
     *                                     error or an issue with Google Play Services (should be enabled and updated)
     * @throws SecureStorageException      Critical error generating keys using the Android Keystore,
     *                                     maybe the device/firmware has a bug or must be updated<br/>
     * @throws Exception                   Generic exceptions shouldn't be triggered
     */
    @WorkerThread
    public void initialize(@NonNull String clientId, @NonNull String idGatewayRedirectUrl,
                           @NonNull List<String> idGatewayMobileAcrValues,
                           @NonNull List<String> idGatewayWifiAcrValues,
                           @NonNull List<String> idGatewayScope, String loginHint,
                           boolean allowAutoRefresh) throws Exception {
        checkRebuildAppComponent();
        idtmLibImpl.initialize(clientId, idGatewayRedirectUrl, idGatewayMobileAcrValues,
                idGatewayWifiAcrValues, idGatewayScope, loginHint, allowAutoRefresh);
    }

    /**
     * Authenticate and get the access token. It will always return the access token, but the
     * execution change based on:<br/>
     * - Access token not present or invalid: get a new access token by user login via ID Gateway
     * web view<br/>
     * - Access token present, but expired: refresh the access token<br/>
     * - Access token valid: get the access token from the secure storage<br/>
     * - Invalid access token is the same as the one stored: refresh the token<br/>
     * <br/>
     * <i>Please note: normally the invalidAccessToken param must be null. <br/>
     * In case the access token returned by the lib is used to perform an API request to the host
     * app service and it responds that the token is no more valid, then the host app should retry
     * authenticate() passing the invalid access token as a param.<br/>
     * This way the IDTM lib can renew the access token even if it not expired yet.</i><br/>
     * <br/>
     * This method is async and can be called multiple times from different threads.<br/>
     * The process will be executed only once, but the result is posted to every observer.<br/>
     *
     * @param allowIdGateway     if true, then the ID Gateway will be started when needed, otherwise
     *                           this will return an error
     * @param invalidAccessToken access token invalidated by a request to an API of the host app
     * @param observer           RxJava observer which receive the result containing the access token info.
     *                           It will also notify when the ID Gateway is shown/hidden over the main app UI.
     */
    public void authenticate(boolean allowIdGateway, @Nullable String invalidAccessToken,
                             @NonNull AuthenticateObserver observer) {
        checkRebuildAppComponent();
        idtmLibImpl.authenticate(allowIdGateway, invalidAccessToken, observer);
    }

    /**
     * Authenticate and get the access token. It will always return the access token, but the
     * execution change based on:<br/>
     * - Access token not present: get a new access token by user login via ID Gateway
     * web view<br/>
     * - Access token present, but expired: refresh the access token<br/>
     * - Access token valid: get the access token from the secure storage<br/>
     * - Invalid access token is the same as the one stored: refresh the token<br/>
     * <br/>
     * <i>Please note: normally the invalidAccessToken param must be null. <br/>
     * In case the access token returned by the lib is used to perform an API request to the host
     * app service and it responds that the token is no more valid, then the host app should retry
     * authenticate() passing the invalid access token as a param.<br/>
     * This way the IDTM lib can renew the access token even if it not expired yet.</i><br/>
     * <br/>
     * This method can be called multiple times from different threads and will stop code execution
     * until the process is finished. It must be used on a worker thread.<br/>
     *
     * @param allowIdGateway     if true, then the ID Gateway will be started when needed, otherwise
     *                           this will return an error
     * @param invalidAccessToken access token invalidated by a request to an API of the host app
     * @return Access token information
     * @throws NotInitializedException     Need to initialize first
     * @throws SecureStorageException      Error encrypting/decrypting data using the Android Keystore,
     *                                     should retry initialize()
     * @throws IdtmServerException         Generic IDTM/IDGW server error, should retry
     * @throws IdtmTemporaryIssueException IDTM server has temporary issues, should retry later
     * @throws IdGatewayRequiredException  ID Gateway is required to get a new access token, but
     *                                     was not allowed to open the webview
     * @throws UserCanceledException       User has canceled the ID Gateway request (eg: pressed back,
     *                                     clicked on Cancel)
     * @throws Exception                   Generic exceptions shouldn't be triggered
     */
    @WorkerThread
    public AccessToken authenticate(boolean allowIdGateway, @Nullable String invalidAccessToken)
            throws Exception {
        checkRebuildAppComponent();
        return idtmLibImpl.authenticate(allowIdGateway, invalidAccessToken);
    }

    /**
     * Logout the user and allows to authenticate again.
     */
    public void logout() throws IdtmInProgressException {
        idtmLibImpl.logout();
    }

    /**
     * Erase all data.
     */
    public void eraseAll() {
        idtmLibImpl.eraseAll();
    }

    /**
     * Method to setup Smapi.
     */
    public void setupSmapi(Context context, boolean usePreProdEnv) {
        SecLib secLib = SecLib.getInstance();
        secLib.setup(context, usePreProdEnv ? SecLib.VF_PRE : SecLib.VF_PROD);
        SecLib.getInstance().setVerbose(true);
    }

    /**
     * Clear certificate pinner hashes
     */
    // debugging only
    public void clearHashes() {
        idtmLibImpl.clearHashes();
    }

    // debugging only
    public String getSdkId() {
        return idtmLibImpl.getSdkId();
    }
}
