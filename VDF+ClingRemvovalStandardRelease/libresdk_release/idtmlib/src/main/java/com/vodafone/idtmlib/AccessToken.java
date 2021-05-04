package com.vodafone.idtmlib;

/**
 * Access token containing the info needed to use other Vodafone APIs.
 */

public class AccessToken {
    private String token;
    private String type;
    private String sub;

    public AccessToken(String token, String type, String sub) {
        this.token = token;
        this.type = type;
        this.sub = sub;
    }

    /**
     * Get the access token to be used in the Authorization header
     * @return access token string
     */
    public String getToken() {
        return token;
    }

    /**
     * Get the access token type to be used in the Authorization header, eg: Bearer
     * @return type string
     */
    public String getType() {
        return type;
    }

    /**
     * Get the sub associated to the access token
     * @return sub string
     */
    public String getSub() {
        return sub;
    }

    @Override
    public String toString() {
        return "AccessToken{" +
                "token='" + token + '\'' +
                ", type='" + type + '\'' +
                ", sub='" + sub + '\'' +
                '}';
    }
}
