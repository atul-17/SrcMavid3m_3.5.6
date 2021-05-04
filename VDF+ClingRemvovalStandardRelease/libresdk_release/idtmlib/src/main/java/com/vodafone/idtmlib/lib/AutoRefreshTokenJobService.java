package com.vodafone.idtmlib.lib;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.vodafone.idtmlib.AccessToken;
import com.vodafone.idtmlib.IdtmLibInjector;
import com.vodafone.idtmlib.lib.rxjava.AuthenticateObservable;
import com.vodafone.idtmlib.lib.utils.Printer;
import com.vodafone.idtmlib.observers.AuthenticateObserver;

import java.text.SimpleDateFormat;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class AutoRefreshTokenJobService extends BroadcastReceiver {
    public static final long AUTO_RERESH_MIN_DELAY_SEC = 180L;

    private static final String TAG = AutoRefreshTokenJobService.class.getSimpleName();
    @Inject
    Printer printer;
    @Inject
    AuthenticateObservable authenticateObservable;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public static void setAlarm(Context context, long startTime) {
        // Cancel if alarm is already scheduled
        cancelAlarm(context);

        // Set up new alarm
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent("com.vodafone.idtmlib.lib.AUTO_REFRESH");
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

        // Ensure that alarm is set up in the future
        long currentTime = System.currentTimeMillis();
        if (startTime <= currentTime || startTime - currentTime < AUTO_RERESH_MIN_DELAY_SEC * 1000L) {
            startTime = currentTime + AUTO_RERESH_MIN_DELAY_SEC * 1000L;
        }

        am.setWindow(AlarmManager.RTC_WAKEUP, startTime, 60000L, pi);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Log.i("IDTM", "Scheduled auto refresh token job to run at " + sdf.format(startTime));

    }

    public static void cancelAlarm(Context context) {
        Intent i = new Intent("com.vodafone.idtmlib.lib.AUTO_REFRESH");
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("IDTM", "auto refresh token job - onReceive of " + intent.getAction());
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        ((IdtmLibInjector) context.getApplicationContext()).getIdtmLib().getAppComponent().inject(this);
        Log.i("IDTM", "Starting auto refresh token job");
        authenticateObservable.startAutoRefresh(new AutoAuthenticateObserver());

        wl.release();
    }

    class AutoAuthenticateObserver extends AuthenticateObserver {

        public AutoAuthenticateObserver() {
        }

        @Override
        public void onSubscribe(Disposable d) {
            compositeDisposable.add(d);
        }

        @Override
        public void onError(Throwable e) {
            Log.e("IDTM", "Exception generated during auto-refresh ignored");
        }

        @Override
        public void onShowIdGateway() {

        }

        @Override
        public void onHideIdGateway() {

        }

        @Override
        public void onComplete(AccessToken accessToken) {

        }
    }
}
