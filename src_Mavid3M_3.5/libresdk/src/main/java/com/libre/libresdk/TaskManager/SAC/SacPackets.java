package com.libre.libresdk.TaskManager.SAC;

import com.libre.libresdk.Util.LibreLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class SacPackets {

    byte[] encryptedssid, encryptedpassphrase;
    String ssid, passphrase;
    int security;
    String originalssid, originalpassphrase, iv;
//    byte[] ssidLengthPacket;
//    byte[] passphraseLengthPacket;
    byte[] securityModePacket;
    byte[] eofPacket;


    public SacPackets(String iv, byte[] encryptedssid, byte[] encryptedpassPhrase, String originalSsid, String originalPassphrase, int security) {
        this.iv = iv;
        this.encryptedssid = encryptedssid;
        this.encryptedpassphrase = encryptedpassPhrase;
        this.originalssid = originalSsid;
        this.originalpassphrase = originalPassphrase;
        this.security = security;
    }

    public byte[] finalPacket() throws IOException {

        byte ivLengthPacket = (byte) (iv.length() & 0x00FF);

        /*suma changes for the byte encoding pattern for chinese characters*/

        String cipherPasswordString=new String(originalpassphrase.getBytes(),"UTF-8");
        String cipherSsidString=new String(originalssid.getBytes(),"UTF-8");
        LibreLogger.d(this,"suma utf encoded ssid string"+cipherSsidString);
        LibreLogger.d(this,"suma utf encoded password string"+cipherPasswordString);
        byte ssidLengthPacket = (byte) (cipherSsidString.getBytes().length & 0x00FF);
        byte passphraseLengthPacket = (byte) ( cipherPasswordString.getBytes().length & 0x00FF);

        //security packet
        securityModePacket = new byte[2];
        securityModePacket[0] = (byte) ((security & 0xFF00) >> 8);
        securityModePacket[1] = (byte) (security & 0x00FF);

        /// escapeMetaCharacters("suma\\");

        //eof packet
        eofPacket = new byte[4];
        eofPacket[0] = (byte) (0xAB & 0xFF);
        eofPacket[1] = (byte) (0xCD & 0xFF);
        eofPacket[2] = (byte) (0xEF & 0xFF);
        eofPacket[3] = (byte) (0x09 & 0xFF);


        //ssid
        byte[] ssidValue = encryptedssid;

        //passphrase
        byte[] passPhraseValue = encryptedpassphrase;

        byte[] ivValue = iv.getBytes();
        //concating all bytes

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        outputStream.write("Connect:".getBytes());
        outputStream.write(ivLengthPacket);
        outputStream.write(ivValue);
        outputStream.write(ssidLengthPacket);
        outputStream.write(ssidValue);
        if (passphraseLengthPacket == 0) {
            outputStream.write(passphraseLengthPacket);
        } else {
            outputStream.write(passphraseLengthPacket);
            outputStream.write(passPhraseValue);
        }
        outputStream.write(securityModePacket);
        outputStream.write(eofPacket);
        outputStream.flush();

        byte[] finalPacket = outputStream.toByteArray();
        String finalPacketString = new String(finalPacket, "UTF-8");
        LibreLogger.d(this, "suma needs to know the final packet string" + finalPacketString);
        LibreLogger.d("SACPackets", "dummy: " + String.format("%02x", finalPacket[0]));
        LibreLogger.d("SACPackets", "dreamgirl: " + String.format("%02x", finalPacket[3]));
        LibreLogger.d("SACPackets", "dreamgirl: " + String.format("%02x", finalPacket[5]));
        LibreLogger.d("SACPackets", "dreamgirl: " + String.format("%02x", finalPacket[6]));
        return finalPacket;
    }

//    public String escapeMetaCharacters(String inputString){
//        final String[] metaCharacters = {"\\","^","$","{","}","[","]","(",")",".","*","+","?","|","<",">","-","&","%"};
//
//        for (int i = 0 ; i < metaCharacters.length ; i++){
//            if(inputString.contains(metaCharacters[i])){
//                inputString = inputString.replace(metaCharacters[i],"\\"+metaCharacters[i]);
//            }
//        }
//        Log.d("suma ","escape meta characters"+inputString);
//        return inputString;
//    }
}
