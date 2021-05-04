package com.vodafone.idtmlib.observers;

import com.vodafone.idtmlib.AccessToken;
import com.vodafone.idtmlib.lib.rxjava.AuthenticateProgress;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Observer for the Authenticate process.
 */
public abstract class AuthenticateObserver implements Observer<AuthenticateProgress> {
    private AccessToken accessToken;

    /**
     * Used to add the observer to the disposable list.<br/>
     * <br/>
     * Just implement as compositeDisposable.add(d);<br/>
     * <br/>
     * A new CompositeDisposable should be instantiated as a global var in an activity, service, etc.<br/>
     * When the observers are not needed anymore, eg: activity.onDestroy(), use compositeDisposable.clear();
     *
     * @param d
     */
    @Override
    public abstract void onSubscribe(Disposable d);

    /**
     * Do not implement
     *
     * @param progress
     */
    @Override
    public void onNext(AuthenticateProgress progress) {
        if (progress.isShowingIdGateway()) {
            onShowIdGateway();
        } else {
            onHideIdGateway();
        }
        this.accessToken = progress.getAccessToken();
    }

    /**
     * Do not implement
     */
    @Override
    public void onComplete() {
        onComplete(accessToken);
    }

    /**
     * Triggered in case of error during the authenticate process.
     *
     * @param e Generic exception, use <i>instanceof</i> to check the specific issue.<br/>
     *      <blockquote>
     *          <b>NotInitializedException</b> Need to initialize first<br/>
     *          <b>SecureStorageException</b> Error encrypting/decrypting data using the Android Keystore,
     *              should retry initialize()<br/>
     *          <b>IdtmServerException</b> Generic IDTM/IDGW server error, should retry<br/>
     *          <b>IdtmTemporaryIssueException</b> IDTM server has temporary issues, should retry later<br/>
     *          <b>IdGatewayRequiredException</b> ID Gateway is required to get a new access token, but
     *              was not allowed to open the webview<br/>
     *          <b>UserCanceledException</b> User has canceled the ID Gateway request (eg: pressed back,
     *              clicked on Cancel)<br/>
     *          <b>Exception</b> Generic exceptions shouldn't be triggered
     *      </blockquote>
     */
    @Override
    public abstract void onError(Throwable e);

    /**
     * Triggered when the ID Gateway is shown to the user
     */
    public abstract void onShowIdGateway();

    /**
     * Triggered when the ID Gateway is hidden to the user
     */
    public abstract void onHideIdGateway();

    /**
     * Triggered when the authenticate is completed and the access token is available
     */
    public abstract void onComplete(AccessToken accessToken);
}
