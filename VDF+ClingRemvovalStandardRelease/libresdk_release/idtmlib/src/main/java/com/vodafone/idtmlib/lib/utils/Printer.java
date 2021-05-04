package com.vodafone.idtmlib.lib.utils;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.inject.Singleton;

/**
 * Printer is a logger class with useful feature like auto adding the class name and method,
 * plus the java file with the line number where the method is used in the code (and creating a
 * link in Android Studio), plus the capability to write a file equal to the Android DDMS.
 * It can catch unhandled exception leading to crashes and log them.
 *
 * The log file is recreated from scratch when it reaches the maximum size.
 */
@Singleton
public class Printer implements UncaughtExceptionHandler {
    private final static int LINE_MAX_CHARS = 2000; // basically used for DDMS
    private final static long MAX_FILE_SIZE = 10485760; // 10 MB

    /**
     * Determines the verbosity of the log TAG
     */
    public enum TagVerbosityLevel {
        /** Use 'Printer' */
        NONE,
        /** Use only the class name */
        CLASS_NAME,
        /** Use class and method names */
        METHOD_NAME,
        /** Use class, method and source file names */
        SOURCE_FILE,
        /** Use class, method, source file names and source line number */
        SOURCE_LINE
    }

    private UncaughtExceptionHandler defaultCrashHandler;
    private String filepath;
    private String customTag;
    private TagVerbosityLevel tagVerbosityLevel;

    public Printer(Context context, UncaughtExceptionHandler defaultCrashHandler, String filepath,
                   String customTag, TagVerbosityLevel tagVerbosityLevel) {
        this.defaultCrashHandler = defaultCrashHandler;
        this.filepath = filepath;
        this.customTag = TextUtils.isEmpty(customTag) ? "Printer" : customTag;
        this.tagVerbosityLevel = tagVerbosityLevel;
        // set crash handler to log app crashes
        Thread.setDefaultUncaughtExceptionHandler(this);
        // set the file path and create a new log file if the current one is too big
        if (TextUtils.isEmpty(filepath)) {
            w("Cannot setup log file: file path not set");
        } else {
            final File file = new File(filepath);
            if (file.length() > MAX_FILE_SIZE) {
                BufferedWriter bufferWriter = null;
                try {
                    bufferWriter = new BufferedWriter(new FileWriter(filepath, false));
                    bufferWriter.write("Exceeded file size\n");
                } catch (Exception e) {
                    // do nothing
                } finally {
                    try {
                        bufferWriter.close();
                    } catch (Exception e) { /* do nothing*/ }
                }
            }
            // updates the Media cache to let the file visible when connecting via usb
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        }
    }

    /**
     * Log an INFO message concatenating the objects.
     * It uses StringBuffer to generate the String.
     *
     * @param objs Primitive and common types while for generic object it uses toString(). Also support Throwable which are stringified.
     **/
    public void i(final Object... objs) {
        msg(Log.INFO, objs);
    }

    /**
     * Log a WARN message concatenating the objects.
     * It uses StringBuffer to generate the String.
     *
     * @param objs Primitive and common types while for generic object it uses toString(). Also support Throwable which are stringified.
     **/
    public void w(final Object... objs) {
        msg(Log.WARN, objs);
    }

    /**
     * Log an ERROR message concatenating the objects.
     * It uses StringBuffer to generate the String.
     *
     * @param objs Primitive and common types while for generic object it uses toString(). Also support Throwable which are stringified.
     **/
    public void e(final Object... objs) {
        msg(Log.ERROR, objs);
    }

    /**
     * Log a DEBUG message concatenating the objects.
     * It uses StringBuffer to generate the String.
     *
     * @param objs Primitive and common types while for generic object it uses toString(). Also support Throwable which are stringified.
     **/
    public void d(final Object... objs) {
        msg(Log.DEBUG, objs);
    }

    protected synchronized void msg(final int type, final Object... objs) {
        StringBuilder message = new StringBuilder(objs.length);
        for (Object obj : objs) {
            if (obj instanceof Throwable) {
                message.append(Log.getStackTraceString((Throwable) obj));
            } else {
                message.append(obj);
            }
        }
        String tag = getRichTag();
        writeDdms(type, tag, message.toString());
        writeFile(type, tag, message.toString());
    }

    private void writeDdms(int type, String tag, String message) {
        for (final String line : splitLongMessage(message)) {
            Log.println(type, tag, line);
        }
    }

    private void writeFile(int type, String tag, String message) {
        if (!TextUtils.isEmpty(filepath)) {
            String messageTypePrefix = "";
            switch (type) {
                case Log.INFO:
                    messageTypePrefix = "I";
                    break;
                case Log.WARN:
                    messageTypePrefix = "W";
                    break;
                case Log.ERROR:
                    messageTypePrefix = "E";
                    break;
                case Log.DEBUG:
                    messageTypePrefix = "D";
                    break;
            }
            BufferedWriter bufferWriter = null;
            try {
                bufferWriter = new BufferedWriter(new FileWriter(filepath, true));
                String currentHumanTime = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US).format(new Date());
                bufferWriter.write(String.format("%s %s/%s:%s\n", currentHumanTime, messageTypePrefix, tag, message));
            } catch (Exception e) {
                // do nothing
            } finally {
                try {
                    bufferWriter.close();
                } catch (Exception e) { /* do nothing */ }
            }
        }
    }

    /**
     * Splits the message if it is longer than LINE_MAX_CHARS to avoid being cut by DDMS
     * @param message log message to print
     * @return list of messages
     */
    private ArrayList<String> splitLongMessage(String message) {
        final ArrayList<String> messages = new ArrayList<>();
        int msgPartsCount = message.length() / LINE_MAX_CHARS;
        for (int i=0; i < msgPartsCount; i++) {
            messages.add(message.substring(i* LINE_MAX_CHARS, (i+1)* LINE_MAX_CHARS));
        }
        if (message.length() % LINE_MAX_CHARS != 0) {
            messages.add(message.substring(msgPartsCount* LINE_MAX_CHARS));
        }
        return messages;
    }

    /**
     * Generate a rich tag, containing the class and method names, the file and the line number.
     * @return rich tag
     */
    private String getRichTag() {
        String richTag = customTag;
        if (tagVerbosityLevel != TagVerbosityLevel.NONE) {
            richTag += " ";
            boolean printerClassFoundPrev = false;
            try {
                final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                for (StackTraceElement stackTraceElement : stackTraceElements) {
                    boolean printerClassFound = stackTraceElement.getClassName().equals(Printer.class.getName());
                    if (printerClassFoundPrev && !printerClassFound) {
                        String className;
                        try {
                            className = stackTraceElement.getClassName().substring(stackTraceElement.getClassName().lastIndexOf(".") + 1);
                        } catch (Exception e) {
                            className = stackTraceElement.getClassName();
                        }
                        switch (tagVerbosityLevel) {
                            case CLASS_NAME:
                                richTag += className;
                                break;
                            case METHOD_NAME:
                                richTag += String.format("%s/%s",
                                        className,
                                        stackTraceElement.getMethodName());
                                break;
                            case SOURCE_FILE:
                                richTag += String.format("%s/%s(%s)",
                                        className,
                                        stackTraceElement.getMethodName(),
                                        stackTraceElement.getFileName());
                                break;
                            case SOURCE_LINE:
                                richTag += String.format("%s/%s(%s:%s)",
                                        className,
                                        stackTraceElement.getMethodName(),
                                        stackTraceElement.getFileName(),
                                        stackTraceElement.getLineNumber());
                                break;
                        }
                        break;
                    }
                    printerClassFoundPrev = printerClassFound;
                }
            } catch (Exception e) {
                // sets a default tag later
            }
        }
        return richTag;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e("FATAL EXCEPTION\n", e);
        if (defaultCrashHandler != null) {
            defaultCrashHandler.uncaughtException(t, e);
        }
    }
}