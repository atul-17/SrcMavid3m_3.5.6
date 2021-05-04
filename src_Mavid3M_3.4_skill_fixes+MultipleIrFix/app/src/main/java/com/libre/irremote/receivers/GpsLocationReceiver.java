package com.libre.irremote.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import com.libre.irremote.MavidApplication;
import com.libre.libresdk.Util.LibreLogger;

public class GpsLocationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            LibreLogger.d(this,"suma in n/w change 16"+isGpsEnabled+"n/w enabled\n"+isNetworkEnabled);

            if (!isGpsEnabled && !isNetworkEnabled) {
                MavidApplication.doneLocationChange = false;
                LibreLogger.d(this,"suma in n/w change 5");

            }else{
                MavidApplication.doneLocationChange = true;
                LibreLogger.d(this,"suma in n/w change 6");
                MavidApplication.bothLOCPERMISSIONgIVEN=true;

            }
        }
    }
}
