package com.libre.libresdk;

import android.net.Network;

import com.libre.libresdk.TaskManager.Alexa.CompanionProvisioningInfo;
import com.libre.libresdk.TaskManager.Communication.HttpMavidClient;
import com.libre.libresdk.TaskManager.Communication.Listeners.CommandStatusListener;
import com.libre.libresdk.TaskManager.Communication.Listeners.CommandStatusListenerWithResponse;
import com.libre.libresdk.TaskManager.Communication.Packet.Encoder.MavidPacketEncoder;

/**
 * Created by bhargav on 8/2/18.
 */

class CommunicationManager {
    static CommunicationManager communicationManager;
    static int COMMAND_TYPE_GET = 0;
    static int COMMAND_TYPE_REQUEST = 1;
    CommunicationManager(){}
    public static CommunicationManager getManager(){
        if (communicationManager == null){
            communicationManager = new CommunicationManager();
        }
        return communicationManager;
    }

    public void askRefreshToken(String ipAddress,CommandStatusListenerWithResponse commandStatusListenerWithResponse){
        sendAlexaCommandToDevice(ipAddress, MavidPacketEncoder.getPacket((byte) 0xAA,COMMAND_TYPE_REQUEST,SubCommands.ASK_ALEXA_REFRESH_TOKEN_NEW,""),commandStatusListenerWithResponse);
    }
    public void askRefreshTokenWithNetwork(String ipAddress, CommandStatusListenerWithResponse commandStatusListenerWithResponse, Network network){
        sendAlexaCommandToDeviceWithNetwork(ipAddress, MavidPacketEncoder.getPacket((byte) 0xAA,COMMAND_TYPE_REQUEST,SubCommands.ASK_ALEXA_REFRESH_TOKEN_NEW,""),
                commandStatusListenerWithResponse,network);
    }
    public void askMetaDataInfo(String ipAddress,CommandStatusListenerWithResponse commandStatusListenerWithResponse){
        sendAlexaCommandToDevice(ipAddress, MavidPacketEncoder.getPacket((byte) 0xAA,COMMAND_TYPE_REQUEST,SubCommands.ASK_META_DATA,""),commandStatusListenerWithResponse);
    }
    public void sendAlexaAuthDetails(String ipAddress, CompanionProvisioningInfo companionProvisioningInfo, CommandStatusListenerWithResponse commandStatusListener){
        sendAlexaCommandToDevice(ipAddress, MavidPacketEncoder.getPacket((byte) 0xAA,COMMAND_TYPE_REQUEST,SubCommands.SEND_AUTH_CODE,"AUTHCODE_EXCH:"+companionProvisioningInfo.toJson().toString()),commandStatusListener);
    }
    public void amazonSignout(String ipAddress,  CommandStatusListener commandStatusListener){
        sendAlexaCommandToDevice(ipAddress, MavidPacketEncoder.getPacket((byte) 0xAA,COMMAND_TYPE_REQUEST,SubCommands.AMAZON_SIGNOUT,"SIGN_OUT"),commandStatusListener);
    }

    public void setAlexaLocale(String ipAddress,String locale,CommandStatusListenerWithResponse commandStatusListenerWithResponse){
        sendAlexaCommandToDevice(ipAddress, MavidPacketEncoder.getPacket((byte) 0xAA,COMMAND_TYPE_REQUEST,SubCommands.UPDATE_LOCALE,"UPDATE_LOCALE:"+locale),commandStatusListenerWithResponse);
    }

    public void getAlexaLangugeList(String ipAddress,CommandStatusListenerWithResponse commandStatusListenerWithResponse){
        sendAlexaCommandToDevice(ipAddress, MavidPacketEncoder.getPacket((byte) 0xAA,COMMAND_TYPE_REQUEST,SubCommands.GET_ALEXA_LANG,""),commandStatusListenerWithResponse);
    }
    public void setAlexaLocaleEmpty(String ipAddress,CommandStatusListenerWithResponse commandStatusListenerWithResponse){
        sendAlexaCommandToDevice(ipAddress, MavidPacketEncoder.getPacket((byte) 0xAA,COMMAND_TYPE_REQUEST,SubCommands.GET_LOCALE,""),commandStatusListenerWithResponse);
    }

//    public void setAutoOtaWrite(String ipAddress, String value, CommandStatusListenerWithResponse commandStatusListenerWithResponse){
//
//        sendAutoCommandToDevice(ipAddress, MavidPacketEncoder.getPacketAutoWrite((byte) 0xAA,COMMAND_TYPE_REQUEST,SubCommands.COMP_APP_WRITE_ENV,value),commandStatusListenerWithResponse);
//    }

       // sendAutoCommandToDevice(ipAddress, MavidPacketEncoder.getPacketAutoWrite((byte) 0xAA,COMMAND_TYPE_REQUEST,SubCommands.COMP_APP_WRITE_ENV,value),commandStatusListenerWithResponse);
   // }


//    public void setZigBeeWrite(String ipAddress, String value, CommandStatusListenerWithResponse commandStatusListenerWithResponse){
//
//        sendAutoCommandToDevice(ipAddress, MavidPacketEncoder.getPacketZigBeeWrite((byte) 0xAA,COMMAND_TYPE_REQUEST,SubCommands.COMP_APP_WRITE_ENV,value),commandStatusListenerWithResponse);
//    }
//    public void setReadZigBee(String ipAddress,String value,CommandStatusListenerWithResponse commandStatusListenerWithResponse){
//        sendAutoCommandToDevice(ipAddress, MavidPacketEncoder.getPacketZigBee((byte) 0xAA,COMMAND_TYPE_REQUEST,SubCommands.COMP_APP_READ_ENV,value),commandStatusListenerWithResponse);
//
//    }
//    public void setAutoOtaRead(String ipAddress,String value, CommandStatusListenerWithResponse commandStatusListenerWithResponse){
//
//        sendAutoCommandToDevice(ipAddress, MavidPacketEncoder.getPacketAutoRead((byte) 0xAA,COMMAND_TYPE_REQUEST,SubCommands.COMP_APP_READ_ENV,value),commandStatusListenerWithResponse);
//    }

//    public void startOTAUpgrade(String ipAddress,final CommandStatusListener commandStatusListener){
//        communicateToDevice(ipAddress, MavidPacketEncoder.getPacket((byte) 0xAA, COMMAND_TYPE_REQUEST, SubCommands.START_OTA_UPGRADE, ""), new CommandStatusListener() {
//            @Override
//            public void failure(Exception e) {
//                commandStatusListener.failure(e);
//            }
//
//            @Override
//            public void success() {
//                commandStatusListener.success();
//            }
//        });
//    }

    public void sendCustomCommands(String ipAddress, int messageBox, String message
            ,CommandStatusListenerWithResponse commandStatusListener){
        communicateToDevice(ipAddress
                ,MavidPacketEncoder.getPacket((byte) 0xAA, COMMAND_TYPE_REQUEST, messageBox, message)
                ,commandStatusListener);

    }
    public void sendCustomCommands(String ipAddress, int messageBox, String message
            ,CommandStatusListener commandStatusListener){
        communicateToDevice(ipAddress
                ,MavidPacketEncoder.getPacket((byte) 0xAA, COMMAND_TYPE_REQUEST, messageBox, message)
                ,commandStatusListener);
    }




    private void sendAlexaCommandToDevice(String ipAddress,byte[] dataPacket,CommandStatusListenerWithResponse commandStatusListener) {
      communicateToDevice(ipAddress,dataPacket,commandStatusListener);
    }

    private void sendAlexaCommandToDeviceWithNetwork(String ipAddress,byte[] dataPacket,CommandStatusListenerWithResponse commandStatusListener,Network network) {
        communicateToDeviceWithNetwork(ipAddress,dataPacket,commandStatusListener,network);
    }

    private void sendAutoCommandToDevice(String ipAddress,byte[] dataPacket,CommandStatusListenerWithResponse commandStatusListener) {
        communicateToDevice(ipAddress,dataPacket,commandStatusListener);
    }

    private void sendAlexaCommandToDevice(String ipAddress,byte[] dataPacket,CommandStatusListener commandStatusListener) {
        communicateToDevice(ipAddress,dataPacket,commandStatusListener);
    }
    private void communicateToDevice(String ipAddress,byte[] dataPacket,CommandStatusListenerWithResponse commandStatusListener){
        // make a http connection
        HttpMavidClient client = new HttpMavidClient(ipAddress,dataPacket,commandStatusListener);
        client.execute();
    }
    private void communicateToDeviceWithNetwork(String ipAddress,byte[] dataPacket,CommandStatusListenerWithResponse commandStatusListener,Network network){
        // make a http connection
        HttpMavidClient client = new HttpMavidClient(ipAddress,dataPacket,commandStatusListener,network);
        client.execute();
    }

    private void communicateToDevice(String ipAddress,byte[] dataPacket,CommandStatusListener commandStatusListener){
        // make a http connection
        HttpMavidClient client = new HttpMavidClient(ipAddress,dataPacket,commandStatusListener);
        client.execute();
    }


    private interface SubCommands{
        int START_OTA_UPGRADE = 1;
        int ASK_ALEXA_REFRESH_TOKEN = 208;

        int ASK_ALEXA_REFRESH_TOKEN_NEW = 233;

        int ASK_META_DATA = 234;
        int SEND_AUTH_CODE = 235;
        int AMAZON_SIGNOUT = 236;
        int UPDATE_LOCALE = 237;
        int GET_ALEXA_LANG=238;
        int GET_LOCALE = 238;
//        int COMP_APP_READ_ENV = 249;
//        int COMP_APP_WRITE_ENV = 248;
    }
}
