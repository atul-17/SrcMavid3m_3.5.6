package com.vodafone.idtmlib.lib.rxjava;

import com.vodafone.idtmlib.AccessToken;

/**
 * Contains the progress of the Authenticate method.
 */
public class AuthenticateProgress {
    private boolean showingIdGateway;
    private AccessToken accessToken;

    public AuthenticateProgress(boolean showingIdGateway, AccessToken accessToken) {
        this.showingIdGateway = showingIdGateway;
        this.accessToken = accessToken;
    }

    /**
     * Indicates if the ID Gateway is currently shown by the lib.
     *
     * @return show status
     */
    public boolean isShowingIdGateway() {
        return showingIdGateway;
    }

    /**
     * Get the user accessToken. This will be available only when the progress is completed.
     *
     * @return user accessToken
     */
    public AccessToken getAccessToken() {
        return accessToken;
    }

    /**
     * The progress is completed when the access token is available
     *
     * @return completed status
     */
    public boolean isCompleted() {
        return accessToken != null;
    }

    @Override
    public String toString() {
        return "AuthenticateProgress{" +
                "showingIdGateway=" + showingIdGateway +
                ", accessToken=" + accessToken +
                '}';
    }
}
