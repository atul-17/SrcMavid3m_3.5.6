package com.vodafone.idtmlib.lib.network;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.vodafone.idtmlib.lib.storage.DataCrypt;
import com.vodafone.idtmlib.lib.storage.Prefs;
import com.vodafone.idtmlib.lib.storage.basic.AesException;
import com.vodafone.idtmlib.lib.storage.basic.Preferences;
import com.vodafone.idtmlib.lib.utils.Device;
import com.vodafone.idtmlib.lib.utils.ISmapiConst;
import com.vodafone.idtmlib.lib.utils.Printer;
import com.vodafone.idtmlib.lib.utils.Smapi;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

public class RevokeTokens {
    private Context context;
    private Preferences preferences;
    private Printer printer;
    private DataCrypt dataCrypt;
    private Smapi smapi;
    private String smapiTransactionId = UUID.randomUUID().toString();
    private String accessToken = null;
    private String refreshToken = null;
    private String clientIdRevoke = null;
    private String authHeader = null;
    private Environment environment;
    private String sdkId = "";
    private String from;

    public RevokeTokens() {
    }

    public RevokeTokens(Context context, Preferences preferences, Printer printer, DataCrypt dataCrypt, Smapi smapi, Environment environment) {
        this.context = context;
        this.printer = printer;
        this.preferences = preferences;
        this.dataCrypt = dataCrypt;
        this.smapi = smapi;
        this.environment = environment;
    }

    public void startLogout(String from) {
        printer.i("Logout - revoke called from: " + from);
        this.from = from;
        try {
            this.accessToken = dataCrypt.getString(Prefs.ACCESS_TOKEN);
            this.clientIdRevoke = dataCrypt.getString(Prefs.CLIENT_ID);
            this.sdkId = dataCrypt.getString(Prefs.SDK_ID);
            this.refreshToken = dataCrypt.getString(Prefs.REFRESH_TOKEN);
            if (TextUtils.isEmpty(this.refreshToken)) {
                this.refreshToken = dataCrypt.getString(Prefs.ACCESS_TOKEN_REFRESH_TOKEN);
            }
        } catch (AesException e) {
            printer.e("Error while getting tokens :" + e);
            preferences.remove(Prefs.ACCESS_TOKEN, Prefs.ACCESS_TOKEN_TYPE,
                    Prefs.REFRESH_TOKEN, Prefs.ACCESS_TOKEN_EXPIRE_TIME_MS,
                    Prefs.ACCESS_TOKEN_SUB, Prefs.ACCESS_TOKEN_ACR, Prefs.REFRESH_TOKEN_GENERATED_AT, Prefs.ACCESS_TOKEN_REFRESH_TOKEN);
            return;
        }

        preferences.remove(Prefs.ACCESS_TOKEN, Prefs.ACCESS_TOKEN_TYPE,
                Prefs.REFRESH_TOKEN, Prefs.ACCESS_TOKEN_EXPIRE_TIME_MS,
                Prefs.ACCESS_TOKEN_SUB, Prefs.ACCESS_TOKEN_ACR, Prefs.REFRESH_TOKEN_GENERATED_AT, Prefs.ACCESS_TOKEN_REFRESH_TOKEN);

        Map<String, String> detailsMap = new HashMap<>();
        detailsMap.put(ISmapiConst.PAYLOAD_CLASS_NAME, "RevokeTokens");
        detailsMap.put(ISmapiConst.PAYLOAD_METHOD_NAME, "startLogout()");
        detailsMap.put(ISmapiConst.PAYLOAD_SDK_ID, sdkId);
        if (this.from.equalsIgnoreCase("autoRefresh")) {
            smapi.logARLogout(context, smapiTransactionId, detailsMap);
        } else if (this.from.equalsIgnoreCase("client-initiated")) {
            smapi.logCILogout(context, smapiTransactionId, detailsMap);
        } else {
            smapi.logLogout(context, smapiTransactionId, detailsMap);
        }

        RemoveCookiesThread thread = new RemoveCookiesThread();
        thread.start();

        if (!TextUtils.isEmpty(accessToken) && !TextUtils.isEmpty(refreshToken) && !TextUtils.isEmpty(clientIdRevoke)) {
            this.authHeader = toBase64(clientIdRevoke);
            if (Device.checkNetworkConnection(context)) {
                RevokeAccessTokenAsynTask revokeAccessTokenAsynTask = new RevokeAccessTokenAsynTask();
                revokeAccessTokenAsynTask.execute();
            } else {
                printer.e("Error : No connection found while revoke");
                detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "NoNetworkConnectionException");
                smapi.logNoNetworkConnectionRevokeToken(context, smapiTransactionId, detailsMap);
            }
        } else {
            printer.i("Getting empty values for tokens or clientId, tokens are already revoked.");
        }
    }

    public void revokeToken(String token, String token_type_hint) {
        printer.d("Revoke", "token :" + token);
        printer.d("Revoke", "token_type_hint :" + token_type_hint);
        printer.d("Revoke", "env :" + environment);

        Map<String, String> detailsMap = new HashMap<>();
        detailsMap.put(ISmapiConst.PAYLOAD_CLASS_NAME, "RevokeTokens");
        detailsMap.put(ISmapiConst.PAYLOAD_METHOD_NAME, "revokeToken()");
        detailsMap.put(ISmapiConst.PAYLOAD_SDK_ID, sdkId);

        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(environment.getRevokeUrl());
            urlConnection = (HttpURLConnection) url.openConnection();
            if (urlConnection != null && (urlConnection instanceof HttpsURLConnection)) {
                ((HttpsURLConnection) urlConnection).setSSLSocketFactory(new TLSSocketFactory());
            }
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Authorization", "Basic " + authHeader);

            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(token_type_hint)) {
                String data = URLEncoder.encode("token_type_hint", "UTF-8")
                        + "=" + URLEncoder.encode(token_type_hint, "UTF-8");

                data += "&" + URLEncoder.encode("token", "UTF-8") + "="
                        + URLEncoder.encode(token, "UTF-8");
                printer.d("Revoke", "data :" + data);
                urlConnection.connect();

                OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
                wr.write(data);
                wr.flush();

                if (from.equalsIgnoreCase("autoRefresh")) {
                    smapi.logARRevokeTokenSuccess(context, smapiTransactionId, token_type_hint, detailsMap);
                } else if (this.from.equalsIgnoreCase("client-initiated")) {
                    smapi.logCIRevokeTokenSuccess(context, smapiTransactionId, token_type_hint, detailsMap);
                } else {
                    smapi.logRevokeTokenSuccess(context, smapiTransactionId, token_type_hint, detailsMap);
                }
                int state = urlConnection.getResponseCode();
                printer.d("Revoke", "Response code: " + state);
            } else {
                printer.i("Revoke", "Tokens are empty before connecting revoke api");
            }

        } catch (KeyManagementException e) {
            detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "KeyManagementException");
            if (from.equalsIgnoreCase("autoRefresh")) {
                smapi.logARRevokeTokenFailed(context, smapiTransactionId, e.getMessage(), detailsMap);
            } else if (this.from.equalsIgnoreCase("client-initiated")) {
                smapi.logRevokeTokenFailed(context, smapiTransactionId, e.getMessage(), detailsMap);
            } else {
                smapi.logRevokeTokenFailed(context, smapiTransactionId, e.getMessage(), detailsMap);
            }
            printer.e(e);
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "NoSuchAlgorithmException");
            if (from.equalsIgnoreCase("autoRefresh")) {
                smapi.logARRevokeTokenFailed(context, smapiTransactionId, e.getMessage(), detailsMap);
            } else if (this.from.equalsIgnoreCase("client-initiated")) {
                smapi.logCIRevokeTokenFailed(context, smapiTransactionId, e.getMessage(), detailsMap);
            } else {
                smapi.logRevokeTokenFailed(context, smapiTransactionId, e.getMessage(), detailsMap);
            }
            printer.e(e);
            e.printStackTrace();
        } catch (SocketTimeoutException se) {
            detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IOException");
            if (from.equalsIgnoreCase("autoRefresh")) {
                smapi.logARRevokeTokenFailedDueToTimeout(context, smapiTransactionId, se.getMessage(), detailsMap);
            } else if (this.from.equalsIgnoreCase("client-initiated")) {
                smapi.logCIRevokeTokenFailedDueToTimeout(context, smapiTransactionId, se.getMessage(), detailsMap);
            } else {
                smapi.logRevokeTokenFailedDueToTimeout(context, smapiTransactionId, se.getMessage(), detailsMap);
            }
            printer.e("SocketTimeoutException: ", se);
            se.printStackTrace();
        } catch (IOException e) {
            detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "IOException");
            if (from.equalsIgnoreCase("autoRefresh")) {
                smapi.logARRevokeTokenFailed(context, smapiTransactionId, e.getMessage(), detailsMap);
            } else if (this.from.equalsIgnoreCase("client-initiated")) {
                smapi.logCIRevokeTokenFailed(context, smapiTransactionId, e.getMessage(), detailsMap);
            } else {
                smapi.logRevokeTokenFailed(context, smapiTransactionId, e.getMessage(), detailsMap);
            }
            printer.e(e);
            e.printStackTrace();
        } catch (Exception e) {
            detailsMap.put(ISmapiConst.PAYLOAD_EXCEPTION_THROWN, "Exception");
            if (from.equalsIgnoreCase("autoRefresh")) {
                smapi.logARRevokeTokenFailed(context, smapiTransactionId, e.getMessage(), detailsMap);
            } else if (this.from.equalsIgnoreCase("client-initiated")) {
                smapi.logCIRevokeTokenFailed(context, smapiTransactionId, e.getMessage(), detailsMap);
            } else {
                smapi.logRevokeTokenFailed(context, smapiTransactionId, e.getMessage(), detailsMap);
            }
            printer.e(e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    public String toBase64(String clientId) {
        byte[] data;
        data = clientId.getBytes(StandardCharsets.UTF_8);
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    public class RevokeAccessTokenAsynTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Map<String, String> detailsMap = new HashMap<>();
            detailsMap.put(ISmapiConst.PAYLOAD_CLASS_NAME, "RevokeAccessTokenAsynTask");
            detailsMap.put(ISmapiConst.PAYLOAD_METHOD_NAME, "doInBackground()");
            detailsMap.put(ISmapiConst.PAYLOAD_SDK_ID, sdkId);
            if (from.equalsIgnoreCase("autoRefresh")) {
                smapi.logARRevokeAccessToken(context, smapiTransactionId, detailsMap);
            } else if (from.equalsIgnoreCase("client-initiated")) {
                smapi.logCIRevokeAccessToken(context, smapiTransactionId, detailsMap);
            } else {
                smapi.logRevokeAccessToken(context, smapiTransactionId, detailsMap);
            }
            revokeToken(accessToken, "access_token");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            printer.i("Calling Api to revoke refresh token");
            RevokeRefreshTokenAsynTask revokeRefreshAsynTask = new RevokeRefreshTokenAsynTask();
            revokeRefreshAsynTask.execute();
        }
    }

    public class RevokeRefreshTokenAsynTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Map<String, String> detailsMap = new HashMap<>();
            detailsMap.put(ISmapiConst.PAYLOAD_CLASS_NAME, "RevokeRefreshTokenAsynTask");
            detailsMap.put(ISmapiConst.PAYLOAD_METHOD_NAME, "doInBackground()");
            detailsMap.put(ISmapiConst.PAYLOAD_SDK_ID, sdkId);
            if (from.equalsIgnoreCase("autoRefresh")) {
                smapi.logARRevokeRefreshToken(context, smapiTransactionId, detailsMap);
            } else if (from.equalsIgnoreCase("client-initiated")) {
                smapi.logCIRevokeRefreshToken(context, smapiTransactionId, detailsMap);
            } else {
                smapi.logRevokeRefreshToken(context, smapiTransactionId, detailsMap);
            }
            revokeToken(refreshToken, "refresh_token");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private class RemoveCookiesThread extends Thread {
        public RemoveCookiesThread() {
        }

        public void run() {
            Looper.prepare();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().removeAllCookies(value -> printer.d("CookieManager onReceiveValue " + value));
            } else {
                CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(context);
                cookieSyncManager.startSync();
                CookieManager.getInstance().removeAllCookie();
                cookieSyncManager.stopSync();
            }
            Looper.loop();
        }
    }
}
