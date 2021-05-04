package com.vodafone.idtmlib.lib.rxjava;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.EncryptedJWT;
import com.vodafone.idtmlib.exceptions.GoogleInstanceIdException;
import com.vodafone.idtmlib.exceptions.IDTMException;
import com.vodafone.idtmlib.exceptions.IdtmInProgressException;
import com.vodafone.idtmlib.exceptions.IdtmRetryException;
import com.vodafone.idtmlib.exceptions.IdtmSSLPeerUnverifiedException;
import com.vodafone.idtmlib.exceptions.IdtmServerException;
import com.vodafone.idtmlib.exceptions.IdtmTemporaryIssueException;
import com.vodafone.idtmlib.exceptions.NoNetworkConnectionException;
import com.vodafone.idtmlib.exceptions.SecureStorageException;
import com.vodafone.idtmlib.exceptions.TimeOutRetriableException;
import com.vodafone.idtmlib.lib.AutoRefreshTokenJobService;
import com.vodafone.idtmlib.lib.network.BadStatusCodeException;
import com.vodafone.idtmlib.lib.network.Environment;
import com.vodafone.idtmlib.lib.network.IdtmApi;
import com.vodafone.idtmlib.lib.network.JwtHelper;
import com.vodafone.idtmlib.lib.network.Response;
import com.vodafone.idtmlib.lib.network.RevokeTokens;
import com.vodafone.idtmlib.lib.network.models.SymmetricKey;
import com.vodafone.idtmlib.lib.network.models.bodies.SetupBody;
import com.vodafone.idtmlib.lib.network.models.responses.ErrorResponse;
import com.vodafone.idtmlib.lib.network.models.responses.JwtResponse;
import com.vodafone.idtmlib.lib.network.models.responses.SetupResponse;
import com.vodafone.idtmlib.lib.storage.DataCrypt;
import com.vodafone.idtmlib.lib.storage.Keys;
import com.vodafone.idtmlib.lib.storage.Prefs;
import com.vodafone.idtmlib.lib.storage.basic.AesException;
import com.vodafone.idtmlib.lib.storage.basic.AndroidKeyStore;
import com.vodafone.idtmlib.lib.storage.basic.Preferences;
import com.vodafone.idtmlib.lib.storage.basic.RsaException;
import com.vodafone.idtmlib.lib.utils.Device;
import com.vodafone.idtmlib.lib.utils.ISmapiConst;
import com.vodafone.idtmlib.lib.utils.Printer;
import com.vodafone.idtmlib.lib.utils.ProofId;
import com.vodafone.idtmlib.lib.utils.Smapi;

import org.json.JSONException;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.SSLPeerUnverifiedException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

@Singleton
public class InitializeObservable implements ObservableOnSubscribe<Object> {
    @Inject
    Environment environment;
    private Context context;
    private Preferences preferences;
    private Printer printer;
    private ProofId proofId;
    private Gson gson;
    private DataCrypt dataCrypt;
    private IdtmApi idtmApi;
    private Smapi smapi;
    private String smapiTransactionId = UUID.randomUUID().toString();
    private Semaphore idtmSemaphore;
    private String clientId;
    private String idGatewayRedirectUrl;
    private List<String> idGatewayMobileAcrValues;
    private List<String> idGatewayWifiAcrValues;
    private List<String> idGatewayScope;
    private String loginHint;
    private boolean allowAutoRefresh;
    private Observable<Object> observable = Observable.create(this)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .replay(1)
            .refCount();
    private String accessToken = null;
    private String refreshToken = null;
    private String clientIdRevoke = null;
    private String authHeader = null;
    private String sdkId = "";

    @Inject
    public InitializeObservable(Context context, Printer printer, Preferences preferences,
                                ProofId proofId, Gson gson, DataCrypt dataCrypt, IdtmApi idtmApi,
                                Smapi smapi, Semaphore idtmSemaphore) {
        this.context = context;
        this.printer = printer;
        this.preferences = preferences;
        this.proofId = proofId;
        this.gson = gson;
        this.dataCrypt = dataCrypt;
        this.idtmApi = idtmApi;
        this.smapi = smapi;
        this.idtmSemaphore = idtmSemaphore;

        printer.d("InitializeObservable: IDTMSemaphore set to: ", idtmSemaphore);


        this.smapi.logInitGenerateInstance(context, smapiTransactionId);

        RxJavaPlugins.setErrorHandler(e -> {

            if (e instanceof UndeliverableException) {
                e = e.getCause();
            }
            if ((e instanceof IOException) || (e instanceof SocketException)) {
                // fine, irrelevant network problem or API that throws on cancellation
                return;
            }
            if (e instanceof InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return;
            }
            if ((e instanceof NullPointerException) || (e instanceof IllegalArgumentException)) {
                // that's likely a bug in the application
                return;
            }
            if (e instanceof IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                return;
            }
        });
    }

    public void start(String clientId, String idGatewayRedirectUrl,
                      List<String> idGatewayMobileAcrValues,
                      List<String> idGatewayWifiAcrValues, List<String> idGatewayScope,
                      Observer<Object> observer, String loginHint) {

        this.clientId = clientId;
        this.idGatewayRedirectUrl = idGatewayRedirectUrl;
        this.idGatewayMobileAcrValues = idGatewayMobileAcrValues;
        this.idGatewayWifiAcrValues = idGatewayWifiAcrValues;
        this.idGatewayScope = idGatewayScope;
        this.loginHint = loginHint;
        observable.subscribe(observer);
    }

    public void start(String clientId, String idGatewayRedirectUrl,
                      List<String> idGatewayMobileAcrValues,
                      List<String> idGatewayWifiAcrValues,
                      List<String> idGatewayScope, String loginHint) throws Exception {

        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            throw new RuntimeException("MAIN THREAD");
        }

        this.clientId = clientId;
        this.idGatewayRedirectUrl = idGatewayRedirectUrl;
        this.idGatewayMobileAcrValues = idGatewayMobileAcrValues;
        this.idGatewayWifiAcrValues = idGatewayWifiAcrValues;
        this.idGatewayScope = idGatewayScope;
        this.loginHint = loginHint;
        try {
            observable.blockingLast();
        } catch (Exception e) {
            if (e.getCause() != null && e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw e;
        }

    }

    public void start(String clientId, String idGatewayRedirectUrl,
                      List<String> idGatewayMobileAcrValues,
                      List<String> idGatewayWifiAcrValues, List<String> idGatewayScope,
                      Observer<Object> observer, String loginHint,
                      boolean allowAutoRefresh) {

        this.clientId = clientId;
        this.idGatewayRedirectUrl = idGatewayRedirectUrl;
        this.idGatewayMobileAcrValues = idGatewayMobileAcrValues;
        this.idGatewayWifiAcrValues = idGatewayWifiAcrValues;
        this.idGatewayScope = idGatewayScope;
        this.loginHint = loginHint;
        this.allowAutoRefresh = allowAutoRefresh;
        observable.subscribe(observer);
    }

    public void start(String clientId, String idGatewayRedirectUrl,
                      List<String> idGatewayMobileAcrValues,
                      List<String> idGatewayWifiAcrValues,
                      List<String> idGatewayScope, String loginHint,
                      boolean allowAutoRefresh) throws Exception {

        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            throw new RuntimeException("MAIN THREAD");
        }

        this.clientId = clientId;
        this.idGatewayRedirectUrl = idGatewayRedirectUrl;
        this.idGatewayMobileAcrValues = idGatewayMobileAcrValues;
        this.idGatewayWifiAcrValues = idGatewayWifiAcrValues;
        this.idGatewayScope = idGatewayScope;
        this.loginHint = loginHint;
        this.allowAutoRefresh = allowAutoRefresh;
        try {
            observable.blockingLast();
        } catch (Exception e) {
            if (e.getCause() != null && e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw e;
        }

    }

    @Override
    public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
        printer.i("Starting setup");
        this.smapiTransactionId = UUID.randomUUID().toString();

        Map<String, String> detailsMap = new HashMap<>();
        detailsMap.put(ISmapiConst.PAYLOAD_CLASS_NAME, "InitializeObservable");
        detailsMap.put(ISmapiConst.PAYLOAD_METHOD_NAME, "subscribe()");
        detailsMap.put(ISmapiConst.PAYLOAD_SDK_ID, sdkId);

        smapi.logInitStarted(context, smapiTransactionId, detailsMap);

        printer.d("Trying to acquire semaphore");
        if (!idtmSemaphore.tryAcquire()) {
            printer.d("Semaphore: Operation already in progress");
            throw new IdtmInProgressException();
        }

        printer.d("Acquired semaphore");

        try {

            ///////////////////////////////////////////////////
            // Check if we have already saved clientId and keys

            String savedClientId;
            try {
                // retrieve the previous client id and also checks for Keystore integrity
                savedClientId = dataCrypt.getString(Prefs.CLIENT_ID);
                if (!TextUtils.isEmpty(savedClientId)) {
                    AndroidKeyStore.getRsaKeyPair(Keys.SETUP);
                }
            } catch (RsaException | AesException e) {
                printer.i("Error in the secure storage, erasing everything and start again");
                detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "SecureStorageException");
                smapi.logKeystoreIssue(context, smapiTransactionId, "RAS or AES Exception while trying ot get RSAKeyPair. Trying to erase keys and do a fresh login. - " + e.getMessage()
                        , detailsMap);
                try {
                    AndroidKeyStore.delete(Keys.SETUP, Keys.PREFERENCES);
                    preferences.clearAllPreferences();
                    savedClientId = null;
                } catch (KeyStoreException e2) {
                    printer.i("Cannot repair the secure storage");
                    smapi.logKeystoreIssue(context, smapiTransactionId, "KeystoreException while trying to clean up key store - " + e2.getMessage(),
                            detailsMap);
                    throw new SecureStorageException("IDTM SDK initialisation: KeyStoreException : Problem to access key store: " + e2.getMessage());
                } catch (Exception e3) {
                    printer.i("Cannot repair the secure storage");
                    smapi.logKeystoreIssue(context, smapiTransactionId, "Unexpected exception while trying to clean up key store - " + e3.getMessage()
                            , detailsMap);
                    throw new SecureStorageException("IDTM SDK initialisation: Exception : Problem to access key store: " + e3.getMessage());
                }
            }

            // Initialization is only required if not done before or has been done for a different clientId
            if (savedClientId == null || !TextUtils.equals(clientId, savedClientId)) {

                ////////////////////////////////////////
                // Check if we have a network connection
                if (!Device.checkNetworkConnection(context)) {
                    printer.e("Error : No connection found");
                    detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "NoNetworkConnectionException");
                    smapi.logNoNetworkConnectionInit(context, smapiTransactionId, detailsMap);
                    throw new NoNetworkConnectionException("IDTM SDK initialisation: no network found : internet issue");
                }

                ///////////////////
                // Generate proofID

                String proofId;
                try {
                    printer.i("Getting Instance ID token");
                    proofId = this.proofId.getToken();
                   /* if (environment.isCas()) {
                        printer.d("Env is Cas");
                        proofId = "some-funny-proof-id";
                    }*/
                    if (proofId == null) {
                        printer.e("proof id is null");
                        detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "GoogleInstanceIdException");
                        smapi.logInitIssueInitializeFirebase(context, smapiTransactionId, detailsMap);
                        throw new GoogleInstanceIdException("IDTM SDK initialisation: unable to initialize firebase, proof id is null");
                    }
                } catch (SocketTimeoutException se) {
                    detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "TimeOutRetriableException");
                    smapi.logInitIssueGenerateProofIdTimeout(context, smapiTransactionId, se.getMessage(), detailsMap);
                    printer.e("SocketTimeoutException: ", se);
                    throw new TimeOutRetriableException();
                } catch (IOException e) {
                    printer.e("Error getting token. Play Services available? ", e);
                    detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "GoogleInstanceIdException");
                    smapi.logInitIssueGenerateProofId(context, smapiTransactionId, detailsMap);
                    throw new GoogleInstanceIdException("IDTM SDK initialisation: IOException :unable to generate proof id : " + e.getMessage());
                }

                printer.d("ProofId: " + proofId);

                try {

                    ////////////////////
                    // Generate new keys

                    String referenceId;
                    KeyPair clientKeyPair;

                    try {
                        printer.i("Generating secure keys");
                        clientKeyPair = AndroidKeyStore.generateRsa(context, Keys.SETUP, false);
                        AndroidKeyStore.generateAes(Keys.PREFERENCES, false);
                        referenceId = dataCrypt.getString(Prefs.REFERENCE_ID);
                        if (TextUtils.isEmpty(referenceId)) {
                            referenceId = UUID.randomUUID().toString();
                        }

                    } catch (RsaException | AesException e) {
                        printer.e("Error generating secure keys ", e);
                        detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "SecureStorageException");
                        smapi.logKeystoreIssue(context, smapiTransactionId, "RSA or AES Exception while trying to generate new secure keys - " + e.getMessage()
                                , detailsMap);
                        throw new SecureStorageException("IDTM SDK initialisation: Error generating secure keys: " + e.getMessage());
                    } catch (Exception e) {
                        printer.e("Error generating secure keys ", e);
                        detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "SecureStorageException");
                        smapi.logKeystoreIssue(context, smapiTransactionId, "Unexpected Exception while trying to generate new secure keys - " + e.getMessage(),
                                detailsMap);
                        throw new SecureStorageException("IDTM SDK initialisation: Exception generating secure keys: " + e.getMessage());
                    }

                    ////////////////////////////////////
                    // Perform setup request to backend

                    SymmetricKey symmetricKey = new SymmetricKey();
                    SetupResponse setupResponse = new SetupResponse();

                    try {
                        printer.i("Performing setup request");
                        smapi.logInitCallbackInitialised(context, smapiTransactionId);
                        SetupBody setupBody = new SetupBody(referenceId, proofId,
                                clientKeyPair != null ? (RSAPublicKey) clientKeyPair.getPublic() : null);
                        JwtResponse jwtResponse = Response.retrieve(JwtResponse.class,
                                idtmApi.setup(clientId, setupBody));
                        EncryptedJWT encryptedJWT = JwtHelper.rsaDecrypt(clientKeyPair != null ? clientKeyPair.getPrivate() : null,
                                jwtResponse.getData());
                        String responseJson = JwtHelper.getJwtMsg(encryptedJWT);
                        printer.i("Setup response: ", responseJson);
                        setupResponse = gson.fromJson(responseJson, SetupResponse.class);
                        symmetricKey = gson.fromJson(setupResponse.getSymmetricKey(),
                                SymmetricKey.class);
                        sdkId = setupResponse.getSdkId();
                    } catch (SSLPeerUnverifiedException e) {
                        printer.e("Error while certificate pinning init ", e);
                        detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmSSLPeerUnverifiedException");
                        smapi.logAuthenticateCertificateIssue(context, smapiTransactionId, e.getMessage(), detailsMap);
                        throw new IdtmSSLPeerUnverifiedException();
                    } catch (BadStatusCodeException e) {
                        int responseCode = e.getResponse().getResponseCode();
                        ErrorResponse errorResponse = gson.fromJson(e.getResponse().getResponseErrorBodyString(), ErrorResponse.class);
                        if (responseCode >= 500 && responseCode < 600) {
                            detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmTemporaryIssueException");
                            smapi.logInitBackendTempIssue(context, smapiTransactionId, gson.toJson(errorResponse), detailsMap);
                            if (!emitter.isDisposed()) {
                                printer.e("Server temporary issue ", e);
                                smapi.logInitBackendTempIssue(context, smapiTransactionId,  gson.toJson(errorResponse), detailsMap);
                                throw new IdtmTemporaryIssueException("Server temporary issue "+ gson.toJson(errorResponse));
                            }
                        }
                        detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmRetryException");
                        smapi.logInitBackendInitFailure(context, smapiTransactionId, errorResponse != null ? errorResponse.getDescription() : null,
                                detailsMap);
                        printer.e("Server error", e);
                        throw new IdtmRetryException();
                    } catch (SocketTimeoutException se) {
                        detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "TimeOutRetriableException");
                        smapi.logInitBackendSocketTimeOutException(context, smapiTransactionId, se.getMessage(), detailsMap);
                        printer.e("SocketTimeoutException: ", se);
                        throw new TimeOutRetriableException();
                    } catch (IOException e) {
                        detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmRetryException");
                        smapi.logInitBackendInitIOException(context, smapiTransactionId, e.getMessage(), detailsMap);
                        printer.e("IO Exception: ", e);
                        throw new IdtmRetryException();
                    } catch (ParseException | JOSEException | JsonParseException e) {
                        detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmServerException");
                        smapi.logInitInvalidJson(context, smapiTransactionId, "Unable to parse JSON - " + e.getMessage(),
                                detailsMap);
                        printer.e("Server error", e);
                        throw new IdtmServerException("Init: Unable to parse JSON - " + e.getMessage());
                    }

                    ///////////////
                    // Saving data

                    printer.i("Saving data");

                    if (!symmetricKey.isValid()) {
                        detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "JSONException");
                        smapi.logInitInvalidJson(context, smapiTransactionId, "Symmetric key from server is invalid",
                                detailsMap);
                        throw new JSONException("Symmetric key Json is not valid");
                    }

                    if (TextUtils.isEmpty(setupResponse.getSdkId())) {
                        detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmServerException");
                        smapi.logInitBackendSdkNotFound(context, smapiTransactionId, detailsMap);
                        throw new IdtmServerException("Init: SdkId not returned from backend");
                    }

                    try {
                        dataCrypt.set(Prefs.CLIENT_ID, clientId);
                        dataCrypt.set(Prefs.SDK_ID, setupResponse.getSdkId());
                        dataCrypt.set(Prefs.SERVER_SYMMETRIC_KEY, symmetricKey.getK());
                        dataCrypt.set(Prefs.SERVER_SYMMETRIC_EXPIRE_TIME_MS,
                                setupResponse.getKeyExpDate() * 1000L);
                        dataCrypt.set(Prefs.SERVER_SYMMETRIC_REFRESH_STATE,
                                setupResponse.getRefreshState());
                        dataCrypt.set(Prefs.CERT_PINNING_HASHES,
                                setupResponse.getCertificates() == null ? null :
                                        gson.toJson(setupResponse.getCertificates()));
                        dataCrypt.set(Prefs.REFERENCE_ID, referenceId);
                        dataCrypt.set(Prefs.ALLOW_AUTO_REFRESH, allowAutoRefresh);
                    } catch (AesException e) {
                        printer.e("Error saving data to secure storage ", e);
                        detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "SecureStorageException");
                        smapi.logKeystoreIssue(context, smapiTransactionId,
                                "AES Exception while saving SDKID, clientID and keys to secure storage - " + e.getMessage()
                                , detailsMap);
                        throw new SecureStorageException("IDTM SDK initialisation: AES Exception while saving SDKID, clientID and keys to secure storage - " + e.getMessage());
                    } catch (Exception e) {
                        printer.e("Error saving data to secure storage ", e);
                        detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "SecureStorageException");
                        smapi.logKeystoreIssue(context, smapiTransactionId, "Unexpected Exception while saving SDKID, clientID and keys to secure storage - " + e.getMessage()
                                , detailsMap);
                        throw new SecureStorageException("IDTM SDK initialisation: Unexpected Exception while saving SDKID, clientID and keys to secure storage - " + e.getMessage());
                    }
                } catch (IDTMException e) {
                    // already handled
                    throw e;
                } catch (Exception e) {
                    // unexpected exception
                    detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmRetryException");
                    smapi.logInitUnexpectedException(context, smapiTransactionId, e.getMessage(), detailsMap);
                    throw new IdtmRetryException();
                }
                detailsMap.put(ISmapiConst.PAYLOAD_SDK_ID, dataCrypt.getString(Prefs.SDK_ID));
                smapi.logInitBackendInitSuccess(context, smapiTransactionId, detailsMap);
            } else {
                smapi.logInitAlreadyInit(context, smapiTransactionId);
            }

            dataCrypt.set(Prefs.ID_GATEWAY_REDIRECT_URL, idGatewayRedirectUrl);
            dataCrypt.set(Prefs.ID_GATEWAY_MOBILE_ACR_VALUES, listToString(idGatewayMobileAcrValues));
            dataCrypt.set(Prefs.ID_GATEWAY_WIFI_ACR_VALUES, listToString(idGatewayWifiAcrValues));
            dataCrypt.set(Prefs.ID_GATEWAY_SCOPE, listToString(idGatewayScope));
            dataCrypt.set(Prefs.LOGIN_HINT, loginHint);
            long accessTokenExpireTimeMs = dataCrypt.getLong(Prefs.ACCESS_TOKEN_EXPIRE_TIME_MS, Long.MIN_VALUE);
            if (accessTokenExpireTimeMs > Long.MIN_VALUE) {
                printer.i("Scheduling auto refresh time: ", accessTokenExpireTimeMs);
                AutoRefreshTokenJobService.setAlarm(context, accessTokenExpireTimeMs);
            }
        } catch (Exception e) {
            if (!(e instanceof IdtmInProgressException)) {
                printer.d("Release semaphore in error handler");
                idtmSemaphore.release();
            }

            throw e;
        }

        printer.i("Completed");

        printer.d("Releasing semaphore on success");
        idtmSemaphore.release();
        emitter.onNext(new Object());
        emitter.onComplete();
    }

    private String listToString(List<String> acrValues) {
        StringBuffer acrValuesBuf = new StringBuffer();
        for (String value : acrValues) {
            acrValuesBuf.append(" ").append(value);
        }
        return acrValuesBuf.substring(1);
    }

    public void startLogout() throws IdtmInProgressException {

        printer.d("Logout: Trying to acquire semaphore");

        if (!idtmSemaphore.tryAcquire()) {
            printer.d("Logout: Unable to acquire semaphore");
            throw new IdtmInProgressException();
        }

        printer.d("Logout: Successfully acquired semaphore");

        try {
            RevokeTokens revokeTokens = new RevokeTokens(context, preferences, printer, dataCrypt, smapi, environment);
            revokeTokens.startLogout("init");
            idtmSemaphore.release();
        } catch (Exception e) {
            idtmSemaphore.release();
        }
    }

    // debugging only
    public void revokeAccessToken() {
        try {
            this.accessToken = dataCrypt.getString(Prefs.ACCESS_TOKEN);
            this.clientIdRevoke = dataCrypt.getString(Prefs.CLIENT_ID);
            RevokeTokens revokeTokens = new RevokeTokens();
            this.authHeader = revokeTokens.toBase64(clientIdRevoke);
            RevokeAccessTokenOnlyAsynTask revokeAccessTokenAsynTask = new RevokeAccessTokenOnlyAsynTask();
            revokeAccessTokenAsynTask.execute();
        } catch (AesException e) {
            return;
        }

    }

    // debugging only
    public void revokeRefreshToken() {
        try {
            this.refreshToken = dataCrypt.getString(Prefs.REFRESH_TOKEN);
            if (TextUtils.isEmpty(this.refreshToken)) {
                this.refreshToken = dataCrypt.getString(Prefs.ACCESS_TOKEN_REFRESH_TOKEN);
            }
            this.clientIdRevoke = dataCrypt.getString(Prefs.CLIENT_ID);
            RevokeTokens revokeTokens = new RevokeTokens();
            this.authHeader = revokeTokens.toBase64(clientIdRevoke);
            RevokeRefreshTokenOnlyAsynTask revokeRefreshTokenAsynTask = new RevokeRefreshTokenOnlyAsynTask();
            revokeRefreshTokenAsynTask.execute();
        } catch (AesException e) {
            return;
        }

    }

    // debugging only
    public void clearHashes() {
        try {
            dataCrypt.set(Prefs.CERT_PINNING_HASHES, null);
        } catch (AesException e) {
            return;
        }
    }

    // debugging only
    public String getSdkId() {
        try {
            return dataCrypt.getString(Prefs.SDK_ID);
        } catch (AesException e) {
            printer.e("Error while getting SdkId :" + e);
            return null;
        }
    }

    // debugging only
    public class RevokeAccessTokenOnlyAsynTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            RevokeTokens revokeTokens = new RevokeTokens(context, preferences, printer, dataCrypt, smapi, environment);
            revokeTokens.revokeToken(accessToken, "access_token");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    // debugging only
    public class RevokeRefreshTokenOnlyAsynTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            RevokeTokens revokeTokens = new RevokeTokens(context, preferences, printer, dataCrypt, smapi, environment);
            revokeTokens.revokeToken(refreshToken, "refresh_token");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}