package com.libre.libresdk.TaskManager.Communication;

import android.content.Context;
import android.net.Network;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import com.libre.libresdk.LibreMavidHelper;
import com.libre.libresdk.TaskManager.Communication.Listeners.CommandStatusListener;
import com.libre.libresdk.TaskManager.Communication.Listeners.CommandStatusListenerWithResponse;
import com.libre.libresdk.TaskManager.Communication.Packet.Decoder.MavidPacketDecoder;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.MessageInfo;
import com.libre.libresdk.Util.LibreLogger;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by bhargav on 9/2/18.
 */

public class HttpMavidClient extends AsyncTask<String, Void, String> {

    private String deviceIp;
    private Context context;
    CommandStatusListenerWithResponse commandStatusListenerWithResponse;
    CommandStatusListener commandStatusListener;
    byte[] dataPacket;
    Network network;
    public HttpMavidClient(String deviceIp , byte[] dataPacket, CommandStatusListenerWithResponse commandStatusListener){
        this.context = context;
        this.deviceIp = deviceIp;
        this.commandStatusListenerWithResponse = commandStatusListener;
        this.dataPacket = dataPacket;
    }

    public HttpMavidClient(String deviceIp , byte[] dataPacket, CommandStatusListenerWithResponse commandStatusListener,Network network){
        this.context = context;
        this.deviceIp = deviceIp;
        this.commandStatusListenerWithResponse = commandStatusListener;
        this.dataPacket = dataPacket;
        this.network = network;
    }

    public HttpMavidClient(String deviceIp , byte[] dataPacket, CommandStatusListener commandStatusListener){
        this.context = context;
        this.deviceIp = deviceIp;
        this.commandStatusListener = commandStatusListener;
        this.dataPacket = dataPacket;
    }
    @Override
    protected String doInBackground(String... strings) {

        Log.d("MavidCommunication","firing api");
        final String BASE_URL = "https://" + deviceIp +":4435"+ "/msgbox";
        Log.d("MavidCommunication","POST to "+deviceIp+". For base URL :"+BASE_URL);
        String inputLine;
        URL myUrl = null;
        HttpsURLConnection connection = null;
        try {

            myUrl = new URL(BASE_URL);
            if (network!=null){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    connection = (HttpsURLConnection) network.openConnection(myUrl);
                }
            }else{
                connection =(HttpsURLConnection) myUrl.openConnection();
                connection.setSSLSocketFactory(createSslContext1(LibreMavidHelper.getMYPEMstring).getSocketFactory());
                connection.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            }

            connection.setRequestMethod("POST");
            connection.setReadTimeout(30000);
            connection.setConnectTimeout(30000);
            String urlParameters = "";
            // Send post request
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.write(this.dataPacket);
            wr.flush();
            wr.close();

            int responseCode = connection.getResponseCode();
            Log.d("MavidCommunication","\nSending 'POST' request to URL : " + myUrl);
            Log.d("MavidCommunication","Post parameters : " + urlParameters);
            Log.d("MavidCommunication","Response Code : " + responseCode);

            if (responseCode==200) {
                // sending http data is successfull
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sendSuccess();
                    }
                }).start();
                if (commandStatusListenerWithResponse==null){
                    return null;
                }
            }else{
                sendFailure(new Exception("Error "+responseCode));
                LibreLogger.d("MavidCommunication","suma in Http mavid client errror \n"+responseCode);
                return null;
            }
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            byte[] dataArr = new byte[4026];

            InputStream is = connection.getInputStream();
            int t = -1;
            int datalen = 0;

            while ((t =  is.read(dataArr,datalen,dataArr.length-datalen)) > 0) {
                // t is the length of read data
                datalen+=t;
            }
            in.close();
            if (commandStatusListenerWithResponse!=null ) {
                commandStatusListenerWithResponse.response(new MessageInfo(deviceIp, getPayload(dataArr, datalen)));
            }
        } catch (SocketTimeoutException e) {
            sendFailure(e);
        }  catch (Exception e) {
            e.printStackTrace();
            Log.d("MavidCommunication"," cred res: "+e.toString());
            sendFailure(e);
        }
        return null;
    }

    void copy(InputStream source, OutputStream target) throws IOException {
        byte[] buf = new byte[8192];
        int length;
        while ((length = source.read(buf)) > 0) {
            target.write(buf, 0, length);
        }
    }

    private void sendSuccess(){
        if (commandStatusListener!=null){
            Log.d("MavidCommunication","Sending success");
            this.commandStatusListener.success();
        }else if(commandStatusListenerWithResponse!=null) {
            Log.d("MavidCommunication","Sending success. Expect response");
            commandStatusListenerWithResponse.success();
        }
    }


    private void sendFailure(Exception e){
        if (commandStatusListener!=null){
            Log.d("MavidCommunication","Sending failure");
            commandStatusListener.failure(e);
        }else if(commandStatusListenerWithResponse!=null) {
            Log.d("MavidCommunication","Sending failure . Expect response"+e.getMessage());
            commandStatusListenerWithResponse.failure(e);
        }
    }










    private String getPayload(byte[] dataArr,int dataLength) {
        MavidPacketDecoder mavidPacketDecoder = new MavidPacketDecoder(dataArr,dataLength);
        return mavidPacketDecoder.getPayload();
    }
//    private ThreadLocal<TrustManager[]> trustAllCerts = new ThreadLocal<TrustManager[]>() {
//        @Override
//        protected TrustManager[] initialValue() {
//            return new TrustManager[]{new X509TrustManager() {
//                public X509Certificate[] getAcceptedIssuers() {
//                    return new X509Certificate[]{};
//                }
//
//                public void checkClientTrusted(X509Certificate[] chain,
//                                               String authType) throws CertificateException {
//                    LibreLogger.d(this, "suma in netty client trusted certi");
//                    convertToX509Cert(LibreMavidHelper.getMYPEMstring);
//
//                }
//
//                public void checkServerTrusted(X509Certificate[] chain,
//                                               String authType) throws CertificateException {
//                    LibreLogger.d(this, "suma in netty server trusted certi");
//                    convertToX509Cert(LibreMavidHelper.getMYPEMstring);
//
//                }
//            }
//            };
//        }
//    };
    public static void convertToX509Cert(String certificateString) throws CertificateException {
        X509Certificate certificate = null;
        CertificateFactory cf = null;
        try {
            if (certificateString != null); // NEED FOR PEM FORMAT CERT STRING
            byte[] certificateData= Base64.decode(certificateString, Base64.DEFAULT);

            cf = CertificateFactory.getInstance("X509");
            certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateData));
            Log.d("suma certi","suma in generate "+certificate);
        } catch (CertificateException e) {
            throw new CertificateException(e);
        }
    }



    private SSLContext createSslContext1(String certificateString)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, KeyManagementException, UnrecoverableKeyException {
        byte[] certificateData= Base64.decode(certificateString, Base64.DEFAULT);

        ByteArrayInputStream derInputStream = new ByteArrayInputStream(certificateData);

        CertificateFactory certificateFactory = null;
        certificateFactory = CertificateFactory.getInstance("X.509");

        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(derInputStream);
        String alias = cert.getSubjectX500Principal().getName();
        LibreLogger.d("HTTPS","suma in string alias\n"+alias);

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null);
        trustStore.setCertificateEntry(alias, cert);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(trustStore, null);
        KeyManager[] keyManagers = kmf.getKeyManagers();

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(trustStore);
        TrustManager[] trustManagers = tmf.getTrustManagers();

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(keyManagers, trustManagers, null);
        return sslContext;
    }










    private static SSLContext createSslContext(String certificateString)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, KeyManagementException, UnrecoverableKeyException {

        ByteArrayInputStream derInputStream = new ByteArrayInputStream(certificateString.getBytes());
        CertificateFactory certificateFactory = null;
        // try {
        certificateFactory = CertificateFactory.getInstance("X.509");
//        } catch (NoSuchProviderException e) {
//            e.printStackTrace();
//        }
        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(derInputStream);
        String alias = cert.getSubjectX500Principal().getName();

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null);
        trustStore.setCertificateEntry(alias, cert);
        KeyManagerFactory kmf = null;
        try {
            kmf = KeyManagerFactory.getInstance("X509","BC");
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        kmf.init(trustStore, null);
        KeyManager[] keyManagers = kmf.getKeyManagers();

        TrustManagerFactory tmf = null;
        try {
            tmf = TrustManagerFactory.getInstance("X509","BC");
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        if (tmf != null) {
            tmf.init(trustStore);
        }
        TrustManager[] trustManagers = tmf.getTrustManagers();

//        X509Certificate certificate = null;
//        CertificateFactory cf = null;
//        try {
//            if (certificateString != null); // NEED FOR PEM FORMAT CERT STRING
//            byte[] certificateData= Base64.decode(certificateString, Base64.DEFAULT);
//
//            cf = CertificateFactory.getInstance("X509");
//            certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateData));
//            Log.d("suma certi","suma in generate "+certificate);
//        } catch (CertificateException e) {
//            throw new CertificateException(e);
//        }

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(keyManagers, trustManagers, null);
        // sslContext.init(null, trustAllCerts.get(), null);

        return sslContext;
    }

    @Override
    protected void onPostExecute(String result) {
        //  httpHandler.onResponse(result);

    }

    private void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }});
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }
}
