package com.vodafone.idtmlib.lib.rxjava;

import com.vodafone.idtmlib.AccessToken;
import com.vodafone.idtmlib.lib.utils.Printer;
import com.vodafone.idtmlib.observers.AuthenticateObserver;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;

public class EmptyAuthenticateObserver extends AuthenticateObserver {
    Printer printer;

    @Inject
    public EmptyAuthenticateObserver(Printer printer) {
        this.printer = printer;
    }

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onError(Throwable e) {
        printer.e("Error during auto refresh ", e);
    }

    @Override
    public void onShowIdGateway() {

    }

    @Override
    public void onHideIdGateway() {

    }

    @Override
    public void onComplete(AccessToken accessToken) {
        printer.i("Auto refresh complete ", accessToken);
    }
}
