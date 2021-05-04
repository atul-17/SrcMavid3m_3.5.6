package com.vodafone.idtmlib.lib.network.models.responses;

import com.google.gson.annotations.SerializedName;
import com.vodafone.idtmlib.lib.network.Response;

public class AccessTokenResponse extends Response {
    private OauthToken oauthToken;

    public OauthToken getOauthToken() {
        return oauthToken;
    }

    public static class OauthToken {
        @SerializedName("access_token") private String accessToken;
        @SerializedName("token_type") private String tokenType;
        @SerializedName("refresh_token") private String refreshToken;
        @SerializedName("expires_in") private int expiresIn;
        private String sub;
        private String acr;

        public String getAccessToken() {
            return accessToken;
        }

        public String getTokenType() {
            return tokenType;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public int getExpiresIn() {
            return expiresIn;
        }

        public String getSub() {
            return sub;
        }

        public String getAcr() {
            return acr;
        }
    }
}
