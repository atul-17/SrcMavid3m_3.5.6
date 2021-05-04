package com.vodafone.idtmlib.lib.dagger;

import android.content.Context;
import android.util.Log;

import com.vodafone.idtmlib.BuildConfig;
import com.vodafone.idtmlib.lib.utils.Printer;

import java.io.File;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PrinterModule {
    private boolean enable;

    public PrinterModule(boolean enable) {
        this.enable = enable;
    }


    @Singleton
    @Provides
    Thread.UncaughtExceptionHandler provideDefaultCrashHandler() {
        return Thread.getDefaultUncaughtExceptionHandler();
    }

    @Singleton
    @Provides
    @Named("printer_file_path")
    String providePrinterFilePath(Context context) {
        File externalCacheDir = context.getExternalCacheDir();
        /* new code fix for Android 11  ****/
        if (externalCacheDir == null) {
            Log.w("PrinterModule", "externalCacheDir is null from getExternalCacheDir()");
            File[] fileArray = context.getExternalFilesDirs(null);
            if (fileArray != null) {
                if (fileArray[0] != null) {
                    externalCacheDir = new File(fileArray[0].getAbsolutePath());
                    if (externalCacheDir == null) {
                        Log.w("PrinterModule", "externalCacheDir is null from getExternalFilesDirs()");
                        externalCacheDir = context.getFilesDir();
                        Log.d("PrinterModule", "getFilesDir() : " + externalCacheDir);
                    } else {
                        Log.d("PrinterModule", "getExternalFilesDirs() : " + externalCacheDir);
                    }

                }
            }
        }
        /* new code fix for Android 11  ****/
        if (externalCacheDir == null) {
            return null;
        }
        StringBuilder filename = new StringBuilder(externalCacheDir.getAbsolutePath());
        filename.append("/idtmLogs.txt");
        return filename.toString();
    }

    @Singleton
    @Provides
    Printer.TagVerbosityLevel provideTagVerbosityLevel() {
        return Printer.TagVerbosityLevel.NONE;
    }

    @Singleton
    @Provides
    Printer providePrinter(Context context, Thread.UncaughtExceptionHandler defaultCrashHandler,
                           @Named("printer_file_path") String filepath,
                           Printer.TagVerbosityLevel tagVerbosityLevel) {
        if (enable) {
            return new Printer(context, defaultCrashHandler, filepath, BuildConfig.APP_CODENAME,
                    tagVerbosityLevel);
        } else {
            return new Printer(context, defaultCrashHandler, null, BuildConfig.APP_CODENAME,
                    tagVerbosityLevel) {

                @Override
                protected synchronized void msg(int type, Object... objs) {
                    // do nothing
                }
            };
        }
    }
}
