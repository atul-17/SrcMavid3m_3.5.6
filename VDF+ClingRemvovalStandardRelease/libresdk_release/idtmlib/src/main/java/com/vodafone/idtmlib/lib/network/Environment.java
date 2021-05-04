package com.vodafone.idtmlib.lib.network;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.common.base.Strings;
import com.vodafone.idtmlib.EnvironmentType;
import com.vodafone.idtmlib.lib.network.models.Certificate;
import com.vodafone.idtmlib.lib.utils.Device;

import java.util.Random;

public class Environment {

   /* // DEV
    public static final String DEV_IDTM_API_URL =
            "http://idtm.cid.vodafo.ne/idtm-apis/ext/";
    public static final String DEV_IDGATEWAY_URL =
            "https://apistagingref.developer.vodafone.com/oauth2/authorize";*/

    // PREPROD_ESP
    public static final String PREPROD_ESP_IDTM_API_URL =
            "https://apistagingref.developer.vodafone.com/v1/omc/";
    public static final String PREPROD_ESP_IDGATEWAY_URL =
            "https://apistagingref.developer.vodafone.com/oauth2/authorize";
    public static final String PREPROD_ESP_REVOKE_API_URL=
            "https://apistagingref.developer.vodafone.com/v1/tokenRevocation/revoke";
    public static final Certificate PREPROD_ESP_IDTM_CERTIFICATE =
            new Certificate("apistagingref.developer.vodafone.com",
                    new String[]{"sha256/teAQgHZOQw2RL/lhUjMHSZWjn/tI225vZennbWX+3gw=",
                            "sha256/MjDgnEGXBiFgy2m7yog9LFlk4ly1+EvPh7dtITdCjao=",
                            "sha256/0VP6bQFaqj6XzIA7KrnQO5JJ8hVEByeKEzUKcLJdJRk=",
                            "sha256/5kJvNEMw0KjrCAu7eXY5HZdvyCS13BbA0VJG1RSP91w=",
                            "sha256/r/mIkG3eEpVdm+u/ko/cwxzOMo1bk4TyHIlByibiA5E="});

    // PREPROD_AWS
    public static final String PREPROD_AWS_IDTM_API_URL =
            "https://eu2-stagingref.api.vodafone.com/v1/omc/";
    public static final String PREPROD_AWS_IDGATEWAY_URL =
            "https://eu2-stagingref.api.vodafone.com/oauth2/authorize";
    public static final String PREPROD_AWS_REVOKE_API_URL=
            "https://eu2-stagingref.api.vodafone.com/v1/tokenRevocation/revoke";
    public static final Certificate PREPROD_AWS_IDTM_CERTIFICATE =
            new Certificate("eu2-stagingref.api.vodafone.com",
                    new String[]{"sha256/BNKgvApjNzXoP4ns5m4ob3n9prxUeFx5RrDm2SAn2DM=",
                            "sha256/5kJvNEMw0KjrCAu7eXY5HZdvyCS13BbA0VJG1RSP91w="});

    // PROD_ESP
    public static final String PROD_ESP_IDTM_API_URL =
            "https://api.developer.vodafone.com/v1/omc/";
    public static final String PROD_ESP_IDGATEWAY_URL =
            "https://api.developer.vodafone.com/oauth2/authorize";
    public static final String PROD_ESP_REVOKE_API_URL=
            "https://api.developer.vodafone.com/v1/tokenRevocation/revoke";
    public static final Certificate PROD_ESP_IDTM_CERTIFICATE =
            new Certificate("api.developer.vodafone.com",
                    new String[]{"sha256/oYJbvGSyc8nAkhhdNpiJMbdK0cGPIGEmLHE77PIxNI4=",
                            "sha256/5kJvNEMw0KjrCAu7eXY5HZdvyCS13BbA0VJG1RSP91w=",
                            "sha256/teAQgHZOQw2RL/lhUjMHSZWjn/tI225vZennbWX+3gw=",
                            "sha256/n14276wyHoG4EnCCN3kIKO+GIrBgmxkT3kaUmRntwzY="
                    });


    // PROD_AWS
    public static final String PROD_AWS_IDTM_API_URL =
            "https://eu2.api.vodafone.com/v1/omc/";
    public static final String PROD_AWS_IDGATEWAY_URL =
            "https://eu2.api.vodafone.com/oauth2/authorize";
    public static final String PROD_AWS_REVOKE_API_URL=
            "https://eu2.api.vodafone.com/v1/tokenRevocation/revoke";
    public static final Certificate PROD_AWS_IDTM_CERTIFICATE =
            new Certificate("eu2.api.vodafone.com",
                    new String[]{"sha256/uGCTsOUC14rNqjclWlX3NltIlWei7vxK0GtBeHneTtQ=",
                            "sha256/5kJvNEMw0KjrCAu7eXY5HZdvyCS13BbA0VJG1RSP91w="
                    });

  /*  // CAS
    public static final String CAS_IDTM_API_URL =
            "https://cas.cid-cloud.vodafone.com/idtm-apis/ext/";
    public static final String CAS_IDGATEWAY_URL =
            "https://apistagingref.developer.vodafone.com/oauth2/authorize";
    public static final String CAS_REVOKE_API_URL=
            "https://apistagingref.developer.vodafone.com/v1/tokenRevocation/revoke";
    public static final Certificate CAS_IDTM_CERTIFICATE =
            new Certificate("cas.cid-cloud.vodafone.com",
                    new String[]{"sha256/teAQgHZOQw2RL/lhUjMHSZWjn/tI225vZennbWX+3gw=",
                                 "sha256/5kJvNEMw0KjrCAu7eXY5HZdvyCS13BbA0VJG1RSP91w=",
                                 "sha256/r/mIkG3eEpVdm+u/ko/cwxzOMo1bk4TyHIlByibiA5E="});*/

    private Device device;
    private EnvironmentType environmentType;
    private String idtmApiUrl;
    private Certificate idtmCertificate;
    private String idgatewayUrl;
    private String revokeUrl;

    public Environment(Device device, EnvironmentType environmentType) {
        this.device = device;
        this.environmentType = environmentType;
        if (isPreprodEsp()) {
            this.idtmApiUrl = PREPROD_ESP_IDTM_API_URL;
            this.idgatewayUrl = PREPROD_ESP_IDGATEWAY_URL;
            this.idtmCertificate = PREPROD_ESP_IDTM_CERTIFICATE;
            this.revokeUrl = PREPROD_ESP_REVOKE_API_URL;
            Log.d("Environment", "PreProd ESP: "+idtmApiUrl);
        } else if (isPreprodAws()) {
            this.idtmApiUrl = PREPROD_AWS_IDTM_API_URL;
            this.idgatewayUrl = PREPROD_AWS_IDGATEWAY_URL;
            this.idtmCertificate = PREPROD_AWS_IDTM_CERTIFICATE;
            this.revokeUrl = PREPROD_AWS_REVOKE_API_URL;
            Log.d("Environment", "PreProd AWS: "+idtmApiUrl);
        } else if (isProdEsp()) {
            this.idtmApiUrl = PROD_ESP_IDTM_API_URL;
            this.idgatewayUrl = PROD_ESP_IDGATEWAY_URL;
            this.idtmCertificate = PROD_ESP_IDTM_CERTIFICATE;
            this.revokeUrl = PROD_ESP_REVOKE_API_URL;
            Log.d("Environment", "Prod ESP: "+idtmApiUrl);
        } else if (isProdAws()) {
            this.idtmApiUrl = PROD_AWS_IDTM_API_URL;
            this.idgatewayUrl = PROD_AWS_IDGATEWAY_URL;
            this.idtmCertificate = PROD_AWS_IDTM_CERTIFICATE;
            this.revokeUrl = PROD_AWS_REVOKE_API_URL;
            Log.d("Environment", "Prod AWS: "+idtmApiUrl);
        }
    }

    /*public boolean isCas() {
        return environmentType == EnvironmentType.CAS;
    }

    public boolean isDev() {
        return environmentType == EnvironmentType.DEV;
    }*/

    public boolean isPreprodEsp() {
        return environmentType == EnvironmentType.PREPROD_ESP;
    }

    public boolean isProdEsp() {
        return environmentType == EnvironmentType.PROD_ESP;
    }

    public boolean isPreprodAws() {
        return environmentType == EnvironmentType.PREPROD_AWS;
    }

    public boolean isProdAws() {
        return environmentType == EnvironmentType.PROD_AWS;
    }


    public String getIdtmApiUrl() {
        return idtmApiUrl;
    }

    public String getIdgatewayUrl(String clientId, String nonce, String idgatewayRedirectUrl,
                                  String mobileAcrValues, String wifiAcrValues, String scope, String loginHint) {
        // randomizing 'state' control param
        int nextInt = new Random().nextInt(9999);
        String state = Strings.padStart(String.valueOf(nextInt), 4, '0');
        // using acr based on connectivity
        String acrValues = device.isConnectedViaWifi() ? wifiAcrValues : mobileAcrValues;
        // build URL
        if(!TextUtils.isEmpty(loginHint)){
            //Add Login_hint
            return Uri.parse(idgatewayUrl).buildUpon()
                    .appendQueryParameter("response_type", "code")
                //    .appendQueryParameter("prompt", "login")
                    .appendQueryParameter("redirect_uri", idgatewayRedirectUrl)
                    .appendQueryParameter("nonce", nonce)
                    .appendQueryParameter("state", state)
                    .appendQueryParameter("client_id", clientId)
                    .appendQueryParameter("acr_values", acrValues)
                    .appendQueryParameter("scope", scope)
                    .appendQueryParameter("login_hint", loginHint)
                    .build()
                    .toString();
        } else {
            return Uri.parse(idgatewayUrl).buildUpon()
                    .appendQueryParameter("response_type", "code")
                 //   .appendQueryParameter("prompt", "login")
                    .appendQueryParameter("redirect_uri", idgatewayRedirectUrl)
                    .appendQueryParameter("nonce", nonce)
                    .appendQueryParameter("state", state)
                    .appendQueryParameter("client_id", clientId)
                    .appendQueryParameter("acr_values", acrValues)
                    .appendQueryParameter("scope", scope)
                    .build()
                    .toString();
        }
    }

    public Certificate getIdtmCertificate() {
        return idtmCertificate;
    }

    public String getRevokeUrl(){
        return revokeUrl;
    }
}