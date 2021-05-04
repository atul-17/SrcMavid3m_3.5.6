package com.vodafone.idtmlib.lib.rxjava;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.nimbusds.jose.JOSEException;
import com.vodafone.idtmlib.AccessToken;
import com.vodafone.idtmlib.exceptions.IDTMException;
import com.vodafone.idtmlib.exceptions.IdGatewayRequiredException;
import com.vodafone.idtmlib.exceptions.IdtmIgnoreException;
import com.vodafone.idtmlib.exceptions.IdtmInProgressException;
import com.vodafone.idtmlib.exceptions.IdtmRetryException;
import com.vodafone.idtmlib.exceptions.IdtmSSLPeerUnverifiedException;
import com.vodafone.idtmlib.exceptions.IdtmServerException;
import com.vodafone.idtmlib.exceptions.IdtmTemporaryIssueException;
import com.vodafone.idtmlib.exceptions.NoNetworkConnectionException;
import com.vodafone.idtmlib.exceptions.NotInitializedException;
import com.vodafone.idtmlib.exceptions.SecureStorageException;
import com.vodafone.idtmlib.exceptions.TimeOutRetriableException;
import com.vodafone.idtmlib.exceptions.UserCanceledException;
import com.vodafone.idtmlib.lib.AutoRefreshTokenJobService;
import com.vodafone.idtmlib.lib.network.BadStatusCodeException;
import com.vodafone.idtmlib.lib.network.Environment;
import com.vodafone.idtmlib.lib.network.IdtmApi;
import com.vodafone.idtmlib.lib.network.JwtHelper;
import com.vodafone.idtmlib.lib.network.Response;
import com.vodafone.idtmlib.lib.network.RevokeTokens;
import com.vodafone.idtmlib.lib.network.models.bodies.GetAccessTokenBody;
import com.vodafone.idtmlib.lib.network.models.bodies.RefreshAccessTokenBody;
import com.vodafone.idtmlib.lib.network.models.responses.AccessTokenResponse;
import com.vodafone.idtmlib.lib.network.models.responses.ErrorResponse;
import com.vodafone.idtmlib.lib.network.models.responses.JwtResponse;
import com.vodafone.idtmlib.lib.network.models.responses.SetupResponse;
import com.vodafone.idtmlib.lib.storage.DataCrypt;
import com.vodafone.idtmlib.lib.storage.Prefs;
import com.vodafone.idtmlib.lib.storage.basic.AesException;
import com.vodafone.idtmlib.lib.storage.basic.Preferences;
import com.vodafone.idtmlib.lib.ui.IdGatewayActivity;
import com.vodafone.idtmlib.lib.ui.elements.IdGatewaySyncer;
import com.vodafone.idtmlib.lib.utils.Device;
import com.vodafone.idtmlib.lib.utils.ISmapiConst;
import com.vodafone.idtmlib.lib.utils.Printer;
import com.vodafone.idtmlib.lib.utils.Smapi;
import com.vodafone.idtmlib.lib.utils.Utils;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;
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
public class AuthenticateObservable implements ObservableOnSubscribe<AuthenticateProgress> {
    public static final long AUTO_REFRESH_PRE_TIME_SEC = 240L; //4min
    private static final int MAX_RETRY_COUNT = 25;
    private Printer printer;
    private Context context;
    private DataCrypt dataCrypt;
    private IdtmApi idtmApi;
    private Environment environment;
    private Gson gson;
    private Preferences preferences;
    private IdGatewaySyncer idGatewaySyncer;
    private Smapi smapi;
    private String smapiTransactionId = UUID.randomUUID().toString();
    private Semaphore idtmSemaphore;

    private boolean allowIdGateway;
    private String invalidAccessToken;
    private boolean autoRefresh;
    private Observable<AuthenticateProgress> observable = Observable.create(this)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .replay(1)
            .refCount();
    private String sdkId;
    private SecretKey serverSymmetricKey;
    private String sdkIdJwtToken;
    private boolean allowAutoRefresh;

    @Inject
    public AuthenticateObservable(Printer printer, Context context, DataCrypt dataCrypt,
                                  IdtmApi idtmApi, Environment environment, Gson gson,
                                  Preferences preferences, IdGatewaySyncer idGatewaySyncer,
                                  Smapi smapi, Semaphore idtmSemaphore) {
        this.printer = printer;
        this.context = context;
        this.dataCrypt = dataCrypt;
        this.idtmApi = idtmApi;
        this.environment = environment;
        this.gson = gson;
        this.preferences = preferences;
        this.idGatewaySyncer = idGatewaySyncer;
        this.smapi = smapi;
        this.idtmSemaphore = idtmSemaphore;
        try {
            this.sdkId = dataCrypt.getString(Prefs.SDK_ID);
            this.allowAutoRefresh = dataCrypt.getBoolean(Prefs.ALLOW_AUTO_REFRESH, false); //by default its false: Disable
            printer.d("AuthenticateObservable: allowAutoRefresh ", allowAutoRefresh);
        } catch (AesException e) {
            e.printStackTrace();
        }

        printer.d("AuthenticateObservable: IDTMSemaphore set to: ", idtmSemaphore);


        RxJavaPlugins.setErrorHandler(e -> {

            printer.d("AuthenticateObservable: Entered error handler: ", e);

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

    public void start(boolean allowIdGateway, String invalidAccessToken,
                      Observer<AuthenticateProgress> observer) {
        printer.i("start AuthenticateObservable");

        this.allowIdGateway = allowIdGateway;
        this.invalidAccessToken = invalidAccessToken;
        this.autoRefresh = false;
        observable.subscribe(observer);
    }

    public AuthenticateProgress start(boolean allowIdGateway, String invalidAccessToken)
            throws Exception {

        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            throw new RuntimeException("MAIN THREAD");
        }

        this.allowIdGateway = allowIdGateway;
        this.invalidAccessToken = invalidAccessToken;
        this.autoRefresh = false;
        try {
            AuthenticateProgress progress = observable.blockingLast();
            return progress;
        } catch (RuntimeException e) {
            if (e.getCause() != null && e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw e;
        }
    }

    public void startAutoRefresh(Observer<AuthenticateProgress> observer) {
        printer.i("start AuthenticateObservable autoRefresh");
        this.smapiTransactionId = UUID.randomUUID().toString();
        this.allowIdGateway = false;
        this.invalidAccessToken = null;
        this.autoRefresh = true;
        observable.subscribe(observer);
    }

    @Override
    public void subscribe(ObservableEmitter<AuthenticateProgress> emitter) throws Exception {

        try {

            AccessToken accessToken = null;
            printer.i("Starting authenticate");
            this.smapiTransactionId = UUID.randomUUID().toString();
            Map<String, String> detailsMap = new HashMap<>();
            detailsMap.put(ISmapiConst.PAYLOAD_CLASS_NAME, "AuthenticateObservable");
            detailsMap.put(ISmapiConst.PAYLOAD_METHOD_NAME, "subscribe()");
            detailsMap.put(ISmapiConst.PAYLOAD_SDK_ID, sdkId);
            smapi.logAuthenticate(context, smapiTransactionId, allowAutoRefresh, detailsMap);

            printer.d("Trying to acquire semaphore");
            if (!idtmSemaphore.tryAcquire()) {
                printer.d("Semaphore: Operation already in progress");
                detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmInProgressException");
                if (!autoRefresh) {
                    smapi.logAuthenticateCISempaphoreError(context, smapiTransactionId, detailsMap);
                } else {
                    smapi.logAuthenticateARSempaphoreError(context, smapiTransactionId, detailsMap);
                }
                throw new IdtmInProgressException();
            }

            printer.d("Acquired semaphore");

            ///////////////////////
            // Check if initialized
            String clientId = dataCrypt.getString(Prefs.CLIENT_ID);
            if (TextUtils.isEmpty(clientId)) {
                printer.e("clientId is empty");
                detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "NotInitializedException");
                smapi.logAuthenticateNotinitialised(context, smapiTransactionId, detailsMap);
                throw new NotInitializedException();
            } else {
                try {
                    //////////////////////////
                    // Get cached access token
                    String accessTokenString = dataCrypt.getString(Prefs.ACCESS_TOKEN);
                    long expireTime = dataCrypt.getLong(Prefs.ACCESS_TOKEN_EXPIRE_TIME_MS, Long.MIN_VALUE);

                    ////////////////////////////////////////////////////////////
                    // Check if token still valid or expired or has been reported as invalid
                    boolean invalidated = TextUtils.equals(accessTokenString, invalidAccessToken);
                    if (invalidAccessToken != null && invalidated) {
                        printer.e("invalidated ...");
                        smapi.logAuthorizeInvalidToken(context, smapiTransactionId, IdGatewayActivity.authId, detailsMap);
                    }

                    if (!TextUtils.isEmpty(accessTokenString) && System.currentTimeMillis() >= expireTime) {
                        printer.e("access token expired ...");
                        smapi.logAuthorizeExpired(context, smapiTransactionId, detailsMap);
                    }

                    long aboutToExpireTime = expireTime - 240000L; //Request for refresh, even if token is not expired yet but about to expire in 4min
                    if (!TextUtils.isEmpty(accessTokenString) && System.currentTimeMillis() <= aboutToExpireTime && !invalidated && !autoRefresh) {
                        printer.i("Access token still valid");
                        smapi.logAuthorizeExisting(context, smapiTransactionId, detailsMap);
                        accessToken = new AccessToken(accessTokenString,
                                dataCrypt.getString(Prefs.ACCESS_TOKEN_TYPE),
                                dataCrypt.getString(Prefs.ACCESS_TOKEN_SUB));
                    }
                    //////////////////////////////////////////////////////////////////////
                    // If we do not have an access token (accessTokenString null or empty)
                    // User nas to login again
                    // If we have an access token that is expired or invalid, try a refresh
                    else {
                        // We need a network connection in any case
                        if (!Device.checkNetworkConnection(context)) {
                            printer.e("Error : No connection found");
                            smapi.logNoNetworkConnectionAuth(context, smapiTransactionId, detailsMap);
                            throw new NoNetworkConnectionException("IDTM SDK authentication: no network found : Internet issue");
                        }

                        // We need SDKID and symmetric key in any case
                        printer.i("Preparing data for authenticate");
                        sdkId = dataCrypt.getString(Prefs.SDK_ID);
                        serverSymmetricKey = dataCrypt.loadServerSymmetricKey();
                        sdkIdJwtToken = JwtHelper.aesEncrypt(serverSymmetricKey, sdkId);

                        // Decide if user needs to log in or if we try a refresh
                        if (TextUtils.isEmpty(accessTokenString)) {
                            if (allowIdGateway) {
                                printer.i("Getting new access token");
                                smapi.logCINoAccessTokenFound(context, smapiTransactionId, "", detailsMap);
                                accessToken = newAccessToken(clientId, sdkId, serverSymmetricKey,
                                        sdkIdJwtToken, emitter);
                            } else {
                                printer.i("Need to get the new access token, but cannot open ID Gateway");
                                detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdGatewayRequiredException");
                                smapi.logAuthenticateIdGatewayRequired(context, smapiTransactionId, detailsMap);
                                throw new IdGatewayRequiredException("IDTM SDK authentication: Cannot open ID Gateway as not flag passed as false by client app");
                            }
                        } else {
                            printer.i("Refreshing access token ",
                                    invalidated ? "[invalid]" :
                                            (autoRefresh ? "[autorefresh]" : "[expired]"));
                            if (!autoRefresh) {
                                smapi.logCIRequestingRefreshToken(context, smapiTransactionId, detailsMap);
                            } else {
                                smapi.logARRequestingRefreshToken(context, smapiTransactionId, detailsMap);
                            }
                            accessToken = refreshAccessToken(clientId, sdkId, serverSymmetricKey,
                                    sdkIdJwtToken, emitter);
                        }
                    }

                } catch (SSLPeerUnverifiedException e) {
                    printer.e("Error while certificate pinning auth ", e);
                    detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmSSLPeerUnverifiedException");
                    smapi.logAuthenticateCertificateIssueInAuth(context, smapiTransactionId, IdGatewayActivity.authId, null);

                    //Call API to get latest certificates
                    JwtResponse jwtResponse = Response.retrieve(JwtResponse.class,
                            idtmApi.getClientDetails(sdkIdJwtToken, clientId, sdkId));
                    String jsonResponse = JwtHelper.aesDecrypt(serverSymmetricKey, jwtResponse.getData());
                    SetupResponse setupResponse = gson.fromJson(jsonResponse, SetupResponse.class);
                    dataCrypt.set(Prefs.CERT_PINNING_HASHES,
                            setupResponse.getCertificates() == null ? null :
                                    gson.toJson(setupResponse.getCertificates()));

                    throw new IdtmSSLPeerUnverifiedException();
                } catch (AesException e) {
                    printer.e("Error loading secure storage ", e);
                    smapi.logAuthenticateDecryptIssue(context, smapiTransactionId, IdGatewayActivity.authId);
                    throw new SecureStorageException("IDTM SDK authentication: IDTM Keys Decryption Issue : " + e.getMessage());
                } catch (BadStatusCodeException e) {
                    int responseCode = e.getResponse().getResponseCode();
                    if (responseCode == 404) {
                        ErrorResponse errorResponse = gson.fromJson(
                                e.getResponse().getResponseErrorBodyString(), ErrorResponse.class);
                        printer.e("responseCode is 404 :", gson.toJson(errorResponse));
                        if (errorResponse != null && errorResponse.isSdkNotFound()) {
                            String referenceId = preferences.getString(Prefs.REFERENCE_ID);
                            preferences.clearAllPreferences();
                            preferences.set(Prefs.REFERENCE_ID, referenceId);
                            detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "NotInitializedException : " + gson.toJson(errorResponse));
                            smapi.logAuthenticateNotinitialised(context, smapiTransactionId, detailsMap);
                            throw new NotInitializedException();
                        } else {
                            detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmRetryException : " + gson.toJson(errorResponse));
                            smapi.logAuthenticateServerIssue(context, smapiTransactionId, IdGatewayActivity.authId,
                                    "Server returned 404", detailsMap);
                            throw new IdtmRetryException();
                        }
                    } else if (responseCode >= 500 && responseCode < 600) {
                        if (!emitter.isDisposed()) {
                            ErrorResponse errorResponse = gson.fromJson(
                                    e.getResponse().getResponseErrorBodyString(), ErrorResponse.class);
                            printer.e("Server temporary issue ", gson.toJson(errorResponse));
                            detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmTemporaryIssueException");
                            smapi.logAuthenticateBackendTempIssue(context, smapiTransactionId, gson.toJson(errorResponse), IdGatewayActivity.authId, detailsMap);
                            throw new IdtmTemporaryIssueException("Server temporary issue " + gson.toJson(errorResponse));
                        }
                    } else {
                        printer.e("Server error", e);
                        detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmRetryException");
                        smapi.logAuthenticateServerIssue(context, smapiTransactionId, IdGatewayActivity.authId,
                                "Unexpected response code from server: " + responseCode, detailsMap);
                        throw new IdtmRetryException();
                    }
                } catch (SocketTimeoutException se) {
                    detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "TimeOutRetriableException");
                    if (!autoRefresh) {
                        smapi.logCIAuthenticateSocketTimeOutIssue(context, smapiTransactionId, IdGatewayActivity.authId,
                                "SocketTimeoutException - " + se.getMessage(), detailsMap);
                    } else {
                        smapi.logARAuthenticateSocketTimeOutIssue(context, smapiTransactionId, IdGatewayActivity.authId,
                                "SocketTimeoutException - " + se.getMessage(), detailsMap);
                    }
                    printer.e("SocketTimeoutException: ", se);
                    throw new TimeOutRetriableException();
                } catch (IOException e) {
                    printer.e("Server error", e);
                    detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmServerException");
                    smapi.logAuthenticateServerIssue(context, smapiTransactionId, IdGatewayActivity.authId,
                            "IOException - " + e.getMessage(), detailsMap);
                    throw new IdtmServerException("Authentication: IOException - " + e.getMessage());
                } catch (JOSEException | ParseException | JsonParseException e) {
                    printer.e("Server error", e);
                    detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmServerException");
                    smapi.logAuthenticateServerIssue(context, smapiTransactionId, IdGatewayActivity.authId,
                            "Unable to parse JSON result - " + e.getMessage(), detailsMap);
                    throw new IdtmServerException("Authentication: Unable to parse JSON result - " + e.getMessage());
                } catch (IDTMException e) {
                    // already handled
                    throw e;
                } catch (Exception e) {
                    detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmRetryException");
                    smapi.logAuthenticateUnexpectedException(context, smapiTransactionId, IdGatewayActivity.authId,
                            "Unexpected response code from server: " + e.getMessage(), detailsMap);
                    throw new IdtmRetryException();
                }


            }
            if (accessToken != null && accessToken.getToken() != null && !accessToken.getToken().isEmpty()) {
                dataCrypt.set(Prefs.REFRESH_COUNTER, -1);
                if (allowAutoRefresh) {
                    scheduleOrCancelAutoRefreshToken(false);
                } else {
                    printer.d("Scheduling auto refresh not allowed after accessToken success ");
                }

                smapi.logAuthenticateSuccess(context, smapiTransactionId, IdGatewayActivity.authId, detailsMap);
                emitter.onNext(new AuthenticateProgress(false, accessToken));
                emitter.onComplete();
            } else {
                throw new IdtmRetryException();
            }
        } catch (IDTMException e) {
            if (!(e instanceof IdtmInProgressException)) {
                printer.d("Release semaphore in error handler");
                idtmSemaphore.release();
            }
            throw e;
        } catch (Exception e) {
            Map<String, String> detailsMap = new HashMap<>();
            detailsMap.put(ISmapiConst.PAYLOAD_CLASS_NAME, "AuthenticateObservable");
            detailsMap.put(ISmapiConst.PAYLOAD_METHOD_NAME, "subscribe()");
            detailsMap.put(ISmapiConst.PAYLOAD_SDK_ID, sdkId);
            detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "UnexpectedException");
            smapi.logAuthenticateUnexpectedException(context, smapiTransactionId, IdGatewayActivity.authId,
                    "Unexpected exception during authenticate: " + e.getMessage(), detailsMap);

            if (!(e instanceof IdtmInProgressException)) {
                printer.d("Release semaphore in error handler");
                idtmSemaphore.release();
            }

            throw new IdtmRetryException();
        }

        printer.d("Releasing semaphore on success");
        idtmSemaphore.release();
    }

    private AccessToken newAccessToken(String clientId, String sdkId, SecretKey serverSymmetricKey,
                                       String sdkIdJwtToken,
                                       ObservableEmitter<AuthenticateProgress> emitter)
            throws BadStatusCodeException, IOException, AesException, JOSEException, ParseException,
            JsonParseException, UserCanceledException, IdtmServerException, IdtmRetryException {

        idGatewaySyncer.reset();
        printer.i("Get nonce API");

        Map<String, String> detailsMap = new HashMap<>();
        detailsMap.put(ISmapiConst.PAYLOAD_CLASS_NAME, "AuthenticateObservable");
        detailsMap.put(ISmapiConst.PAYLOAD_METHOD_NAME, "newAccessToken()");
        detailsMap.put(ISmapiConst.PAYLOAD_SDK_ID, sdkId);

        smapi.logNonceRequest(context, smapiTransactionId, detailsMap);

        JwtResponse jwtResponse = Response.retrieve(JwtResponse.class,
                idtmApi.getNonce(clientId, sdkIdJwtToken, sdkId));
        // TODO: Maybe the following response will be encrypted
        // String jsonResponse = JwtHelper.aesDecrypt(serverSymmetricKey, jwtResponse.getData());
        String nonce = jwtResponse.getData();
        printer.i("Nonce: ", nonce);

        // Do not open the IDGateway view if app is in background
        ActivityManager.RunningAppProcessInfo myProcess = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(myProcess);
        if (myProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
            // User has put app in background - this is the same as if user has cancelled login
            detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "UserCanceledException");
            smapi.logAuthenticateUserCancelled(context, smapiTransactionId, "User has put app in background - this is the same as if user has cancelled login", IdGatewayActivity.authId, detailsMap);
            throw new UserCanceledException("IDTM SDK authentication: User has put app in background, so webview cannot be triggered");
        }

        printer.i("Opening the ID Gateway");
        String idGatewayRedirectUrl = dataCrypt.getString(Prefs.ID_GATEWAY_REDIRECT_URL);
        String mobileAcrValues = dataCrypt.getString(Prefs.ID_GATEWAY_MOBILE_ACR_VALUES);
        String wifiAcrValues = dataCrypt.getString(Prefs.ID_GATEWAY_WIFI_ACR_VALUES);
        String scope = dataCrypt.getString(Prefs.ID_GATEWAY_SCOPE);
        String loginHint = dataCrypt.getString(Prefs.LOGIN_HINT);
        String idGatewayUrl = environment.getIdgatewayUrl(clientId, nonce,
                idGatewayRedirectUrl, mobileAcrValues, wifiAcrValues, scope, loginHint);
        printer.i("URL: " + idGatewayUrl);
        IdGatewayActivity.start(context, idGatewayUrl);
        try {
            idGatewaySyncer.waitForStart();
        } catch (InterruptedException e) {
            detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmServerException");
            smapi.logNonceBackendFailure(context, smapiTransactionId, e.getMessage(), detailsMap);
            throw new IdtmServerException("Authentication: backend failure while fetching nonce - " + e.getMessage());
        }

        smapi.logWebviewStarted(context, smapiTransactionId, detailsMap);
        emitter.onNext(new AuthenticateProgress(true, null));

        try {
            idGatewaySyncer.waitForRedirectToAuth();
            smapi.logWebviewRedirectedToAuth(context, smapiTransactionId, IdGatewayActivity.authId, detailsMap);
        } catch (InterruptedException e) {
            detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmServerException");
            smapi.logWebviewRedirectedToAuthFailure(context, smapiTransactionId,
                    IdGatewayActivity.authId, e.getMessage(), detailsMap);
            throw new IdtmServerException("Authentication: redirection to IDGW failed - " + e.getMessage());
        }

        try {
            idGatewaySyncer.waitForFinish();
        } catch (InterruptedException e) {
            detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmServerException");
            smapi.logIdGatewaySyncAuthFailure(context, smapiTransactionId,
                    IdGatewayActivity.authId, e.getMessage(), detailsMap);
            throw new IdtmServerException("Authentication: Idgw sync failed - " + e.getMessage());
        }

        smapi.logWebviewFinished(context, smapiTransactionId, IdGatewayActivity.authId, detailsMap);
        emitter.onNext(new AuthenticateProgress(false, null));
        if (idGatewaySyncer.isSuccess()) {
            printer.i("Get token API");
            smapi.logAccessTokenRequest(context, smapiTransactionId, null, idGatewaySyncer.getCode(), nonce);
            GetAccessTokenBody getAccessTokenBody = new GetAccessTokenBody(sdkId, nonce,
                    idGatewaySyncer.getCode(), idGatewayRedirectUrl);
            jwtResponse = Response.retrieve(JwtResponse.class,
                    idtmApi.getAccessToken(clientId, sdkIdJwtToken, sdkId, getAccessTokenBody));
            smapi.logAuthenticateBackendSuccess(context, smapiTransactionId, IdGatewayActivity.authId, detailsMap);
            String jsonResponse = JwtHelper.aesDecrypt(serverSymmetricKey, jwtResponse.getData());
            printer.i("Decrypted response newAccessToken: ", jsonResponse);
            AccessTokenResponse.OauthToken oauthToken = gson.fromJson(jsonResponse,
                    AccessTokenResponse.class).getOauthToken();
            printer.i("Saving data newAccessToken");

            printer.i("Current refresh token: ", oauthToken.getRefreshToken());
            if (TextUtils.isEmpty(oauthToken.getRefreshToken())) {
                //refresh token returned empty from apix: log smapi
                smapi.logRefreshTokenEmptyOrNullFromApix(context, smapiTransactionId, detailsMap);
                throw new IdtmRetryException("IDTM SDK authentication: Refresh token is null or empty from APIX while login");
            }

            dataCrypt.set(Prefs.ACCESS_TOKEN, oauthToken.getAccessToken());
            dataCrypt.set(Prefs.ACCESS_TOKEN_TYPE, oauthToken.getTokenType());
            dataCrypt.set(Prefs.REFRESH_TOKEN, oauthToken.getRefreshToken());
            dataCrypt.set(Prefs.ACCESS_TOKEN_REFRESH_TOKEN, oauthToken.getRefreshToken());
            dataCrypt.set(Prefs.ACCESS_TOKEN_EXPIRE_TIME_MS, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(oauthToken.getExpiresIn()));
            dataCrypt.set(Prefs.ACCESS_TOKEN_SUB, oauthToken.getSub());
            dataCrypt.set(Prefs.ACCESS_TOKEN_ACR, oauthToken.getAcr());
            String createdAt = Utils.getHumanReadableDate(System.currentTimeMillis());
            dataCrypt.set(Prefs.REFRESH_TOKEN_GENERATED_AT, createdAt);
            printer.i("Refresh token generated at: ", createdAt);
            if (!TextUtils.isEmpty(oauthToken.getRefreshToken())) {
                detailsMap.put(ISmapiConst.PAYLOAD_REFRESH_TOKEN, oauthToken.getRefreshToken().substring(oauthToken.getRefreshToken().length() - 6));//send last 6 character of refresh token to smapi
            }
            detailsMap.put(ISmapiConst.PAYLOAD_REFRESH_TOKEN_REQUESTED_AT, createdAt);

            printer.i("autoRefresh value: ", autoRefresh);
            if (!autoRefresh) {
                smapi.logCIAuthenticateAccessTokenGenerated(context, smapiTransactionId, createdAt, IdGatewayActivity.authId, detailsMap);
            } else {
                smapi.logARAuthenticateAccessTokenGenerated(context, smapiTransactionId, createdAt, IdGatewayActivity.authId, detailsMap);
            }

            return new AccessToken(oauthToken.getAccessToken(), oauthToken.getTokenType(), oauthToken.getSub());

        } else if (idGatewaySyncer.isCanceled()) {
            detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "UserCanceledException");
            smapi.logAuthenticateUserCancelled(context, smapiTransactionId, idGatewaySyncer.getError(), IdGatewayActivity.authId, detailsMap);
            throw new UserCanceledException("IDTM SDK authentication: User cancelled webview authentication : " + idGatewaySyncer.getError());
        } else if (idGatewaySyncer.isUserFailedToLogin()) {
            detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "UserCanceledException");
            smapi.logAuthenticateUserFailedToLogin(context, smapiTransactionId, idGatewaySyncer.getError(), IdGatewayActivity.authId, detailsMap);
            throw new UserCanceledException("IDTM SDK authentication: User failed to login during webview authentication : " + idGatewaySyncer.getError());
        } else if (idGatewaySyncer.invalidScope()) {
            detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmServerException: " + idGatewaySyncer.getError());
            smapi.logAuthenticateUserFailedToLogin(context, smapiTransactionId, idGatewaySyncer.getError(), IdGatewayActivity.authId, detailsMap);
            throw new IdtmServerException("IDTM SDK authentication: User failed to login during webview authentication as : " + idGatewaySyncer.getError());
        } else {
            detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmServerException: " + idGatewaySyncer.getError());
            smapi.logAuthenticateServerIssue(context, smapiTransactionId,
                    IdGatewayActivity.authId, "Issue on IDGW - Login result is neither success nor cancelled", detailsMap);
            throw new IdtmServerException("Authentication: Issue on IDGW - Login result is neither success nor cancelled: " + idGatewaySyncer.getError());
        }
    }

    private AccessToken refreshAccessToken(String clientId, String sdkId,
                                           SecretKey serverSymmetricKey, String sdkIdJwtToken,
                                           ObservableEmitter<AuthenticateProgress> emitter)
            throws BadStatusCodeException, IOException, AesException, JOSEException, ParseException,
            JsonParseException, UserCanceledException, IdtmServerException, IdtmRetryException, SecureStorageException, IdGatewayRequiredException, IdtmIgnoreException {
        try {
            Map<String, String> dMap = new HashMap<>();
            dMap.put(ISmapiConst.PAYLOAD_CLASS_NAME, "AuthenticateObservable");
            dMap.put(ISmapiConst.PAYLOAD_METHOD_NAME, "refreshAccessToken()");
            dMap.put(ISmapiConst.PAYLOAD_SDK_ID, sdkId);

            if (!autoRefresh) {
                smapi.logCIRefreshToken(context, smapiTransactionId, dMap);
            } else {
                smapi.logRefreshToken(context, smapiTransactionId, dMap);
            }

            // Get current refresh token
            String refreshToken = dataCrypt.getString(Prefs.REFRESH_TOKEN);
            printer.d("Stored refreshToken Prefs.REFRESH_TOKEN: " + refreshToken);
            // Backward compatibility
            if (TextUtils.isEmpty(refreshToken)) {
                refreshToken = dataCrypt.getString(Prefs.ACCESS_TOKEN_REFRESH_TOKEN);
                printer.d("Stored refreshToken Prefs.ACCESS_TOKEN_REFRESH_TOKEN: " + refreshToken);
            }
            if (TextUtils.isEmpty(refreshToken)) {
                //refresh token returned empty from data storage: log smapi
                smapi.logRefreshTokenEmptyOrNullInDataStore(context, smapiTransactionId, dMap);
                printer.d("Stored refreshToken is empty: login again");
                AccessToken accessToken = newAccessToken(clientId, sdkId, serverSymmetricKey, sdkIdJwtToken, emitter);
                // If login was successful, return access token
                if (accessToken != null) {
                    return accessToken;
                }

            }

            // Prepare request
            RefreshAccessTokenBody body = new RefreshAccessTokenBody(sdkId, refreshToken);
            printer.i("Refresh token API");

            // Call refresh token API
            JwtResponse jwtResponse = Response.retrieve(JwtResponse.class,
                    idtmApi.refreshAccessToken(clientId, sdkIdJwtToken, sdkId, body));

            // Evaluate response
            String jsonResponse = JwtHelper.aesDecrypt(serverSymmetricKey, jwtResponse.getData());
            printer.i("Decrypted response refreshAccessToken: ", jsonResponse);
            AccessTokenResponse.OauthToken oauthToken = gson.fromJson(jsonResponse,
                    AccessTokenResponse.class).getOauthToken();

            printer.i("Current refresh token in refreshAccessToken api: ", oauthToken.getRefreshToken());
            if (TextUtils.isEmpty(oauthToken.getRefreshToken())) {
                //refresh token returned empty from apix: log smapi
                smapi.logRefreshTokenEmptyOrNullFromApix(context, smapiTransactionId, dMap);
                throw new IdtmRetryException("IDTM SDK authentication: Refresh token is null or empty from APIX while fetching refreshToken");
            }

            // Store new tokens
            printer.i("Saving data refreshAccessToken");
            dataCrypt.set(Prefs.ACCESS_TOKEN, oauthToken.getAccessToken());
            dataCrypt.set(Prefs.ACCESS_TOKEN_TYPE, oauthToken.getTokenType());
            dataCrypt.set(Prefs.REFRESH_TOKEN, oauthToken.getRefreshToken());
            dataCrypt.set(Prefs.ACCESS_TOKEN_REFRESH_TOKEN, oauthToken.getRefreshToken());
            dataCrypt.set(Prefs.ACCESS_TOKEN_EXPIRE_TIME_MS, System.currentTimeMillis() +
                    TimeUnit.SECONDS.toMillis(oauthToken.getExpiresIn()));

            String createdAt = Utils.getHumanReadableDate(System.currentTimeMillis());
            dataCrypt.set(Prefs.REFRESH_TOKEN_GENERATED_AT, createdAt);
            dMap.put(ISmapiConst.PAYLOAD_REFRESH_TOKEN_REQUESTED_AT, createdAt);
            if (!TextUtils.isEmpty(oauthToken.getRefreshToken())) {
                dMap.put(ISmapiConst.PAYLOAD_REFRESH_TOKEN, oauthToken.getRefreshToken().substring(oauthToken.getRefreshToken().length() - 6));//send last 6 character of refresh token to smapi
            }

            if (!autoRefresh) {
                smapi.logAuthenticateCIRefreshTokenSuccess(context, smapiTransactionId, createdAt, dMap);
            } else {
                smapi.logAuthenticateRefreshTokenSuccess(context, smapiTransactionId, createdAt, dMap);
            }
            return new AccessToken(oauthToken.getAccessToken(), oauthToken.getTokenType(), dataCrypt.getString(Prefs.ACCESS_TOKEN_SUB));
        }

        // We received a response from the backend with response status != 200
        // - If http 400 OR Http 500 with (UserRedirectRequiredException && NOT initiated by autoRefresh):
        //     Handle as non-retriable issue...
        //     - Invalidate all tokens
        //     - If IDGateway is allowed: Ask customer to log in again
        //     - If IDGateway is not allowed and call was not initiated by auto refresh: Return IDGatewayRequiredException
        //     - If IDGateway is not allowed and call was not initiated by auto refresh: Stop auto refresh task
        // - If http (500 without UserRedirectRequiredException) or (500 with UserRedirectRequiredException but was initiated by autoRefresh)
        //     Handle as retriable exception...
        //     - Set / increase retry counter
        //     - If retry-counter <  Max retries AND call was not initiated by auto refresh:
        //          Return IDTMRetryException
        //     - If retry-counter <  Max retries AND call was initiated by auto refresh:
        //          Schedule next auto refresh
        //     - If retry-counter >=  Max retries AND call was not initiated by auto refresh:
        //          - Invalidate all tokens
        //          - If IDGateway is allowed: Ask customer to log in again
        //          - If IDGateway is not allowed: Return IDGatewayRequiredException
        //     - If retry-counter >=  Max retries AND call was initiated by auto refresh:
        //          - Invalidate all tokens
        //          - Stop auto refresh task
        catch (BadStatusCodeException e) {

            boolean nonRetriableError = false;

            int responseCode = 500;
            String errorResponseBody = "";
            String errorDescription = "";

            if (e.getResponse() != null) {
                responseCode = e.getResponse().getResponseCode();
                errorResponseBody = e.getResponse().getResponseErrorBodyString();
                errorDescription = "Code: " + responseCode + ", Body: " + errorResponseBody;
            }

            printer.e("APIX returns error code while refreshing access token: ", errorDescription);
            printer.e("autoRefresh value in exception : " + autoRefresh);

            Map<String, String> detailsMap = new HashMap<>();
            detailsMap.put(ISmapiConst.PAYLOAD_CLASS_NAME, "AuthenticateObservable");
            detailsMap.put(ISmapiConst.PAYLOAD_METHOD_NAME, "refreshAccessToken()");
            detailsMap.put(ISmapiConst.PAYLOAD_SDK_ID, sdkId);

            if (!autoRefresh) {
                smapi.logCIAuthenticateRefreshTokenAPIXError(context, smapiTransactionId, errorDescription, detailsMap);
            } else {
                smapi.logAuthenticateRefreshTokenAPIXError(context, smapiTransactionId, errorDescription, detailsMap);
            }

            // Check if error is retriable or not
            if (responseCode == 400) {
                printer.e("APIX error is a non-retriable error");
                detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "APIX error is a non-retriable error code: 400");
                if (!autoRefresh) {
                    smapi.logCIAuthenticateRefreshTokenNonRetriableError(context, smapiTransactionId, errorDescription, detailsMap);
                } else {
                    smapi.logAuthenticateRefreshTokenNonRetriableError(context, smapiTransactionId, errorDescription, detailsMap);
                }
                nonRetriableError = true;
            } else {
                // Check if response contains UserRedirectRequiredException and intiated by client
                if (errorResponseBody != null && errorResponseBody.contains("UserRedirectRequiredException") && !autoRefresh) {
                    printer.e("APIX error is a non-retriable error");
                    detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "APIX error is a non-retriable error: UserRedirectRequiredException");
                    smapi.logCIAuthenticateRefreshTokenNonRetriableError(context, smapiTransactionId, errorDescription, detailsMap);
                    nonRetriableError = true;
                }
            }

            // Check retry counter and if necessary, turn retriable error into non-retriable error
            if (!nonRetriableError && !autoRefresh) {
                printer.e("APIX error is a retriable error");
                long counter = dataCrypt.getLong(Prefs.REFRESH_COUNTER, -1);
                if (counter == -1) {
                    dataCrypt.set(Prefs.REFRESH_COUNTER, 1);
                } else {
                    dataCrypt.set(Prefs.REFRESH_COUNTER, counter + 1);
                }
                printer.d("Retry count: ", counter);
                // Max retries - handle like non-retriable error
                if (counter + 1 >= MAX_RETRY_COUNT) {
                    printer.e("APIX error is a retriable error, but retry counter has reached max attempts. Handling as non-retriable error, counter value: " + counter);
                    detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "APIX error is a retriable error, but retry counter has reached max attempts, counter value: " + counter);
                    if (!autoRefresh) {
                        smapi.logCIAuthenticateRefreshTokenRetriableErrorMaxRetryCount(context, smapiTransactionId, detailsMap);
                    } else {
                        smapi.logAuthenticateRefreshTokenRetriableErrorMaxRetryCount(context, smapiTransactionId, detailsMap);
                    }
                    nonRetriableError = true;
                }
            }

            if (nonRetriableError) {
                // Invalidate tokens
                printer.d("Invalidate tokens as nonRetriableError.... autoRefresh :" + autoRefresh);

                RevokeTokens revokeTokens = new RevokeTokens(context, preferences, printer, dataCrypt, smapi, environment);
                if (autoRefresh) {
                    revokeTokens.startLogout("autoRefresh");
                } else {
                    revokeTokens.startLogout("client-initiated");
                }

                printer.d("allowIdGateway value: " + allowIdGateway);
                // Ask user to log in again if possible
                if (allowIdGateway) {
                    printer.d("Ask customer to log in again");
                    if (!autoRefresh) {
                        smapi.logCIAuthenticateRefreshTokenNewManualLogin(context, smapiTransactionId, errorDescription, detailsMap);
                    } else {
                        smapi.logARAuthenticateRefreshTokenNewManualLogin(context, smapiTransactionId, errorDescription, detailsMap);
                    }

                    AccessToken accessToken = newAccessToken(clientId, sdkId, serverSymmetricKey, sdkIdJwtToken, emitter);

                    // If login was successful, return access token
                    // Handling of refresh counter and auto-refresh is done in calling method
                    if (accessToken != null) {
                        return accessToken;
                    }
                    // If login was not successful
                    else {
                        printer.d("Log in was not successful.");
                        detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmServerException");
                        if (!autoRefresh) {
                            smapi.logCIAuthenticateRefreshTokenManualLoginFailed(context, smapiTransactionId, detailsMap);
                        } else {
                            smapi.logARAuthenticateRefreshTokenManualLoginFailed(context, smapiTransactionId, detailsMap);
                        }
                        printer.e("Throwing IdtmServerException");
                        throw new IdtmServerException("Authentication: Initiating new login as refresh token failed as non-retriable");
                    }
                } else {
                    // if call was initiated by container app, return IDGatewayRequiredException
                    if (!autoRefresh) {
                        printer.e("Cannot handle non-retriable error gracefully because IDGateway is not allowed.");
                        detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdGatewayRequiredException");
                        smapi.logAuthenticateRefreshTokenNonRetriableNoGatewayAllowed(context, smapiTransactionId, detailsMap);
                        throw new IdGatewayRequiredException("Client-initiated authorization: Cannot handle non-retriable error gracefully because IDGateway is not allowed");
                    }
                    // otherwise we have a non-retriable error caused by auto-refresh
                    else {
                        // Do nothing
                        printer.d("Non-retriable exception during auto-refresh. Auto-refresh stopped.");
                        detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmIgnoreException");
                        smapi.logAuthenticateRefreshTokenNonRetriableDuringAutoLogin(context, smapiTransactionId, detailsMap);
                        // We must return a non-null value - otherwise next call will be blocked
                        throw new IdtmIgnoreException();
                    }
                }
            }
            // Handle retriable errors now
            else {
                // If call was initiated by container app, return IdtmRetryException
                if (!autoRefresh) {
                    printer.e("Retriable error - inform container app");
                    detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmRetryException");
                    smapi.logCIAuthenticateRefreshTokenRetriableError(context, smapiTransactionId, errorDescription, detailsMap);
                    throw new IdtmRetryException();
                }
                // If call was done by auto-refresh - schedule next refresh timer
                else {
                    printer.e("Retriable error during auto-refresh - schedule next attempt");
                    detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmIgnoreException");
                    smapi.logAuthenticateRefreshTokenRetriableErrorDuringAutoRefresh(context, smapiTransactionId, detailsMap);
                    //SMAPI: Retriable error during auto-refresh - schedule next attempt
                    if (allowAutoRefresh) {
                        scheduleOrCancelAutoRefreshToken(true);
                    } else {
                        printer.d("Scheduling auto refresh not allowed after Retriable error ");
                    }
                    // We must return a non-null value - otherwise next call will be blocked
                    throw new IdtmIgnoreException();
                }
            }
        }
        // Handle AES exception separately
        // If call was not initiated by auto refresh: return SecureStorageException
        // If call was initiated by auto refresh: Schedule next retry
        catch (AesException e1) {
            printer.e("Error AesException ", e1);
            Map<String, String> detailsMap = new HashMap<>();
            detailsMap.put(ISmapiConst.PAYLOAD_CLASS_NAME, "AuthenticateObservable");
            detailsMap.put(ISmapiConst.PAYLOAD_METHOD_NAME, "refreshToken()");
            detailsMap.put(ISmapiConst.PAYLOAD_SDK_ID, sdkId);
            detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "SecureStorageException");

            if (!autoRefresh) {
                smapi.logCIAuthenticateRefreshTokenAESException(context, smapiTransactionId, detailsMap);
                throw new SecureStorageException("Client-initiated authorization: AES exception during token refresh : " + e1.getMessage());
            } else {
                smapi.logARAuthenticateRefreshTokenAESException(context, smapiTransactionId, detailsMap);
                // try again
                if (allowAutoRefresh) {
                    scheduleOrCancelAutoRefreshToken(true);
                } else {
                    printer.d("Scheduling auto refresh not allowed after AesException");
                }
                // We must return a non-null value - otherwise next call will be blocked
                throw new IdtmIgnoreException();
            }
        }
        // Any other exception is a server exception that is retriable and does not reqiure a retry counter
        // If call was not initiated by auto refresh: return IDTM retry exceptiom
        // If call was initiated by auto refresh: Schedule next retry
        catch (Exception e) {
            printer.e("Server error", e);
            Map<String, String> detailsMap = new HashMap<>();
            detailsMap.put(ISmapiConst.PAYLOAD_CLASS_NAME, "AuthenticateObservable");
            detailsMap.put(ISmapiConst.PAYLOAD_METHOD_NAME, "refreshToken()");
            detailsMap.put(ISmapiConst.PAYLOAD_SDK_ID, sdkId);
            if (!autoRefresh) {
                detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IdtmRetryException");
                smapi.logAuthenticateServerIssue(context, smapiTransactionId, IdGatewayActivity.authId, "Unexpected exception during refresh: " + e.getMessage(), detailsMap);
                throw new IdtmRetryException();
            } else {
                // try again
                if (allowAutoRefresh) {
                    scheduleOrCancelAutoRefreshToken(true);
                } else {
                    printer.d("Scheduling auto refresh not allowed after IdtmRetryException");
                }
                // We must return a non-null value - otherwise next call will be blocked
                throw new IdtmIgnoreException();
            }
        }
    }

    private void scheduleOrCancelAutoRefreshToken(boolean retry) {
        printer.d("scheduleOrCancelAutoRefreshToken - retry flag = ", retry);

        Map<String, String> detailsMap = new HashMap<>();
        detailsMap.put(ISmapiConst.PAYLOAD_CLASS_NAME, "AuthenticateObservable");
        detailsMap.put(ISmapiConst.PAYLOAD_METHOD_NAME, "scheduleOrCancelAutoRefreshToken()");
        detailsMap.put(ISmapiConst.PAYLOAD_SDK_ID, sdkId);

        try {
            if (!retry) {
                long accessTokenExpireTimeMs = dataCrypt.getLong(Prefs.ACCESS_TOKEN_EXPIRE_TIME_MS, Long.MIN_VALUE);
                //Set alarm for auto refresh once we have access-refresh token, time set as : expiry minus 4min
                String alarmTime = Utils.getHumanReadableDate(accessTokenExpireTimeMs - AUTO_REFRESH_PRE_TIME_SEC * 1000L);
                smapi.logAuthenticateAutoRefreshTokenJob(context, smapiTransactionId, alarmTime, detailsMap);
                printer.d("Token generated, setting AutoRefresh alarm at : " + alarmTime);
                AutoRefreshTokenJobService.setAlarm(context, accessTokenExpireTimeMs - AUTO_REFRESH_PRE_TIME_SEC * 1000L);
            } else {
                long currentTime = System.currentTimeMillis();
                //Set alarm for auto refresh as there was error earlier, time set as : 12 hrs from current time
                AutoRefreshTokenJobService.setAlarm(context, currentTime + 720L * 60000L); //12 Hr
            }
        } catch (AesException e) {
            detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "AesException");
            smapi.logAuthenticateRefreshTokenJobError(context, smapiTransactionId, e.getMessage(), detailsMap);
            printer.e("Error while setting the auto refresh ", e);
        }
    }
}
