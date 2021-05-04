package com.libre.libresdk.TaskManager.SAC.Listeners;

/**
 * Created by bhargav on 5/2/18.
 */

public interface SACListener {
    public void success();
    public void failure(String message);
    public void successBLE(byte [] b);
}
