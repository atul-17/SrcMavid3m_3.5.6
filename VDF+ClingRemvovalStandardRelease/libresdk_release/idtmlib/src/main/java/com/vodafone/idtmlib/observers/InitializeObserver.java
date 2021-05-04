package com.vodafone.idtmlib.observers;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Observer for the Initialize process.
 */
public abstract class InitializeObserver implements Observer<Object> {

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
    public void onNext(Object progress) {

    }

    /**
     * Triggered in case of error during the initialize process.
     *
     * @param e Generic exception, use <i>instanceof</i> to check the specific issue.<br/>
     *      <blockquote>
     *          <b>IdtmServerException</b> Generic IDTM server error, should retry<br/>
     *          <b>IdtmTemporaryIssueException</b> IDTM server has temporary issues, should retry later<br/>
     *          <b>GoogleInstanceIdException</b> Error retrieving the Google instance ID, could be a network
     *              error or an issue with Google Play Services (should be enabled and updated)
     *          <b>SecureStorageException</b> Critical error generating keys using the Android Keystore,
     *              maybe the device/firmware has a bug or must be updated<br/>
     *          <b>Exception</b> Generic exceptions shouldn't be triggered
     *      </blockquote>
     */
    @Override
    public abstract void onError(Throwable e);

    /**
     * Triggered when the initialize process is completed
     */
    @Override
    public abstract void onComplete();
}