package com.libre.libresdk.TaskManager.Discovery.MessageDecoder;

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Created by bhargav on 2/2/18.
 */

public class DatagramDecoder {
    private DatagramPacket datagramPacket;
    public DatagramDecoder(DatagramPacket datagramPacket){
        this.datagramPacket = datagramPacket;
    }

    private DatagramPacket getDatagramPacket() {
        return datagramPacket;
    }

    private void setDatagramPacket(DatagramPacket datagramPacket) {
        this.datagramPacket = datagramPacket;
    }

    public InetAddress getIpaddress(){
        return datagramPacket.getAddress();
    }

    public int getPort(){
        return datagramPacket.getPort();
    }

    public int getLength(){
        return datagramPacket.getLength();
    }

    public String getMessage(){
        return new String(datagramPacket.getData());
    }


}
