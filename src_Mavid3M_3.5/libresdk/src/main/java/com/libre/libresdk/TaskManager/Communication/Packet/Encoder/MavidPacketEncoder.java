package com.libre.libresdk.TaskManager.Communication.Packet.Encoder;

/**
 * Created by bhargav on 8/2/18.
 */

public class MavidPacketEncoder {
    byte header;
    byte commandType;
    byte messageBox;
    short dataLength;
    byte crc;
    String payload;
    byte[] dataArray;
    private static int PACKET_INFO_SIZE = 6;
    public MavidPacketEncoder(byte header, byte commandType, byte messageBox, String payload){
        this.header = header;
        this.commandType = commandType;
        this.messageBox = messageBox;
        this.payload = payload;
        this.dataLength = (short) payload.length();
    }

      public static byte[] getPacket(byte header,int commandType, int messageBox, String payload){
        byte[] bytes = new byte[PACKET_INFO_SIZE + payload.length()];
        MavidPacketEncoder mavidPacket = new MavidPacketEncoder((byte) header,(byte) commandType,(byte) messageBox,payload);
        fillByteArray(bytes,mavidPacket);
        /* get message box number
        int i= (bytes[2]<< 0)&0xff;*/
        /*  get length
        int i= (bytes[3]<< 8)&0xff00|
                (bytes[4]<< 0)&0x00ff;*/
        return bytes;
    }

    public static byte[] getPacketAutoWrite(byte header, int commandType, int messageBox, String payload){
        byte[] bytes = new byte[PACKET_INFO_SIZE + payload.length()+2];
        MavidPacketEncoder mavidPacket = new MavidPacketEncoder((byte) header,(byte) commandType,(byte) messageBox,payload);
        fillByteArrayAutoWrite(bytes,mavidPacket);
        /* get message box number
        int i= (bytes[2]<< 0)&0xff;*/
        /*  get length
        int i= (bytes[3]<< 8)&0xff00|
                (bytes[4]<< 0)&0x00ff;*/
        return bytes;
    }
    public static byte[] getPacketZigBeeWrite(byte header, int commandType, int messageBox, String payload){
        byte[] bytes = new byte[PACKET_INFO_SIZE + payload.length()+2];
        MavidPacketEncoder mavidPacket = new MavidPacketEncoder((byte) header,(byte) commandType,(byte) messageBox,payload);
        fillByteArrayZigBeeWrite(bytes,mavidPacket);
        /* get message box number
        int i= (bytes[2]<< 0)&0xff;*/
        /*  get length
        int i= (bytes[3]<< 8)&0xff00|
                (bytes[4]<< 0)&0x00ff;*/
        return bytes;
    }
    public static byte[] getPacketZigBee(byte header, int commandType, int messageBox, String payload){
        byte[] bytes = new byte[PACKET_INFO_SIZE +2];
        MavidPacketEncoder mavidPacket = new MavidPacketEncoder((byte) header,(byte) commandType,(byte) messageBox,payload);
        fillByteArrayZigBeeRead(bytes,mavidPacket);
        /* get message box number
        int i= (bytes[2]<< 0)&0xff;*/
        /*  get length
        int i= (bytes[3]<< 8)&0xff00|
                (bytes[4]<< 0)&0x00ff;*/
        return bytes;
    }
    public static byte[] getPacketAutoRead(byte header, int commandType, int messageBox, String payload){
        byte[] bytes = new byte[PACKET_INFO_SIZE +2];
        MavidPacketEncoder mavidPacket = new MavidPacketEncoder((byte) header,(byte) commandType,(byte) messageBox,payload);
        fillByteArrayAutoRead(bytes,mavidPacket);
        /* get message box number
        int i= (bytes[2]<< 0)&0xff;*/
        /*  get length
        int i= (bytes[3]<< 8)&0xff00|
                (bytes[4]<< 0)&0x00ff;*/
        return bytes;
    }




    private static void fillByteArray(byte[] bytes, MavidPacketEncoder mavidPacket) {
        bytes[0] = mavidPacket.getHeader();
        bytes[1] = mavidPacket.getCommandType();
        bytes[2] = mavidPacket.getMessageBox();

        bytes[3] = (byte) ((mavidPacket.getDataLength() & 0xFF00) >> 8);
        bytes[4] = (byte) (mavidPacket.getDataLength() & 0x00FF);

        bytes[5] = mavidPacket.getCRC();

        byte[] payloadArr = mavidPacket.getPayload().getBytes();
        fillPayloadArray(bytes,payloadArr);
    }

    private static void fillByteArrayAutoWrite(byte[] bytes, MavidPacketEncoder mavidPacket) {
        bytes[0] = mavidPacket.getHeader();
        bytes[1] = mavidPacket.getCommandType();
        bytes[2] = mavidPacket.getMessageBox();

        int payloadLen =mavidPacket.getDataLength() +2 ;

        bytes[3] = (byte) ((payloadLen & 0xFF00) >> 8);
        bytes[4] = (byte) (payloadLen & 0x00FF);

        bytes[5] = mavidPacket.getCRC();

        byte[] newPayload = new byte[2+mavidPacket.getPayload().length()];
        newPayload[0] = (byte) (44 & 0x00FF);
        newPayload[1] = (byte) ((44 & 0xFF00) >> 8);
        byte[] payloadArr = mavidPacket.getPayload().getBytes();
        fillNewPayloadArray(newPayload,payloadArr);
        fillPayloadArray(bytes,newPayload);
    }

    private static void fillByteArrayZigBeeWrite(byte[] bytes, MavidPacketEncoder mavidPacket) {
        bytes[0] = mavidPacket.getHeader();
        bytes[1] = mavidPacket.getCommandType();
        bytes[2] = mavidPacket.getMessageBox();

        int payloadLen =mavidPacket.getDataLength() +2 ;

        bytes[3] = (byte) ((payloadLen & 0xFF00) >> 8);
        bytes[4] = (byte) (payloadLen & 0x00FF);

        bytes[5] = mavidPacket.getCRC();

        byte[] newPayload = new byte[2+mavidPacket.getPayload().length()];
        newPayload[0] = (byte) (0);
        newPayload[1] = (byte) (0);
        byte[] payloadArr = mavidPacket.getPayload().getBytes();

        fillNewPayloadArray(newPayload,payloadArr);

        fillPayloadArray(bytes,newPayload);
    }

    private static void fillByteArrayZigBeeRead(byte[] bytes, MavidPacketEncoder mavidPacket) {
        bytes[0] = mavidPacket.getHeader();
        bytes[1] = mavidPacket.getCommandType();
        bytes[2] = mavidPacket.getMessageBox();

        int payloadLen =mavidPacket.getDataLength() +2 ;

        bytes[3] = (byte) ((payloadLen & 0xFF00) >> 8);
        bytes[4] = (byte) (payloadLen & 0x00FF);

        bytes[5] = mavidPacket.getCRC();

        byte[] newPayload = new byte[2];
        newPayload[0] = (byte) (0);
        newPayload[1] = (byte) (0);
        /*byte[] payloadArr = mavidPacket.getPayload().getBytes();
        fillNewPayloadArray(newPayload,payloadArr);*/

        fillPayloadArray(bytes,newPayload);
    }

    private static void fillByteArrayAutoRead(byte[] bytes, MavidPacketEncoder mavidPacket) {
        bytes[0] = mavidPacket.getHeader();
        bytes[1] = mavidPacket.getCommandType();
        bytes[2] = mavidPacket.getMessageBox();

        int payloadLen =mavidPacket.getDataLength() +2 ;

        bytes[3] = (byte) ((payloadLen & 0xFF00) >> 8);
        bytes[4] = (byte) (payloadLen & 0x00FF);

        bytes[5] = mavidPacket.getCRC();

        byte[] newPayload = new byte[2];
        newPayload[0] = (byte) (44 & 0x00FF);
        newPayload[1] = (byte) ((44 & 0xFF00) >> 8);
        /*byte[] payloadArr = mavidPacket.getPayload().getBytes();
        fillNewPayloadArray(newPayload,payloadArr);*/

        fillPayloadArray(bytes,newPayload);
    }

    private static void fillNewPayloadArray(byte[] bytes, byte[] payloadArr) {
        int i = 2;
        for (byte payloadByte : payloadArr){
            bytes[i++] = payloadByte;
        }
    }

    private static void fillPayloadArray(byte[] bytes, byte[] payloadArr) {
        int i = PACKET_INFO_SIZE;
        for (byte payloadByte : payloadArr){
            bytes[i++] = payloadByte;
        }
    }

    public byte getHeader(){
        return header;
    }
    public byte getCommandType(){
        return commandType;
    }
    public byte getMessageBox(){
        return messageBox;
    }
    public short getDataLength(){
        return dataLength;
    }
    public byte getCRC(){
        return crc;
    }
    public String getPayload(){
        return payload;
    }
}

