package com.libre.libresdk.TaskManager.Alexa.Listeners;

import android.os.Bundle;

/**
 * Created by bhargav on 8/2/18.
 */

public interface AlexaLoginListener {
    public void Success(Bundle bundle);
    public void Failure();
}
