package com.vodafone.idtmlib.lib.utils;


import android.content.Context;

import com.google.common.collect.ImmutableMap;
import com.vodafone.idtmlib.BuildConfig;
import com.vodafone.lib.seclibng.SecLib;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Smapi implements ISmapiConst {

    private static final String USECASE = "x-vf-trace-usecase-id";
    private static final String TRANSACTION = "x-vf-trace-transaction-id";
    private static final String AUTHORIZATION = "x-vf-trace-authorization-id";
    private static final String IDTM_VERSION = "x-vf-trace-idtm-version";
    private static final String DEVICE_NAME = "deviceName";
    private static final String OS_VERSION = "deviceOSVersion";

    //init usecases

    //init transaction

    private static final String PAGE_NAME = "NA";
    private static final String SUB_PAGE = "NA";
    private Printer printer;

    @Inject
    public Smapi(Printer printer, Context context) {
        // SecLib must be initialized by the main app
        this.printer = printer;
    }

    public void logEventNG(Context context, String eventElement, String eventDescription, String pageName, String subPage,
                           String useCase, String transaction, String authId, Map<String, String> payload) {
        printer.d("logEventNG SecLibNG eventEle: " + eventElement);
        printer.d("logEventNG SecLibNG eventDescp: " + eventDescription);
        printer.d("logEventNG SecLibNG authId: " + authId);
        try {
            //  Class.forName("com.vodafone.lib.seclibng.SecLibNG");
            //Adding custom payload
            Map<String, Object> eventMapObj = new HashMap<>();
            eventMapObj.put(USECASE, useCase);
            eventMapObj.put(IDTM_VERSION, BuildConfig.VERSION_NAME);
            eventMapObj.put(TRANSACTION, transaction);
            if (authId != null) {
                eventMapObj.put(AUTHORIZATION, authId);
            }
            eventMapObj.put(DEVICE_NAME, Device.getDeviceModel());
            eventMapObj.put(OS_VERSION, "Android " + Device.getOSVersion());

            if (payload != null) {
                for (Map.Entry<String, String> entry : payload.entrySet()) {
                    eventMapObj.put(entry.getKey(), entry.getValue());
                }
            }

            SecLib.getInstance().logCustomEvent(eventElement, eventDescription, pageName, subPage, eventMapObj);

        } catch (Exception e) {
            e.printStackTrace();
            printer.e(e.getMessage());
        }
    }

    /******** Generic Smapi events ************/
    public void logKeystoreIssue(Context context, String transactionID, String errorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_INIT, EVENT_KEYSTORE_ISSUE + errorDescription, PAGE_NAME, SUB_PAGE, USECASE_GENERIC, transactionID, null, payload);
    }

    /******** Init Smapi events ************/

    public void logInitStarted(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_INIT, EVENT_INIT_STARTED, PAGE_NAME, SUB_PAGE,
                USECASE_INIT_STARTED, transactionID, null, payload);
    }

    public void logNoNetworkConnectionInit(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_INIT, EVENT_INIT_UNABLE_TO_CONNECT_TO_NETWORK, PAGE_NAME, SUB_PAGE, USECASE_INIT_NETWORK, transactionID, null, payload);
    }

    public void logNoNetworkConnectionAuth(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_AUTHENTICATE_UNABLE_TO_CONNECT_TO_NETWORK, PAGE_NAME, SUB_PAGE, USECASE_INIT_NETWORK, transactionID, null, payload);
    }

    public void logNoNetworkConnectionRevokeToken(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_INIT, EVENT_REVOKE_UNABLE_TO_CONNECT_TO_NETWORK, PAGE_NAME, SUB_PAGE, USECASE_INIT_NETWORK, transactionID, null, payload);
    }

    public void logInitIssueGenerateProofId(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_INIT, EVENT_INIT_UNABLE_TO_GENERATE_PROOF_ID, PAGE_NAME, SUB_PAGE, USECASE_INIT_PROOFID, transactionID, null, payload);
    }

    public void logInitIssueInitializeFirebase(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_INIT, EVENT_INIT_UNABLE_TO_INITIALIZE_FIREBASE, PAGE_NAME, SUB_PAGE, USECASE_INIT_PROOFID, transactionID, null, payload);
    }

    public void logInitGenerateInstance(Context context, String transactionID) {
        logEventNG(context, EVENT_INIT, EVENT_INIT_GENERATE_INSTANCE, PAGE_NAME, SUB_PAGE, USECASE_INIT_GEN_INSTANCE, transactionID, null, null);
    }

    public void logInitAlreadyInit(Context context, String transactionID) {
        logEventNG(context, EVENT_INIT, EVENT_INIT_ALREADY_INITIALIZED, PAGE_NAME, SUB_PAGE, USECASE_INIT_ALREADY_INIT, transactionID, null, null);
    }

    public void logInitCallbackInitialised(Context context, String transactionID) {
        logEventNG(context, EVENT_INIT, EVENT_INIT_CALL_BACKEND_INITIALIZE, PAGE_NAME, SUB_PAGE, USECASE_INIT_BACKEND_INIT, transactionID, null, null);
    }

    public void logInitBackendInitSuccess(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_INIT, EVENT_INIT_BACKEND_INITIALIZE_SUCCESS, PAGE_NAME, SUB_PAGE, USECASE_INIT_SUCCESS, transactionID, null, payload);
    }

    public void logInitBackendInitIOException(Context context, String transactionID, String errorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_INIT, EVENT_INIT_BACKEND_INITIALIZE_IOEXCEPTION + "\n Error Description: " + errorDescription, PAGE_NAME, SUB_PAGE, USECASE_INIT_FAILURE, transactionID, null, payload);
    }

    public void logInitBackendInitFailure(Context context, String transactionID, String errorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_INIT, EVENT_INIT_BACKEND_INITIALIZE_FAILURE + "\n Error Description: " + errorDescription, PAGE_NAME, SUB_PAGE, USECASE_INIT_FAILURE, transactionID, null, payload);
    }

    public void logInitBackendSdkNotFound(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_INIT, EVENT_INIT_BACKEND_INITIALIZE_SDK_NOT_FOUND, PAGE_NAME, SUB_PAGE, USECASE_INIT_SDK_NOT_FOUND, transactionID, null, payload);
    }

    public void logInitBackendTempIssue(Context context, String transactionID, String errorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_INIT, EVENT_INIT_BACKEND_INITIALIZE_TEMPORARY_ISSUE + "\n Error Description: " + errorDescription, PAGE_NAME, SUB_PAGE, USECASE_INIT_BACKEND_TEMP_ISSUE, transactionID, null, payload);
    }

    public void logInitUnexpectedException(Context context, String transactionID,
                                           String errorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_INIT, EVENT_INIT_UNEXPECTED_EXCEPTION + "\n Error Description: " + errorDescription, PAGE_NAME,
                SUB_PAGE, USECASE_INIT_FAILURE, transactionID, null, payload);
    }

    public void logInitCertificateIssueInWebview(Context context, String transactionID, String errorDescription) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_AUTH_CERTIFICATE_ISSUE_WEBVIEW + "\n Error Description: " + errorDescription, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_WEBVIEW_CERTIFICATE_ISSUE,
                transactionID, null, null);
    }

    public void logCheckRefreshCertificateInWebview(Context context, String transactionID) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_AUTH_CHECK_REFRESH_CERTFICATE, PAGE_NAME, SUB_PAGE, USECASE_AUTH_CHECK_REFRESH_CERTFICATE,
                transactionID, null, null);
    }

    public void logExceptionInWebview(Context context, String transactionID, String errorDescription) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_AUTH_WEBVIEW_ISSUE + "\n Error Description: " + errorDescription, PAGE_NAME, SUB_PAGE, USECASE_AUTH_WEBVIEW_ISSUE,
                transactionID, null, null);
    }

    /******** Authenticate Smapi events ************/

    public void logAuthenticate(Context context, String transactionID, boolean value, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_AUTHENTICATE_STARTED + ":  allowAutoRefresh enable: " + value, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE,
                transactionID, null, payload);
    }

    public void logAuthenticateNotinitialised(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_AUTHENTICATE_INIT_NOT_INIT, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_INIT_NOT_INIT,
                transactionID, null, payload);
    }

    public void logAuthenticateCISempaphoreError(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_CI_AUTHENTICATE_SEMAPHORE_ACQUIRE, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_SEMAPHORE,
                transactionID, null, payload);
    }

    public void logAuthenticateARSempaphoreError(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_AR_AUTHENTICATE_SEMAPHORE_ACQUIRE, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_SEMAPHORE,
                transactionID, null, payload);
    }

    public void logAuthenticateSuccess(Context context, String transactionID, String authId, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_AUTHENTICATE_SYNC_SUCCESS, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_SUCCESS,
                transactionID, authId, payload);
    }

    public void logNonceRequest(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_NONCE_REQUEST_STARTED, EVENT_NONCE_REQUEST_STARTED, PAGE_NAME, SUB_PAGE, USECASE_NONCE_REQUEST,
                transactionID, null, payload);
    }

    public void logNonceBackendFailure(Context context, String transactionID, String errorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_NONCE_BACKEND_FAILURE, EVENT_NONCE_BACKEND_FAILURE + "\n Error Description: " + errorDescription, PAGE_NAME, SUB_PAGE, USECASE_NONCE_REQUEST,
                transactionID, null, payload);
    }

    public void logInitInvalidJson(Context context, String transactionID, String errorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_INIT, EVENT_INIT_BACKEND_INVALID_JSON + "\n Error Description: " + errorDescription, PAGE_NAME, SUB_PAGE, USECASE_INIT_INVALID_JSON, transactionID,
                null, payload);
    }


    public void logWebviewStarted(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_WEBVIEW_STARTED, EVENT_WEBVIEW_STARTED, PAGE_NAME, SUB_PAGE, USECASE_WEBVIEW_STARTED,
                transactionID, null, payload);
    }

    public void logWebviewRedirectedToAuth(Context context, String transactionID, String authId, Map<String, String> payload) {
        logEventNG(context, EVENT_IDGW_REDIRECT, EVENT_IDGW_REDIRECT, PAGE_NAME, SUB_PAGE, USECASE_WEBVIEW_STARTED,
                transactionID, authId, payload);
    }

    public void logWebviewRedirectedToAuthFailure(Context context, String transactionID, String authId, String errorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_IDGW_REDIRECT, EVENT_IDGW_REDIRECT_FAILURE + "\n Error Description: " + errorDescription, PAGE_NAME, SUB_PAGE, USECASE_WEBVIEW_STARTED,
                transactionID, authId, payload);
    }

    public void logIdGatewaySyncAuthFailure(Context context, String transactionID, String authId, String errorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_IDGW_REDIRECT, EVENT_IDGW_SYNC_FAILED + "\n Error Description: " + errorDescription, PAGE_NAME, SUB_PAGE, USECASE_WEBVIEW_STARTED,
                transactionID, authId, payload);
    }

    public void logWebviewFinished(Context context, String transactionID, String authId, Map<String, String> payload) {
        logEventNG(context, EVENT_WEBVIEW_FINISHED, EVENT_WEBVIEW_FINISHED, PAGE_NAME, SUB_PAGE, USECASE_WEBVIEW_FINISHED,
                transactionID, authId, payload);
    }

    public void logAccessTokenRequest(Context context, String transactionID, String authId, String code, String nonce) {
        logEventNG(context, EVENT_ACCESS_TOKEN_REQUEST, EVENT_ACCESS_TOKEN_REQUEST, PAGE_NAME, SUB_PAGE,
                USECASE_ACCESS_TOKEN_REQUEST, transactionID, authId,
                ImmutableMap.of("code", code, "nonce", nonce));
    }

    public void logAuthenticateBackendSuccess(Context context, String transactionID, String authId, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE_BACKEND_SUCCESS, EVENT_AUTHENTICATE_BACKEND_SUCCESS, PAGE_NAME, SUB_PAGE,
                USECASE_ACCESS_TOKEN_REQUEST, transactionID, authId,
                payload);
    }

    public void logARAuthenticateAccessTokenGenerated(Context context, String transactionID, String desc, String authId, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE_ACCESS_TOKEN_GENERATED, EVENT_AR_AUTHENTICATE_ACCESS_TOKEN_GENERATED + " at " + desc, PAGE_NAME, SUB_PAGE,
                USECASE_ACCESS_TOKEN_REQUEST, transactionID, authId,
                payload);
    }

    public void logCIAuthenticateAccessTokenGenerated(Context context, String transactionID, String desc, String authId, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE_ACCESS_TOKEN_GENERATED, EVENT_CI_AUTHENTICATE_ACCESS_TOKEN_GENERATED + " at " + desc, PAGE_NAME, SUB_PAGE,
                USECASE_ACCESS_TOKEN_REQUEST, transactionID, authId,
                payload);
    }

    public void logAuthorizeExisting(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHORIZE_EXISTING, EVENT_CI_EXISTING_TOKEN, PAGE_NAME, SUB_PAGE, USECASE_AUTHORIZE_EXISTING,
                transactionID, null, payload);
    }

    public void logAuthorizeExpired(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHORIZE_EXPIRED, EVENT_AUTHORIZE_EXPIRED, PAGE_NAME, SUB_PAGE, USECASE_AUTHORIZE_EXPIRED,
                transactionID, null, payload);
    }

    public void logAuthorizeInvalidToken(Context context, String transactionID, String authId, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHORIZE_INVALID_TOKEN, EVENT_AUTHORIZE_INVALID_TOKEN, PAGE_NAME, SUB_PAGE, USECASE_AUTHORIZE_FAILED,
                transactionID, authId, payload);
    }

    public void logRefreshToken(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_REFRESH_TOKEN, EVENT_AR_REFRESH_TOKEN, PAGE_NAME, SUB_PAGE, USECASE_REFRESH_TOKEN,
                transactionID, null, payload);
    }

    public void logCIRefreshToken(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_REFRESH_TOKEN, EVENT_CI_REFRESH_TOKEN, PAGE_NAME, SUB_PAGE, USECASE_REFRESH_TOKEN,
                transactionID, null, payload);
    }

    public void logAuthenticateIdGatewayRequired(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_AUTHENTICATE_IDGATEWAY_REQUIRED, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_IDGATEWAY_REQUIRED,
                transactionID, null, payload);
    }

    public void logAuthenticateBackendTempIssue(Context context, String transactionID, String ErrorDescription, String authId, Map<String, String> payload) {
        logEventNG(context, EVENT_INIT, EVENT_AUTHENTICATE_BACKEND_TEMP_ISSUE + " Error Description: " + ErrorDescription, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_BACKEND_TEMP_ISSUE,
                transactionID, authId, payload);
    }

    public void logAuthenticateCertificateIssue(Context context, String transactionID, String authId, Map<String, String> payload) {
        logEventNG(context, EVENT_INIT, EVENT_INIT_CERTIFICATE_ISSUE, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_CERTIFICATE_ISSUE,
                transactionID, authId, payload);
    }

    public void logAuthenticateCertificateIssueInAuth(Context context, String transactionID, String authId, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_AUTHENTICATE_CERTIFICATE_ISSUE, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_CERTIFICATE_ISSUE,
                transactionID, authId, payload);
    }

    public void logAuthenticateServerIssue(Context context, String transactionID, String authId, String ErrorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_CI_AUTHENTICATE_SERVER_ISSUE + " Error Description: " + ErrorDescription, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_SERVER_ISSUE,
                transactionID, authId, payload);
    }

    public void logAuthenticateUnexpectedException(Context context, String transactionID, String authId, String ErrorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_INIT, EVENT_AUTHENTICATE_UNEXPECTED_EXCEPTION + " Error Description: " + ErrorDescription, PAGE_NAME, SUB_PAGE, USECASE_AUTHORIZE_FAILED,
                transactionID, authId, payload);
    }

    public void logAuthenticateDecryptIssue(Context context, String transactionID, String authId) {
        logEventNG(context, EVENT_INIT, EVENT_AUTHENTICATE_DECRYPT_ISSUE, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_DECRYPT_ISSUE,
                transactionID, authId, null);
    }

    public void logAuthenticateUserCancelled(Context context, String transactionID, String errorDescription, String authId, Map<String, String> payload) {
        logEventNG(context, EVENT_INIT, EVENT_AUTHENTICATE_USER_CANCELLED + "\n Error Description: " +errorDescription, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_USER_CANCELLED,
                transactionID, authId, payload);
    }

    public void logAuthenticateUserFailedToLogin(Context context, String transactionID, String errorDescription, String authId, Map<String, String> payload) {
        logEventNG(context, EVENT_INIT, EVENT_AUTHENTICATE_USER_FAILED_TO_LOGIN + "\n Error Description: " + errorDescription, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_USER_CANCELLED,
                transactionID, authId, payload);
    }


    public void logAuthenticateRefreshTokenSuccess(Context context, String transactionID, String desc, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_AR_REFRESHTOKEN_SUCCESS + " at " + desc, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_REFRESHTOKEN_SUCCESS,
                transactionID, null, payload);
    }

    public void logAuthenticateRefreshTokenAPIXError(Context context, String transactionID, String errorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_AUTHENTICATE_REFRESHTOKEN_APIX_ERROR + "\n Error Description: " + errorDescription, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_REFRESHTOKEN_FAILED,
                transactionID, null, payload);
    }

    public void logAuthenticateRefreshTokenNonRetriableError(Context context, String transactionID, String errorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_AUTHENTICATE_REFRESHTOKEN_NON_RETRABLE_ERROR + " : " + errorDescription,
                PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_REFRESHTOKEN_FAILED,
                transactionID, null, payload);
    }

    public void logAuthenticateRefreshTokenRetriableErrorMaxRetryCount(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_AUTHENTICATE_REFRESHTOKEN_RETRABLE_MAX_RETRY_COUNT, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_REFRESHTOKEN_FAILED,
                transactionID, null, payload);
    }

    public void logCIAuthenticateRefreshTokenRetriableErrorMaxRetryCount(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_CI_AUTHENTICATE_REFRESHTOKEN_RETRABLE_MAX_RETRY_COUNT, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_REFRESHTOKEN_FAILED,
                transactionID, null, payload);
    }

    public void logCIAuthenticateRefreshTokenNewManualLogin(Context context, String transactionID, String description, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_CI_AUTHENTICATE_REFRESHTOKEN_NEW_MANUAL_LOGIN + " : " + description,
                PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_REFRESHTOKEN_FAILED,
                transactionID, null, payload);
    }

    public void logARAuthenticateRefreshTokenNewManualLogin(Context context, String transactionID, String description, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_AR_AUTHENTICATE_REFRESHTOKEN_NEW_MANUAL_LOGIN + " : " + description,
                PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_REFRESHTOKEN_FAILED,
                transactionID, null, payload);
    }

    public void logCIAuthenticateRefreshTokenManualLoginFailed(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_CI_AUTHENTICATE_REFRESHTOKEN_NEW_MANUAL_LOGIN_FAILED, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_REFRESHTOKEN_FAILED,
                transactionID, null, payload);
    }

    public void logARAuthenticateRefreshTokenManualLoginFailed(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_AR_AUTHENTICATE_REFRESHTOKEN_NEW_MANUAL_LOGIN_FAILED, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_REFRESHTOKEN_FAILED,
                transactionID, null, payload);
    }

    public void logAuthenticateRefreshTokenNonRetriableNoGatewayAllowed(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_INIT, EVENT_CI_AUTHENTICATE_REFRESHTOKEN_NON_RETIABLE_NO_GATEWAY_ALLOWED, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_REFRESHTOKEN_FAILED,
                transactionID, null, payload);
    }

    public void logAuthenticateRefreshTokenNonRetriableDuringAutoLogin(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_AUTHENTICATE_REFRESHTOKEN_NON_RETRIABLE_DURING_AUTO_LOGIN, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_REFRESHTOKEN_FAILED,
                transactionID, null, payload);
    }

    public void logCIAuthenticateRefreshTokenRetriableError(Context context, String transactionID, String description, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_CI_AUTHENTICATE_REFRESHTOKEN_RETRIABLE_ERROR + " : " + description
                , PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_REFRESHTOKEN_FAILED,
                transactionID, null, payload);
    }

    public void logAuthenticateRefreshTokenRetriableErrorDuringAutoRefresh(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_AUTHENTICATE_REFRESHTOKEN_RETRIABLE_ERROR_DURING_AUTO_REFRESH, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_REFRESHTOKEN_FAILED,
                transactionID, null, payload);
    }

    public void logCIAuthenticateRefreshTokenAESException(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_CI_AUTHENTICATE_REFRESHTOKEN_AES_EXCEPTION, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_REFRESHTOKEN_FAILED,
                transactionID, null, payload);
    }

    public void logARAuthenticateRefreshTokenAESException(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_AR_AUTHENTICATE_REFRESHTOKEN_AES_EXCEPTION, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_REFRESHTOKEN_FAILED,
                transactionID, null, payload);
    }

    public void logAuthenticateAutoRefreshTokenJob(Context context, String transactionID, String desc, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_AR_AUTO_REFRESH_JOB + ". AutoRefresh set at : " + desc, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_AUTO_REFRESH_JOB,
                transactionID, null, payload);
    }

    public void logAuthenticateRefreshTokenJobError(Context context, String transactionID, String errorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_AUTHENTICATE_AUTO_REFRESH_JOB_ERROR + "\n Error Description: " + errorDescription, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_AUTO_REFRESH_JOB_ERROR,
                transactionID, null, payload);
    }

    public void logLogout(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_LOG_OUT, EVENT_LOGOUT, PAGE_NAME, SUB_PAGE, USECASE_LOGOUT,
                transactionID, null, payload);
    }

    public void logRevokeAccessToken(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_LOG_OUT, EVENT_REVOKE_ACCESS_TOKEN, PAGE_NAME, SUB_PAGE, USECASE_REVOKE_ACCESS_TOKEN,
                transactionID, null, payload);
    }

    public void logRevokeRefreshToken(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_LOG_OUT, EVENT_REVOKE_REFRESH_TOKEN, PAGE_NAME, SUB_PAGE, USECASE_REVOKE_REFRESH_TOKEN,
                transactionID, null, payload);
    }

    public void logRevokeTokenSuccess(Context context, String transactionID, String tokenHint, Map<String, String> payload) {
        logEventNG(context, EVENT_LOG_OUT, EVENT_REVOKE_TOKEN_SUCCESS + tokenHint, PAGE_NAME, SUB_PAGE, USECASE_REVOKE_TOKEN_SUCCESS,
                transactionID, null, payload);
    }

    public void logRevokeTokenFailed(Context context, String transactionID, String errorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_LOG_OUT, EVENT_REVOKE_TOKEN_FAILED + "\n Error Description: " + errorDescription, PAGE_NAME, SUB_PAGE, USECASE_REVOKE_TOKEN_FAILED,
                transactionID, null, payload);
    }

    public void logARRequestingRefreshToken(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_REFRESH_TOKEN, EVENT_AR_REQUESITNG_REFRESH_JOB, PAGE_NAME, SUB_PAGE, USECASE_REFRESH_TOKEN,
                transactionID, null, payload);
    }

    public void logCIRequestingRefreshToken(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_REFRESH_TOKEN, EVENT_CI_REQUESITNG_REFRESH_JOB, PAGE_NAME, SUB_PAGE, USECASE_REFRESH_TOKEN,
                transactionID, null, payload);
    }

    public void logCINoAccessTokenFound(Context context, String transactionID, String authId, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_CI_NO_TOKEN_FOUND, PAGE_NAME, SUB_PAGE, USECASE_REFRESH_TOKEN,
                transactionID, authId, payload);
    }

    public void logARLogout(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_LOG_OUT, EVENT_AR_LOGOUT, PAGE_NAME, SUB_PAGE, USECASE_LOGOUT,
                transactionID, null, payload);
    }

    public void logCILogout(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_LOG_OUT, EVENT_CI_LOGOUT, PAGE_NAME, SUB_PAGE, USECASE_LOGOUT,
                transactionID, null, payload);
    }

    public void logARRevokeAccessToken(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_LOG_OUT, EVENT_AR_REVOKE_ACCESS_TOKEN, PAGE_NAME, SUB_PAGE, USECASE_REVOKE_ACCESS_TOKEN,
                transactionID, null, payload);
    }

    public void logARRevokeRefreshToken(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_LOG_OUT, EVENT_AR_REVOKE_REFRESH_TOKEN, PAGE_NAME, SUB_PAGE, USECASE_REVOKE_REFRESH_TOKEN,
                transactionID, null, payload);
    }

    public void logCIRevokeAccessToken(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_LOG_OUT, EVENT_CI_REVOKE_ACCESS_TOKEN, PAGE_NAME, SUB_PAGE, USECASE_REVOKE_ACCESS_TOKEN,
                transactionID, null, payload);
    }

    public void logCIRevokeRefreshToken(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_LOG_OUT, EVENT_CI_REVOKE_REFRESH_TOKEN, PAGE_NAME, SUB_PAGE, USECASE_REVOKE_REFRESH_TOKEN,
                transactionID, null, payload);
    }

    public void logARRevokeTokenSuccess(Context context, String transactionID, String tokenHint, Map<String, String> payload) {
        logEventNG(context, EVENT_LOG_OUT, EVENT_AR_REVOKE_TOKEN_SUCCESS + tokenHint, PAGE_NAME, SUB_PAGE, USECASE_REVOKE_TOKEN_SUCCESS,
                transactionID, null, payload);
    }

    public void logARRevokeTokenFailed(Context context, String transactionID, String errorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_LOG_OUT, EVENT_AR_REVOKE_TOKEN_FAILED + "\n Error Description: " + errorDescription, PAGE_NAME, SUB_PAGE, USECASE_REVOKE_TOKEN_FAILED,
                transactionID, null, payload);
    }

    public void logCIRevokeTokenSuccess(Context context, String transactionID, String tokenHint, Map<String, String> payload) {
        logEventNG(context, EVENT_LOG_OUT, EVENT_CI_REVOKE_TOKEN_SUCCESS + tokenHint, PAGE_NAME, SUB_PAGE, USECASE_REVOKE_TOKEN_SUCCESS,
                transactionID, null, payload);
    }

    public void logCIRevokeTokenFailed(Context context, String transactionID, String errorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_LOG_OUT, EVENT_CI_REVOKE_TOKEN_FAILED + "\n Error Description: " + errorDescription, PAGE_NAME, SUB_PAGE, USECASE_REVOKE_TOKEN_FAILED,
                transactionID, null, payload);
    }

    public void logCIAuthenticateRefreshTokenAPIXError(Context context, String transactionID, String errorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_CI_AUTHENTICATE_REFRESHTOKEN_APIX_ERROR + "\n Error Description: " + errorDescription, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_REFRESHTOKEN_FAILED,
                transactionID, null, payload);
    }

    public void logCIAuthenticateRefreshTokenNonRetriableError(Context context, String transactionID, String errorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_CI_AUTHENTICATE_REFRESHTOKEN_NON_RETRABLE_ERROR + " : " + errorDescription,
                PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_REFRESHTOKEN_FAILED,
                transactionID, null, payload);
    }

    public void logAuthenticateCIRefreshTokenSuccess(Context context, String transactionID, String desc, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_CI_REFRESHTOKEN_SUCCESS + " at " + desc, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_REFRESHTOKEN_SUCCESS,
                transactionID, null, payload);
    }

    public void logInitBackendSocketTimeOutException(Context context, String transactionID, String errorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_INIT, EVENT_INIT_BACKEND_SOCEKT_TIMEOUT_EXCEPTION + "\n Error Description: " + errorDescription, PAGE_NAME, SUB_PAGE, USECASE_INIT_FAILURE, transactionID, null, payload);
    }

    public void logInitIssueGenerateProofIdTimeout(Context context, String transactionID, String errorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_INIT, EVENT_INIT_UNABLE_TO_GENERATE_PROOF_ID_TIMEOUT + "\n Error Description: " + errorDescription, PAGE_NAME, SUB_PAGE, USECASE_INIT_FAILURE, transactionID, null, payload);
    }

    public void logCIAuthenticateSocketTimeOutIssue(Context context, String transactionID, String authId, String ErrorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_CI_AUTHENTICATE_TIMEOUT_ISSUE + "Error Description: " + ErrorDescription, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_SERVER_ISSUE,
                transactionID, authId, payload);
    }

    public void logARAuthenticateSocketTimeOutIssue(Context context, String transactionID, String authId, String ErrorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_AUTHENTICATE, EVENT_AR_AUTHENTICATE_TIMEOUT_ISSUE + "Error Description: " + ErrorDescription, PAGE_NAME, SUB_PAGE, USECASE_AUTHENTICATE_SERVER_ISSUE,
                transactionID, authId, payload);
    }

    public void logARRevokeTokenFailedDueToTimeout(Context context, String transactionID, String errorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_LOG_OUT, EVENT_AR_REVOKE_TOKEN_FAILED_TIMEOUT + "\n Error Description: " + errorDescription, PAGE_NAME, SUB_PAGE, USECASE_REVOKE_TOKEN_FAILED,
                transactionID, null, payload);
    }

    public void logCIRevokeTokenFailedDueToTimeout(Context context, String transactionID, String errorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_LOG_OUT, EVENT_CI_REVOKE_TOKEN_FAILED_TIMEOUT + "\n Error Description: " + errorDescription, PAGE_NAME, SUB_PAGE, USECASE_REVOKE_TOKEN_FAILED,
                transactionID, null, payload);
    }

    public void logRevokeTokenFailedDueToTimeout(Context context, String transactionID, String errorDescription, Map<String, String> payload) {
        logEventNG(context, EVENT_LOG_OUT, EVENT_REVOKE_TOKEN_FAILED_TIMEOUT + "\n Error Description: " + errorDescription, PAGE_NAME, SUB_PAGE, USECASE_REVOKE_TOKEN_FAILED,
                transactionID, null, payload);
    }

    public void logRefreshTokenEmptyOrNullInDataStore(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_REFRESH_TOKEN_EMPTY_NULL, EVENT_CI_REFRESH_TOKEN_EMPTY_NULL, PAGE_NAME, SUB_PAGE, USECASE_REFRESH_TOKEN,
                transactionID, null, payload);
    }

    public void logRefreshTokenEmptyOrNullFromApix(Context context, String transactionID, Map<String, String> payload) {
        logEventNG(context, EVENT_REFRESH_TOKEN_EMPTY_NULL, EVENT_CI_REFRESH_TOKEN_EMPTY_NULL_APIX, PAGE_NAME, SUB_PAGE, USECASE_REFRESH_TOKEN,
                transactionID, null, payload);
    }
}
