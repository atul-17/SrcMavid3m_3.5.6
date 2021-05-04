package com.libre.libresdk;

import android.content.Context;

import com.libre.libresdk.TaskManager.Alexa.Listeners.AlexaLoginListener;
import com.libre.libresdk.TaskManager.Alexa.Listeners.ListenerUtils.AlexaParams;


/**
 * Created by bhargav on 8/2/18.
 */

public class AlexaManager {
    private static AlexaManager alexaManager;
    private AlexaManager(){
    }
    public static AlexaManager getManager(){
        if (alexaManager == null){
            alexaManager = new AlexaManager();
        }
        return alexaManager;
    }
    public void loginWithAmazon(AlexaParams params, Context context, AlexaLoginListener alexaLoginListener){

    }



}
