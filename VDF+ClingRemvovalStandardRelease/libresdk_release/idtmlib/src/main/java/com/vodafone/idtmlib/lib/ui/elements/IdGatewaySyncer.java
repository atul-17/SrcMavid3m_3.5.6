package com.vodafone.idtmlib.lib.ui.elements;

import android.text.TextUtils;

import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class IdGatewaySyncer {
    private CountDownLatch startLatch = new CountDownLatch(1);
    private CountDownLatch redirectLatch = new CountDownLatch(1);
    private CountDownLatch finishLatch = new CountDownLatch(1);
    private String code;
    private String error;
    private boolean canceled;
    private boolean userFailedToLogin;
    private boolean invalidScope;

    @Inject
    public IdGatewaySyncer() {

    }

    public boolean isSuccess() {
        return !TextUtils.isEmpty(code);
    }

    public boolean isError() {
        if (isSuccess()) {
            return false;
        } else {
            return !canceled;
        }
    }

    public boolean isCanceled() {
        if (isSuccess()) {
            return false;
        } else {
            return canceled;
        }
    }

    public boolean isUserFailedToLogin() {
        if (isSuccess()) {
            return false;
        } else {
            return userFailedToLogin;
        }
    }

    public boolean invalidScope() {
        if (isSuccess()) {
            return false;
        } else {
            return invalidScope;
        }
    }

    public String getCode() {
        return code;
    }

    public String getError() {
        return error;
    }

    public void reset() {
        startLatch = new CountDownLatch(1);
        redirectLatch = new CountDownLatch(1);
        finishLatch = new CountDownLatch(1);
        code = null;
        error = null;
        canceled = false;
        userFailedToLogin = false;
        invalidScope =false;
    }

    public void waitForRedirect() throws InterruptedException {
        redirectLatch.await();
    }


    public void waitForStart() throws InterruptedException {
        startLatch.await();
    }

    public void notifyStart() {
        startLatch.countDown();
    }

    public void notifyRedirectToAuth() {
        redirectLatch.countDown();
    }

    public void waitForFinish() throws InterruptedException {
        finishLatch.await();
    }


    public void waitForRedirectToAuth() throws InterruptedException {
        redirectLatch.await();
    }

    public void notifyFinishUserCanceled(String error) {
        this.error = error;
        this.canceled = true;
        redirectLatch.countDown();
        finishLatch.countDown();
    }

    public void notifyFinishUserFailedToLogin(String error) {
        this.userFailedToLogin = true;
        this.error = error;
        redirectLatch.countDown();
        finishLatch.countDown();
    }

    public void notifyFinishInvalidScope(String error) {
        this.invalidScope = true;
        this.error = error;
        redirectLatch.countDown();
        finishLatch.countDown();
    }

    public void notifyFinish(String code) {
        this.code = code;
        this.error = code;
        redirectLatch.countDown();
        finishLatch.countDown();
    }
}
