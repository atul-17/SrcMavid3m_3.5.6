package com.libre.libresdk.TaskManager.SAC.Listeners;

import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.MessageInfo;

/**
 * Created by bhargav on 27/4/18.
 */

public interface SACListenerWithResponse {
    public void response(MessageInfo messageInfo);
    public void failure(Exception e);
}
