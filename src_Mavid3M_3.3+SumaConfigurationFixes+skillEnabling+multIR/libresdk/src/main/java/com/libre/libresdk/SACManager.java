package com.libre.libresdk;

import android.os.Bundle;
import android.util.Log;

import com.libre.libresdk.Constants.BundleConstants;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.MessageInfo;
import com.libre.libresdk.TaskManager.SAC.Listeners.SACListener;
import com.libre.libresdk.TaskManager.SAC.Listeners.SACListenerWithResponse;
import com.libre.libresdk.TaskManager.SAC.SacPackets;
import com.libre.libresdk.Util.LibreLogger;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class SACManager {
    private int MAX_ITERATION_COUNT = 3;
    static SACManager sacManager;
    private static final int STATUS_OK = 0;
    private static final int STATUS_ERROR = 1;
    Socket socket;
    private String SAC_IPADDRESS = "192.168.10.1";
    private int SAC_PORT = 7777;
    int dataSendingIterationCount =0;
//    String key="5375f0a6c57f076093602318907e44f2";
    String decryptedSsidText,decryptedPasswordText;
    String cipherPasswordString,cipherTextString;
    String passPhrase;
    SACManager(){
    }

    public static SACManager getManager(){
        if (sacManager==null){
            sacManager = new SACManager();
        }
        return sacManager;
    }
    public void configure(final Bundle params, final SACListener sacListener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                getManager().sendSACDataToDevice(params,sacListener);
            }
        }).start();
    }

    public byte[] configureBLE(final Bundle params, final SACListener sacListener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                getManager().sendSACDataToDeviceBLE(params,sacListener);
            }
        }).start();

        return getManager().sendSACDataToDeviceBLE(params,sacListener);
    }


    public void sendCustomSACMessage(final byte[] message,final SACListenerWithResponse sacListenerWithResponse){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket = null;
                DataOutputStream dataOutputStream = null;
                try {
                    // Create a new Socket instance and connect to host
                    socket = getClientSocket();
                    // connection successful
                    dataOutputStream = new DataOutputStream(
                            socket.getOutputStream());
                    InputStream inputStream = socket.getInputStream();
                    dataOutputStream.write(message);
                    dataOutputStream.flush();
                    //after response from device. Either close the socket or send credentials again
                    int serverResponseStatus;
                    String output = "";
                    //read header
                    byte[] buffer = new byte[6];
                    int expectedMessageLength=0;
                    int ipReadLength = 0;
                    if ((serverResponseStatus = inputStream.read(buffer)) != -1 ){
                        if (isExpectedHeader(buffer[0])){
                            expectedMessageLength = getDataLength(buffer);
                            //now read the payload
                            buffer = new byte[1000];
                            LibreLogger.d(this,"Read data length is "+expectedMessageLength);
                            //adding header length
                            expectedMessageLength+=6;
                            ipReadLength += serverResponseStatus;
                            //output += new String(buffer, 6, serverResponseStatus-6);
                            while(( (ipReadLength<expectedMessageLength )
                                    && (serverResponseStatus = inputStream.read(buffer)) != -1)) {
                                output += new String(buffer, 0, serverResponseStatus);
                                ipReadLength+=serverResponseStatus;
                                LibreLogger.d(this,"Read data length till now is "+ipReadLength+", expected is "+expectedMessageLength);
                               /* if (ipReadLength >= expectedMessageLength){
                                    LibreLogger.d(this,"Got full Read data, expected :"+expectedMessageLength+" , read data is :"+ipReadLength);
                                    break;
                                }*/
                            }

                            //output is the full data expected from the device.
                            LibreLogger.d(this,"Read data Payload received as "+output);

                            LibreLogger.d(this,"got scan list response from SAC device "+output.length()+" ,"+output);
                            sacListenerWithResponse.response(new MessageInfo(socket.getInetAddress().getHostAddress(),output));
                        }
                    }
                } catch (Exception e){
                    LibreLogger.d(this,"scan list exception"+e.getMessage());
                    sacListenerWithResponse.failure(e);
                }
            }
        }).start();
    }

    private boolean isExpectedHeader(byte b) {
        if (b == (byte)0xAA){
            return true;
        }
        return false;
    }


    public void sendSACDataToDevice(final Bundle params, final SACListener sacListener) {
        Socket socket = null;
        DataOutputStream dataOutputStream = null;
        try {
            // Create a new Socket instance and connect to host
            socket = getClientSocket();
            // connection successful
            dataOutputStream = new DataOutputStream(
                    socket.getOutputStream());
            InputStream inputStream = socket.getInputStream();


            Cipher cipher = null;
            try {
                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            }
            byte[] iv = new byte[0];
            if (cipher != null) {
                iv = new byte[cipher.getBlockSize()];
                System.out.println("block size : "+cipher.getBlockSize());
            }
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            final StringBuilder builder = new StringBuilder();
            for(byte b : ivParameterSpec.getIV()) {
                builder.append(String.format("%02x", b));
            }
            String ivHexString=builder.toString();
            byte[] convertedHexbyte = hexStrToByteArray(builder.toString());
            LibreLogger.d(this,"suma need to know IV bytes hex value"+builder.toString());

            LibreLogger.d(this,"suma need to know IV bytes hex value length"+iv.length);
            byte[] hexaKey=hexStrToByteArray(LibreMavidHelper.symmentricKey);
            LibreLogger.d(this,"suma need to know IV bytes app security key"+LibreMavidHelper.symmentricKey);

            // transfer packet to the server
            String ssid = params.getString(BundleConstants.SACConstants.SSID);
            String passPhrase = params.getString(BundleConstants.SACConstants.PASSPHRASE);
            int security= params.getInt(BundleConstants.SACConstants.NETWORK_TYPE);

            byte[] cipherText = new byte[0];
            try {
                assert ssid != null;
                cipherText = encrypt(ssid.getBytes(),hexaKey,convertedHexbyte);
                cipherTextString= new String(cipherText, "UTF-8");
                LibreLogger.d(this,"suma need to know ssid encrypt final value"+cipherTextString);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                decryptedSsidText = decrypt(cipherText,hexaKey,convertedHexbyte);
                //String ssidDecryptIV = new String(ivParameterSpec.getIV(), "UTF-8");
                //LibreLogger.d(this,"suma need to know ssid encrypt ssidDecryptIV value"+ssidDecryptIV);
                LibreLogger.d(this,"suma need to know ssid decrypt final value :"+decryptedSsidText);
            } catch (Exception e) {
                e.printStackTrace();
            }

            byte[] cipherTextPassword = new byte[0];
            try {
                assert passPhrase != null;
                cipherTextPassword = encrypt(passPhrase.getBytes(),hexaKey,convertedHexbyte);
                cipherPasswordString = new String(cipherTextPassword, "UTF-8");
                LibreLogger.d(this,"suma need to know password encrypt final value"+cipherPasswordString);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                decryptedPasswordText = decrypt(cipherTextPassword,hexaKey, convertedHexbyte);
                String ssidDecryptIV = new String(ivParameterSpec.getIV(), "UTF-8");
                LibreLogger.d(this,"suma need to know password decrypt final value"+decryptedPasswordText);

            } catch (Exception e) {
                e.printStackTrace();
            }

            SacPackets sacPackets = new SacPackets(ivHexString,cipherText, cipherTextPassword,ssid,passPhrase,security);
            LibreLogger.d(this,"suma need to know final value originalssid"+ssid);
            LibreLogger.d(this,"suma need to know final value originalpassphrase"+passPhrase);
            dataOutputStream.write(sacPackets.finalPacket());
            LibreLogger.d(this, "waiting for response from host:  " + new String(sacPackets.finalPacket(), 0));
            //after response from device. Either close the socket or send credentials again
            int serverResponseStatus;
            String output = "";
            byte[] buffer = new byte[10];
            if((serverResponseStatus = inputStream.read(buffer)) != -1) {
                output += new String(buffer, 0, serverResponseStatus);
                LibreLogger.d(this,"got response from SAC device "+output);
            }
            if (!isResponsePositive(buffer)){
                //recursive call
                sacListener.failure("Failed to connect");
            }else {
                sacListener.success();
                socket.close();
                setClientSocket(null);
            }
        } catch (IOException e) {
            sacListener.failure(e.getMessage());
            Log.d("atul_io_exception",e.getMessage());
        } catch (Exception e){
            Log.d("atul_socket_exception",e.getMessage());
        }

    }


    private byte[] sendSACDataToDeviceBLE(Bundle params,SACListener sacListener) {
//        Socket socket = null;
//        DataOutputStream dataOutputStream = null;
        try {
            // Create a new Socket instance and connect to host
//            socket = getClientSocket();
            // connection successful
//            dataOutputStream = new DataOutputStream(
//                    socket.getOutputStream());
//            InputStream inputStream = socket.getInputStream();


            Cipher cipher = null;
            try {
                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            }
            byte[] iv = new byte[0];
            if (cipher != null) {
                iv = new byte[cipher.getBlockSize()];
                System.out.println("block size : "+cipher.getBlockSize());
            }
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            final StringBuilder builder = new StringBuilder();
            for(byte b : ivParameterSpec.getIV()) {
                builder.append(String.format("%02x", b));
            }
            String ivHexString=builder.toString();
            byte[] convertedHexbyte = hexStrToByteArray(builder.toString());
            LibreLogger.d(this,"suma need to know IV bytes hex value"+builder.toString());

            LibreLogger.d(this,"suma need to know IV bytes hex value length"+iv.length);

            byte[] hexaKey=hexStrToByteArray(LibreMavidHelper.symmentricKey);

//            LibreLogger.d(this,"suma need to know IV value"+ ivStringConverted);
//            byte[] cipherText = new byte[0];
//            try {
//                cipherText = encrypt(,key, ivParameterSpec.getIV());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            System.out.println("Encrypted Text : "+ Base64.encodeToString(cipherText,Base64.NO_PADDING));
//            String finalEncryptedString= Base64.encodeToString(cipherText,Base64.NO_PADDING);


            // transfer packet to the server
            String ssid = params.getString(BundleConstants.SACConstants.SSID);
            String passPhrase = params.getString(BundleConstants.SACConstants.PASSPHRASE);
            int security= params.getInt(BundleConstants.SACConstants.NETWORK_TYPE);

            byte[] cipherText = new byte[0];
            byte[] ssidArray = new byte[0];
            String ssidEncrypt;
            try {
                assert ssid != null;
                String encodedSsid=new String(ssid.getBytes(),"UTF-8");

                cipherText = encrypt(encodedSsid.getBytes(),hexaKey,convertedHexbyte);

                //ssidEncrypt = new String(cipherText, "UTF-8");
               // ssidArray=ssidEncrypt.getBytes("UTF-8");
               // LibreLogger.d(this,"suma need to know ssid encrypt final value"+ssidEncrypt);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                decryptedSsidText = decrypt(cipherText,hexaKey,convertedHexbyte);
                //String ssidDecryptIV = new String(ivParameterSpec.getIV(), "UTF-8");
                //LibreLogger.d(this,"suma need to know ssid encrypt ssidDecryptIV value"+ssidDecryptIV);
                LibreLogger.d(this,"suma need to know ssid decrypt final value :"+decryptedSsidText);
            } catch (Exception e) {
                e.printStackTrace();
            }

            byte[] cipherTextPassword = new byte[0];

            try {
                String encodedPassPhrase = new String(passPhrase.getBytes(),"UTF-8");

                cipherTextPassword = encrypt(encodedPassPhrase.getBytes(),hexaKey,convertedHexbyte);

               // cipherPasswordString = new String(cipherTextPassword, "UTF-8");

               // passwordArray=cipherPasswordString.getBytes("UTF-8");

                LibreLogger.d(this,"suma need to know password encrypt final value"+cipherPasswordString);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                decryptedPasswordText = decrypt(cipherTextPassword,hexaKey, convertedHexbyte);
                String ssidDecryptIV = new String(ivParameterSpec.getIV(), "UTF-8");
                LibreLogger.d(this,"suma need to know password decrypt final value"+decryptedPasswordText);

            } catch (Exception e) {
                e.printStackTrace();
            }

            SacPackets sacPackets = new SacPackets(ivHexString,cipherText, cipherTextPassword,ssid,passPhrase,security);
            LibreLogger.d(this,"suma need to know final value originalssid"+ssid);
            LibreLogger.d(this,"suma need to know final value originalpassphrase"+sacPackets.finalPacket().length);
            sacListener.successBLE(sacPackets.finalPacket());
            return sacPackets.finalPacket();

        } catch (IOException e) {
            sacListener.failure(e.getMessage());
        } catch (Exception e){

        }
        return null;
    }

    public byte[] hexStrToByteArray(String hex) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(hex.length() / 2);

        for (int i = 0; i < hex.length(); i += 2) {
            String output = hex.substring(i, i + 2);
            int decimal = Integer.parseInt(output, 16);
            baos.write(decimal);
        }
        return baos.toByteArray();
    }

    public byte[] encrypt(byte[] plaintext, byte[] key, byte[] IV) throws Exception
    {
        //Get Cipher Instance
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        //Create SecretKeySpec
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

        //Create IvParameterSpec
        IvParameterSpec ivSpec = new IvParameterSpec(IV);

        //Initialize Cipher for ENCRYPT_MODE
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        //Perform Encryption
        return cipher.doFinal(plaintext);
    }
    public static String decrypt (byte[] cipherText, byte[] key, byte[] IV) throws Exception
    {
        //Get Cipher Instance
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        //Create SecretKeySpec
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

        //Create IvParameterSpec
        IvParameterSpec ivSpec = new IvParameterSpec(IV);

        //Initialize Cipher for DECRYPT_MODE
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        //Perform Decryption
        byte[] decryptedText = cipher.doFinal(cipherText);

        return new String(decryptedText);

    }
    private void setDataSendingIterationCount(int count){
        this.dataSendingIterationCount = count;
    }
    private int getDataSendingIterationCount(){
        return this.dataSendingIterationCount;
    }

    private void setMaxIterationCount(int count){
        this.MAX_ITERATION_COUNT = count;
    }
    private int getMaxIterationCount(){
        return this.dataSendingIterationCount;
    }
    private static boolean isResponsePositive(byte[] output) {
        try {
            switch ((int)output[1]){
                case STATUS_OK:
                    //status is OK
                    return true;
                case STATUS_ERROR:
                    //status is ERROR
                    return false;
            }
        }catch (Exception e){
            Log.d("SACManager",""+e.getMessage());
        }
        return false;
    }

    public String getSAC_IPADDRESS() {
        return SAC_IPADDRESS;
    }

    public void setSAC_IPADDRESS(String SAC_IPADDRESS) {
        this.SAC_IPADDRESS = SAC_IPADDRESS;
    }

    public int getSAC_PORT() {
        return SAC_PORT;
    }

    public void setSAC_PORT(int SAC_PORT) {
        this.SAC_PORT = SAC_PORT;
    }

    public Socket getClientSocket() throws IOException {
        if (socket==null || socket.isClosed() || !socket.isConnected()){
            socket = new Socket(getManager().getSAC_IPADDRESS(), getManager().getSAC_PORT());
        }
        Log.d("SACManager","Socket successful");
        return socket;
    }
    public void setClientSocket(Socket socket){
        this.socket = socket;
    }

    public int getDataLength(byte[] buf) {
        byte b1 = buf[3];
        byte b2 = buf[4];
        short s = (short) (b1<<8 | b2 & 0xFF);

        LibreLogger.d(this,"Data length is returned as "+s);

        return s;
    }
}

