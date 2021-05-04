package com.vodafone.idtmlib.lib.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {

    public static String getHumanReadableDate(long epochSec) {
        SimpleDateFormat formatTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        formatTime.setTimeZone(TimeZone.getDefault());
        Date date = new Date(epochSec);
        return formatTime.format(date);
    }
}
