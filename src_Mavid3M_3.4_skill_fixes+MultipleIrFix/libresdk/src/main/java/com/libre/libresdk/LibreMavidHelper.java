package com.libre.libresdk;

import android.content.Context;
import android.net.Network;
import android.os.Bundle;

import com.libre.libresdk.TaskManager.Alexa.CompanionProvisioningInfo;
import com.libre.libresdk.TaskManager.Alexa.Listeners.AlexaLoginListener;
import com.libre.libresdk.TaskManager.Alexa.Listeners.ListenerUtils.AlexaParams;
import com.libre.libresdk.TaskManager.Communication.Listeners.CommandStatusListener;
import com.libre.libresdk.TaskManager.Communication.Listeners.CommandStatusListenerWithResponse;
import com.libre.libresdk.TaskManager.Discovery.Listeners.DeviceListener;
import com.libre.libresdk.TaskManager.Discovery.CustomExceptions.WrongStepCallException;
import com.libre.libresdk.TaskManager.SAC.Listeners.SACListener;
import com.libre.libresdk.TaskManager.SAC.Listeners.SACListenerWithResponse;

/**
 * Created by bhargav on 2/2/18.
 */

public class LibreMavidHelper {
    public static String symmentricKey="";
    public static String getMYPEMstring="";
    public interface COMMANDS{
//        int START_OTA_UPGRADE = 1;
//        int START_BSL_UPGRADE = 2;
        int ASK_ALEXA_REFRESH_TOKEN = 208;
        int ASK_META_DATA = 234;
        int SEND_AUTH_CODE = 235;
        int AMAZON_SIGNOUT = 236;
        int UPDATE_LOCALE = 237;
      //  int COMP_APP_READ_ENV = 249;
      //  int COMP_APP_WRITE_ENV = 248;
      //  int START_BT_SOURCE = 242;
       // int START_BT_SEARCH = 243;
      //  int START_BT_CONNECT = 244;
      //  int START_BT_STATUS = 245;
      //  int START_BT_SOURCE_List = 240;
      //  int START_BT_SOURCE_List2 = 241;
      //  int FW_URL = 239;

        int BATTERY_STATUS = 238;
        int DEVICE_STATE = 250;

        int APP_WIFI_CONNECT = 251;

        //sending all the remote details
        //and also to retrive the button list
        int SEND_IR_REMOTE_DETAILS_AND_RETRIVING_BUTTON_LIST = 205;

        int ALEXA_SKILL_LINKING = 225;
    }

    public interface AC_REMOTE_CONTROLS {
        String AC_POWER_ON_BUTTON = "PWR_ON";
        String AC_POWER_OFF_BUTTON = "PWR_OFF";

        String AC_TEMP_UP = "TEMP_UP";
        String AC_TEMP_DOWN = "TEMP_DOWN";

        String AC_TEMP = "TEMP";
        String AC_MODE = "MODE";

        String AC_SWING = "SWING";
        String AC_DIRECTION = "DIRECTION";
        String AC_FAN_SPEED = "FAN_SPEED";
    }

    public interface REMOTECONTROLBUTTONNAME {
        String POWER_BUTTON = "POWER";
        String EXIT_BUTTON = "EXIT";

        String HOME_BUTTON = "HOME";
        String MENU_BUTTON = "MENU";

        String OPTION_BUTTON = "OPTIONS";
        String GUIDE_BUTTON = "GUIDE";

        String LANG_BUTTON = "LANGUAGE";
        String VOLUME_MUTE_BUTTON = "VOL MUTE";

        String BACK_BUTTON = "BACK";

        String SOURCE_BUTTON = "SOURCE";

        String REWIND_BUTTON = "REWIND";
        String PREV_BUTTON = "PREVIOUS";

        String NEXT_BUTTON = "NEXT";
        String VOLUME_UP = "VOL UP";

        String VOLUME_DOWN = "VOL DOWN";
        String OK_BUTTON = "OK";

        String SELECT_BUTTON = "SELECT";

        String UP_BUTTON = "UP";
        String DOWN_BUTTON = "DOWN";

        String RIGHT_BUTTON = "RIGHT";
        String LEFT_BUTTON = "LEFT";

        String CHANNEL_UP = "CH UP";
        String CHANNEL_DOWN = "CH DOWN";

        String BLUE_BUTTON = "BLUE";
        String GREEN_BUTTON = "GREEN";

        String YELLOW_BUTTON = "YELLOW";
        String RED_BUTTON = "RED";

        String PLAY_BUTTON = "PLAY";
        String PAUSE_BUTTON = "PAUSE";

        String FAST_FORWARD_BUTTON = "FAST FORWARD";
        String INFO_BUTTON = "INFO";

        String STOP_BUTTON = "STOP";
        String REC_BUTTON = "RECORD";


        String ONE_NOS_BUTTON = "1";
        String TWO_NOS_BUTTON = "2";
        String THREE_NOS_BUTTON = "3";

        String FOUR_NOS_BUTTON = "4";
        String FIVE_NOS_BUTTON = "5";

        String SIX_NOS_BUTTON = "6";
        String SEVEN_NOS_BUTTON = "7";

        String EIGHT_NOS_BUTTON = "8";
        String NINE_NOS_BUTTON = "9";

        String ZERO_NOS_BUTTON = "0";

    }


    public static void setAdvertiserMessage(String message) {
        DiscoveryManager.getManager().setAdvertisingMessage(message);
    }

    public static void setAdvertiseeValidator(String message) {
        //  DiscoveryManager.getManager().setAdvertiseeValidator(message);
    }

    public static void startDiscovery(DeviceListener deviceListener) throws Exception {
        DiscoveryManager.getManager().startDiscovery(deviceListener);
    }

    public static void stopdiscovery() {
        DiscoveryManager.getManager().stopDiscovery();
    }

    public static void configure(Bundle params, SACListener sacListener) {
        SACManager.getManager().configure(params, sacListener);
    }

    public static void configureBLE(Bundle params, SACListener sacListener) {
        SACManager.getManager().configureBLE(params, sacListener);
    }

    public static void sendCustomSACMessage(byte[] message, SACListenerWithResponse sacListenerWithResponse) {
        SACManager.getManager().sendCustomSACMessage(message, sacListenerWithResponse);
    }

    public static void sendCustomSACMessage(String message, SACListenerWithResponse sacListenerWithResponse) {
        SACManager.getManager().sendCustomSACMessage(message.getBytes(), sacListenerWithResponse);
    }

 //    public static void setZigBeeAutoRead(String ipAddress, String value, CommandStatusListenerWithResponse commandStatusListenerWithResponse){
//        CommunicationManager.getManager().setReadZigBee(ipAddress,value,commandStatusListenerWithResponse);
//    }
//

    public static void advertise() throws WrongStepCallException {
        try {
            DiscoveryManager.getManager().advertise();
        } catch (WrongStepCallException e) {
            throw e;
        }
    }

    public static void advertiseWithIp(String ip, Context context) throws WrongStepCallException {
        try {
            DiscoveryManager.getManager().advertise(ip, context);
        } catch (WrongStepCallException e) {
            throw e;
        }
    }

//    @Deprecated
//    public static void startOTAUpgrade(String ipAddress, CommandStatusListener commandStatusListener) {
//        CommunicationManager.getManager().startOTAUpgrade(ipAddress, commandStatusListener);
//
//    }

    public static void loginWithAmazon(AlexaParams params, Context context, AlexaLoginListener alexaLoginListener) {
        AlexaManager.getManager().loginWithAmazon(params, context, alexaLoginListener);
    }

    public static void askRefreshToken(String ipAddress, CommandStatusListenerWithResponse commandStatusListenerWithResponse) {
        CommunicationManager.getManager().askRefreshToken(ipAddress, commandStatusListenerWithResponse);
    }

    public static void askRefreshTokenWithNetwork(String ipAddress, CommandStatusListenerWithResponse commandStatusListenerWithResponse, Network network) {
        CommunicationManager.getManager().askRefreshTokenWithNetwork(ipAddress, commandStatusListenerWithResponse, network);
    }

    public static void askMetaDataInfo(String ipAddress, CommandStatusListenerWithResponse commandStatusListenerWithResponse) {
        CommunicationManager.getManager().askMetaDataInfo(ipAddress, commandStatusListenerWithResponse);
    }

    public static void amazonSignout(String ipAddress, CommandStatusListener commandStatusListener) {
        CommunicationManager.getManager().amazonSignout(ipAddress, commandStatusListener);
    }

    public static void setAlexaLocale(final String ipAddress, String locale, CommandStatusListenerWithResponse commandStatusListenerWithResponse) {
        CommunicationManager.getManager().setAlexaLocale(ipAddress, locale, commandStatusListenerWithResponse);

    }
    public static void getAlexaLangList(String ipAddress,  CommandStatusListenerWithResponse commandStatusListenerWithResponse) {
        CommunicationManager.getManager().getAlexaLangugeList(ipAddress, commandStatusListenerWithResponse);

    }
    public static void getAlexaLocale(String ipAddress, CommandStatusListenerWithResponse commandStatusListenerWithResponse) {
        CommunicationManager.getManager().setAlexaLocaleEmpty(ipAddress, commandStatusListenerWithResponse);
    }
   // public static void setAUtoOTAWrite(String ipAddress, String value, CommandStatusListenerWithResponse commandStatusListenerWithResponse) {
   //     CommunicationManager.getManager().setAutoOtaWrite(ipAddress, value, commandStatusListenerWithResponse);

   // }

   // public static void setZigBeeWrite(String ipAddress, String value, CommandStatusListenerWithResponse commandStatusListenerWithResponse) {
       // CommunicationManager.getManager().setZigBeeWrite(ipAddress, value, commandStatusListenerWithResponse);

  //  }

   // public static void setAUtoOTARead(String ipAddress, String value, CommandStatusListenerWithResponse commandStatusListenerWithResponse) {
      //  CommunicationManager.getManager().setAutoOtaRead(ipAddress, value, commandStatusListenerWithResponse);

  //  }


    public static void sendAlexaAuthDetails(String ipAddress, CompanionProvisioningInfo companionProvisioningInfo
            , CommandStatusListenerWithResponse commandStatusListener) {
        CommunicationManager.getManager().sendAlexaAuthDetails(ipAddress, companionProvisioningInfo, commandStatusListener);
    }

    public static void sendCustomCommands(String ipAddress, int messageBox, String message
            , CommandStatusListenerWithResponse commandStatusListener) {
        CommunicationManager.getManager().sendCustomCommands(ipAddress, messageBox, message, commandStatusListener);
    }

    public static void sendCustomCommands(String ipAddress, int messageBox, String message
            , CommandStatusListener commandStatusListener) {
        CommunicationManager.getManager().sendCustomCommands(ipAddress, messageBox, message, commandStatusListener);
    }
}
