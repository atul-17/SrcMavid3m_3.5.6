package com.vodafone.idtmlib.lib.ui.elements;


import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.nimbusds.jose.JOSEException;
import com.vodafone.idtmlib.exceptions.IdtmSSLPeerUnverifiedException;
import com.vodafone.idtmlib.lib.network.BadStatusCodeException;
import com.vodafone.idtmlib.lib.network.IdtmApi;
import com.vodafone.idtmlib.lib.network.JwtHelper;
import com.vodafone.idtmlib.lib.network.Response;
import com.vodafone.idtmlib.lib.network.models.responses.JwtResponse;
import com.vodafone.idtmlib.lib.network.models.responses.SetupResponse;
import com.vodafone.idtmlib.lib.storage.DataCrypt;
import com.vodafone.idtmlib.lib.storage.Prefs;
import com.vodafone.idtmlib.lib.storage.basic.AesException;
import com.vodafone.idtmlib.lib.utils.Printer;
import com.vodafone.idtmlib.lib.utils.Smapi;

import java.io.IOException;
import java.security.cert.Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.inject.Inject;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import okhttp3.CertificatePinner;

public abstract class CertificatePinnerWebViewClient extends WebViewClient {
    @Inject
    DataCrypt dataCrypt;
    @Inject
    Smapi smapi;
    private Printer printer;
    private CertificatePinner certificatePinner;
    private IdtmApi idtmApi;
    private Gson gson;
    private Context context;
    private String smapiTransactionId = UUID.randomUUID().toString();

    public CertificatePinnerWebViewClient(Printer printer, CertificatePinner certificatePinner, IdtmApi idtmapi, Context c) {
        this.printer = printer;
        this.certificatePinner = certificatePinner;
        this.idtmApi = idtmapi;
        this.context = c;
        printer.i("Preparing data for authenticate");
        try {
            smapi = new Smapi(this.printer, this.context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        return processRequest(Uri.parse(url));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        printer.d("Request ", request.toString());
        return processRequest(request.getUrl());
    }

    private WebResourceResponse processRequest(Uri uri) {

        SSLSocket socket = null;
        if (TextUtils.equals(uri.getScheme(), "https")) {
            try {
                printer.d("Checking host: ", uri.getHost(), " authority: ", uri.getAuthority());
                SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
                socket = (SSLSocket) sf.createSocket(uri.getHost(), 443);
                if (socket != null && (socket instanceof SSLSocket)) {
                    ((SSLSocket) socket).setEnabledProtocols(new String[]{"TLSv1.2"});

                }
                setSNIHost(sf, socket, uri.getHost());
                SSLSession s = socket.getSession();
                Certificate[] certs = s.getPeerCertificates();
                certificatePinner.check(uri.getHost(), Arrays.asList(certs));

                //throw new SSLPeerUnverifiedException("sslexcep");

            } catch (SSLPeerUnverifiedException ssle) {
                printer.w("Certificate Pinning failed for host ", uri.getHost(), ": ", ssle.getMessage());
                smapi.logInitCertificateIssueInWebview(context, smapiTransactionId, ssle.getMessage());
                /* Call API to get latest certificates **/
                try {
                    printer.w("Fetching certificates again.");

                    String sdkId = dataCrypt.getString(Prefs.SDK_ID);
                    SecretKey serverSymmetricKey = dataCrypt.loadServerSymmetricKey();
                    String sdkIdJwtToken = JwtHelper.aesEncrypt(serverSymmetricKey, sdkId);
                    String clientId = dataCrypt.getString(Prefs.CLIENT_ID);
                    gson = new Gson();

                    JwtResponse jwtResponse = Response.retrieve(JwtResponse.class,
                            idtmApi.getClientDetails(sdkIdJwtToken, clientId, sdkId));
                    String jsonResponse = JwtHelper.aesDecrypt(serverSymmetricKey, jwtResponse.getData());
                    SetupResponse setupResponse = gson.fromJson(jsonResponse, SetupResponse.class);
                    printer.d("Certificate List: " + setupResponse.getCertificates().size());
                    dataCrypt.set(Prefs.CERT_PINNING_HASHES, setupResponse.getCertificates() == null ? null :
                            gson.toJson(setupResponse.getCertificates()));
                    printer.d("Saved certificate to prefs");

                    checkWebViewCertificates(uri);

                } catch (IOException | BadStatusCodeException ie) {
                    smapi.logExceptionInWebview(context, smapiTransactionId, ie.getMessage());
                    printer.e("Server error", ie);
                    ie.printStackTrace();
                    return new WebResourceResponse(null, null, null);
                } catch (AesException ae) {
                    smapi.logExceptionInWebview(context, smapiTransactionId, ae.getMessage());
                    printer.e("Error saving data to secure storage", ae);
                    ae.printStackTrace();
                    return new WebResourceResponse(null, null, null);
                } catch (JOSEException | ParseException je) {
                    smapi.logExceptionInWebview(context, smapiTransactionId, je.getMessage());
                    printer.e("Error parsing data", je);
                    je.printStackTrace();
                    return new WebResourceResponse(null, null, null);
                } catch (IdtmSSLPeerUnverifiedException ise) {
                    smapi.logInitCertificateIssueInWebview(context, smapiTransactionId, ssle.getMessage());
                    printer.e("Error while checking certificates again.", ise);
                    ise.printStackTrace();
                    return new WebResourceResponse(null, null, null);
                }
                //return new WebResourceResponse(null, null, null);
            } catch (IOException e) {
                smapi.logExceptionInWebview(context, smapiTransactionId, e.getMessage());
                printer.w("IOException for host ", uri.getHost(), ": ", e.getMessage());
                return new WebResourceResponse(null, null, null);
            } finally {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                    smapi.logExceptionInWebview(context, smapiTransactionId, e.getMessage());
                    printer.w("Exception while closing socket in finally." + e);
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public void setSNIHost(final SSLSocketFactory factory, final SSLSocket socket, final String hostname) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            printer.i("Setting SNI via SSLParameters");
            SNIHostName sniHostName = new SNIHostName(hostname);
            SSLParameters sslParameters = socket.getSSLParameters();
            List<SNIServerName> sniHostNameList = new ArrayList<>(1);
            sniHostNameList.add(sniHostName);
            sslParameters.setServerNames(sniHostNameList);
            socket.setSSLParameters(sslParameters);
        } else if (factory instanceof android.net.SSLCertificateSocketFactory) {
            printer.i("Setting SNI via SSLCertificateSocketFactory");
            ((android.net.SSLCertificateSocketFactory) factory).setHostname(socket, hostname);
        } else {
            printer.i("Setting SNI via reflection");
            try {
                socket.getClass().getMethod("setHostname", String.class).invoke(socket, hostname);
            } catch (Throwable e) {
                // ignore any error, we just can't set the hostname...
                printer.e("Could not call SSLSocket#setHostname(String) method ", e);
            }
        }
    }

    private void checkWebViewCertificates(Uri uri) throws IdtmSSLPeerUnverifiedException {

        SSLSocket socket = null;
        CertificatePinner.Builder certificateBuilder = new CertificatePinner.Builder();
        smapi.logCheckRefreshCertificateInWebview(context, smapiTransactionId);
        try {
            String certificateHashes = dataCrypt.getString(Prefs.CERT_PINNING_HASHES);
            com.vodafone.idtmlib.lib.network.models.Certificate[] certificates =
                    gson.fromJson(certificateHashes, com.vodafone.idtmlib.lib.network.models.Certificate[].class);

            for (com.vodafone.idtmlib.lib.network.models.Certificate certificate : certificates) {
                certificateBuilder.add(certificate.getDomain(), certificate.getHashes());
            }

        } catch (AesException ae) {
            printer.w("Exception while fetching certificates from preferences." + ae);
            ae.printStackTrace();
        }
        CertificatePinner certPinner = certificateBuilder.build();
        if (TextUtils.equals(uri.getScheme(), "https")) {
            try {
                printer.d("Checking certificates again.");
                SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
                socket = (SSLSocket) sf.createSocket(uri.getHost(), 443);
                if (socket != null && (socket instanceof SSLSocket)) {
                    ((SSLSocket) socket).setEnabledProtocols(new String[]{"TLSv1.2"});

                }
                setSNIHost(sf, socket, uri.getHost());
                SSLSession s = socket.getSession();
                Certificate[] certs = s.getPeerCertificates();
                printer.d("Certificates length: " + certs.length);
                certPinner.check(uri.getHost(), Arrays.asList(certs));

            } catch (SSLPeerUnverifiedException se) {
                smapi.logInitCertificateIssueInWebview(context, smapiTransactionId, se.getMessage());
                printer.w("checkWebViewCertificates SSLPeerUnverifiedException:", uri.getHost());
                printer.w("Throw IdtmSSLPeerUnverifiedException.");
                throw new IdtmSSLPeerUnverifiedException();
            } catch (IOException ie) {
                smapi.logInitCertificateIssueInWebview(context, smapiTransactionId, ie.getMessage());
                printer.w("checkWebViewCertificates IOException ", uri.getHost(), ": ", ie.getMessage());
                throw new IdtmSSLPeerUnverifiedException();
            } finally {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                    smapi.logExceptionInWebview(context, smapiTransactionId, e.getMessage());
                    printer.w("Exception while closing socket in finally." + e);
                    e.printStackTrace();
                }
            }
        }
    }
}
