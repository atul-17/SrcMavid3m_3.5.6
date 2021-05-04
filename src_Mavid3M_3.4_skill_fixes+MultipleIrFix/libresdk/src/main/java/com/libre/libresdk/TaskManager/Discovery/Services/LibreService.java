package com.libre.libresdk.TaskManager.Discovery.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by bhargav on 3/2/18.
 */

public class LibreService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }
}
