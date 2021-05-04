package com.libre.libresdk.TaskManager.Communication.Listeners;

/**
 * Created by bhargav on 9/2/18.
 */

public interface CommandStatusListener {
    public void failure(Exception e);
    public void success();
}
