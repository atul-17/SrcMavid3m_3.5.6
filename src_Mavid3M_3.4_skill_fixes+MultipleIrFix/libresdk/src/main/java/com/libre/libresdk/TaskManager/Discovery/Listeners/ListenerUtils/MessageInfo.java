package com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils;

/**
 * Created by bhargav on 2/2/18.
 */

public class MessageInfo {
    private String message;
    private String ipAddressOfSender;
    public MessageInfo(String ipAddressOfSender,String message){
        setIpAddressOfSender(ipAddressOfSender);
        setMessage(message);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIpAddressOfSender() {
        return ipAddressOfSender;
    }

    public void setIpAddressOfSender(String ipAddressOfSender) {
        this.ipAddressOfSender = ipAddressOfSender;
    }
}
