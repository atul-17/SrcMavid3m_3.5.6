package com.libre.libresdk.TaskManager.Communication.Packet.Decoder;

import com.libre.libresdk.Util.LibreLogger;

import java.io.UnsupportedEncodingException;

/**
 * Created by bhargav on 9/2/18.
 */

public class MavidPacketDecoder {
    byte header;
    byte commandType;
    byte messageBox;
    short dataLength;
    byte crc;
    String payload;

    int readDataLength;
    public MavidPacketDecoder(byte[] data,int dataLength){
        this.readDataLength = dataLength;
        setHeader(data[0]);
        setCommandType(data[1]);
        setMessageBox(data[2]);
        setDataLength();
        setCrc(data[5]);
        try {
            setPayload(new String(getPayloadArr(data), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private int getPayloadDataLength(byte[] buf) {
        byte b1 = buf[3];
        byte b2 = buf[4];
        short s = (short) (b1<<8 | b2 & 0xFF);

        LibreLogger.d(this,"Data length is returned as "+s);

        return s;
    }
    public MavidPacketDecoder(byte[] data){
        this.readDataLength = data.length;
        setHeader(data[0]);
        setCommandType(data[1]);
        setMessageBox(data[2]);
        setDataLength();
        setCrc(data[5]);
        try {
            setPayload(new String(getPayloadArr(data), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private byte[] getPayloadArr(byte[] data) {
        int newArrSize = getDataLength();
        int pos = 6;
        byte[] payloadArr = new byte[newArrSize];
            for (int i = 0 ; i<getDataLength();i++){
                payloadArr[i] = data[pos++];
            }
        return payloadArr;
    }

    public byte getHeader(){
        return this.header;
    }
    public void setHeader(byte header){
        this.header = header;
    }
    public byte getCommandType(){
        return this.commandType;
    }
    public void setCommandType(byte commandType){
        this.commandType = commandType;
    }
    public byte getMessageBox(){
        return this.messageBox;
    }
    public void setMessageBox(byte messageBox){
        this.messageBox = messageBox;
    }
    public short getDataLength(){
        return this.dataLength;
    }
    public void setDataLength(){
        this.dataLength = (short)(this.readDataLength-6);
    }
    public byte getCrc(){
        return this.crc;
    }
    public void setCrc( byte crc){
        this.crc = crc;
    }
    public String getPayload(){
        return this.payload;
    }
    public void setPayload(String payload){
        this.payload = payload;
    }

}
