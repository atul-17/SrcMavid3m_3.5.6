package com.libre.libresdk;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import com.libre.libresdk.Exceptions.WifiException;
import com.libre.libresdk.TaskManager.Discovery.Listeners.DeviceListener;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo;
import com.libre.libresdk.TaskManager.Discovery.CustomExceptions.WrongStepCallException;
import com.libre.libresdk.TaskManager.Discovery.MessageDecoder.DatagramDecoder;
import com.libre.libresdk.Util.BusEventProgressUpdate;
import com.libre.libresdk.Util.LibreLogger;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by bhargav on 2/2/18.
 */

class DiscoveryManager {
    private static DiscoveryManager discoveryManager;
    private MulticastSocket communicationSocket;
    private boolean isAllowedToListen = false;
    private WifiManager manager;
    private DhcpInfo dhcp;
    private boolean isUDPServerSocketAlive = false;
    private String advertisingMessage = "M-SEARCH * HTTP/1.1\r\n" +
            "MX: 10\r\n" +
            "ST: urn:schemas-upnp-org:device:DDMSServer:1\r\n" +
            "HOST: 239.255.255.250:1800\r\n" +
            "MAN: \"ssdp:discover\"\r\n" +
            "\r\n";
    private String LSSDP_MULTICAST_ADDRESS = "239.255.255.250";
    private int LSSDP_PORT = 1800;
    private DatagramSocket datagramSocket;
    int sourcePort = -10;
    //temporary
    private DeviceListener deviceListenerMain;
    private String advertiseeValidator;
    Thread threadToListen, threadToListenMulticast;
    private Object WifiManager;

    private DiscoveryManager() {
        setAdvertiseeValidator("MSEARCH RESPONSE");
    }

    public static DiscoveryManager getManager() {
        if (discoveryManager == null) {
            discoveryManager = new DiscoveryManager();
        }
        return discoveryManager;
    }

    public void setAdvertisingAddress(String ipAddress) {
        LSSDP_MULTICAST_ADDRESS = ipAddress;
    }

    public void setAdvertisingPort(int port) {
        LSSDP_PORT = port;
    }

    public void startDiscovery(final DeviceListener deviceListener) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // create a service here - advertise in service
                try {

                    deviceListenerMain = deviceListener;
                    getManager().advertise(deviceListener);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    //temporary
    public DeviceListener getDeviceListener() {
        return this.deviceListenerMain;
    }

    //this method is to simply send MSEARCH message. No response/look up expected
    public void advertise() throws WrongStepCallException {
        if (sourcePort == -10) {
            throw new WrongStepCallException("Some issue. Call startDiscovery again");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String message = getAdvertisingMessage();
                try {
                    DatagramPacket advertisingPacket = new DatagramPacket(message.getBytes(),
                            message.length(), InetAddress.getByName(LSSDP_MULTICAST_ADDRESS), LSSDP_PORT);
                    if (datagramSocket == null || datagramSocket.isClosed()) {
                        LibreLogger.d(this, "datagramSocket is " + datagramSocket);
                        if (datagramSocket != null) {
                            datagramSocket.close();
                            isUDPServerSocketAlive = false;
                        }


                        LibreLogger.d(this, "datagramSocket is " + datagramSocket + " creating");
                        datagramSocket = createDatagramSocket();
                        LibreLogger.d(this, "datagramSocket is " + datagramSocket + " after creating");
                        isUDPServerSocketAlive = true;
                        listenToResponse(datagramSocket);
                    }
                    Log.d("DisMgr", "Advertising the message " + message + " to multicast group");
                    for (int i = 0; i < 3; i++) {
                        Log.d("DisMgr", "advertising for " + i);
                        sendMessageToUDPSocket(advertisingPacket);
                        Thread.sleep(1000);
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (SocketException e) {
                    datagramSocket = null;
                    e.printStackTrace();
                } catch (IOException e) {
                    datagramSocket = null;
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    deviceListenerMain.failures(e);
                    e.printStackTrace();
                } catch (WifiException e) {
                    e.printStackTrace();
                    deviceListenerMain.failures(e);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    deviceListenerMain.failures(e);
                }
            }
        }).start();
    }

    //this method is to simply send MSEARCH message. No response/look up expected
    public void advertise(final String ip, final Context context) throws WrongStepCallException {
        if (sourcePort == -10) {
            throw new WrongStepCallException("Some issue. Call startDiscovery again");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String message = getAdvertisingMessage();
                try {
                    DatagramPacket advertisingPacket = new DatagramPacket(message.getBytes(),
                            message.length(), InetAddress.getByName(LSSDP_MULTICAST_ADDRESS), LSSDP_PORT);
                    if (datagramSocket == null
                            || datagramSocket.isClosed()) {
                        LibreLogger.d(this, "datagramSocket is " + datagramSocket);
                        if (datagramSocket != null) {
                            datagramSocket.close();
                            isUDPServerSocketAlive = false;
                        }
                        LibreLogger.d(this, "datagramSocket is " + datagramSocket + " creating");
                        datagramSocket = createDatagramSocket(ip, context);
                        LibreLogger.d(this, "datagramSocket is " + datagramSocket.getInetAddress() + " after creating" + ip);
                        isUDPServerSocketAlive = true;
                        listenToResponse(datagramSocket);
                    }
                    Log.d("DiscoveryManager", "Advertising the message " + message + " to multicast group");
                    for (int i = 0; i < 3; i++) {
                        Log.d("DiscoveryManager", "advertising for " + i);
                        sendMessageToUDPSocket(advertisingPacket);
                        Thread.sleep(1000);
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (SocketException e) {
                    datagramSocket = null;
                    e.printStackTrace();
                } catch (IOException e) {
                    datagramSocket = null;
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    deviceListenerMain.failures(e);
                    e.printStackTrace();
                } catch (WifiException e) {
                    e.printStackTrace();
                    deviceListenerMain.failures(e);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    deviceListenerMain.failures(e);
                }
            }
        }).start();
    }

    private void sendMessageToUDPSocket(DatagramPacket advertisingPacket) throws IOException, WifiException {
        try {
            LibreLogger.d(this, "datagramSocket send packet " + advertisingPacket);
            datagramSocket.send(advertisingPacket);
        } catch (NullPointerException e) {
            datagramSocket = createDatagramSocket();
        }
    }

    private DatagramSocket createDatagramSocket() throws SocketException, WifiException {
        DatagramSocket datagramSocket = new DatagramSocket(null);
        datagramSocket.setTrafficClass(0x10);
        datagramSocket.setReuseAddress(true);

        InetSocketAddress address = new InetSocketAddress(getLocalIpAddress(true), sourcePort);
        datagramSocket.bind(address);
        return datagramSocket;
    }


    private DatagramSocket createDatagramSocket(String ip, Context context) throws SocketException, WifiException {

        DatagramSocket datagramSocket = new DatagramSocket(null);
        datagramSocket.setTrafficClass(0x10);
        datagramSocket.setReuseAddress(true);
        InetSocketAddress address = new InetSocketAddress(ip, sourcePort);
        LibreLogger.d(this, "suma in creating datagram socket getting ip\n" + ip);
        datagramSocket.bind(address);
        return datagramSocket;
    }

    //    private DatagramSocket createDatagramSocket2(String ip) throws SocketException, WifiException {
//        DatagramSocket datagramSocket = new DatagramSocket(null);
//        datagramSocket.setTrafficClass(0x10);
//        datagramSocket.setReuseAddress(true);
//        LibreLogger.d(this,"suma in Discovery Manager create Datagram Socket\n"+new Utils().getIPAddress(true));
//
//        InetSocketAddress address = new InetSocketAddress(, sourcePort);
//        datagramSocket.bind(address);
//        return datagramSocket;
//    }
    private void advertise(DeviceListener deviceListener) {
        String message = getAdvertisingMessage();
        sourcePort = getAvailablePort();
        try {
            DatagramPacket advertisingPacket = new DatagramPacket(message.getBytes(),
                    message.length(), InetAddress.getByName(LSSDP_MULTICAST_ADDRESS), LSSDP_PORT);
            if (datagramSocket == null
                    || datagramSocket.isClosed()) {
                if (datagramSocket != null) {
                    datagramSocket.close();
                    isUDPServerSocketAlive = false;
                }
                LibreLogger.d(this, "datagramSocket is " + datagramSocket + " creating");
                datagramSocket = createDatagramSocket();
                LibreLogger.d(this, "datagramSocket is " + datagramSocket + " after creating");
                isUDPServerSocketAlive = true;
                listenToResponse(datagramSocket, deviceListener);
            }

            Log.d("DiscoveryManager", "Advertising the message " + message + " to multicast group");
            for (int i = 0; i < 1; i++) {
                Log.d("DiscoveryManager", "advertising for " + i);
                sendMessageToUDPSocket(advertisingPacket);
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            datagramSocket = null;
            e.printStackTrace();
        } catch (WifiException e) {
            e.printStackTrace();
            deviceListenerMain.failures(e);
        } catch (NullPointerException e) {
            datagramSocket = null;
            deviceListenerMain.failures(e);
            e.printStackTrace();
        } catch (Exception e) {
            datagramSocket = null;
            e.printStackTrace();
        }
    }

    //this is a temporary method. Not used inside SDK
    public int getOpenedPort() {
        return sourcePort;
    }

    private void listenToResponse(final DatagramSocket datagramSocket) {
        listenToResponse(datagramSocket, getDeviceListener());

    }

    public static String getLocalIpAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
    }

    private void listenToResponse(DatagramSocket datagramSocket, final DeviceListener deviceListener) {
        if (datagramSocket == null) {
            return;
        }

        startMulticastSocket();
        Log.d("DiscoveryManager", " listening to response");
        ListeningThread listeningThread = new ListeningThread(datagramSocket, deviceListener);
        threadToListen = new Thread(listeningThread);
        threadToListen.start();
        LibreLogger.d(this, "get thread state" + threadToListen.getState());
        MulticastListeningThread multicastListeningThread = new MulticastListeningThread(communicationSocket, deviceListener);
        threadToListenMulticast = new Thread(multicastListeningThread);
        threadToListenMulticast.start();

    }

    private void startMulticastSocket() {
        try {
            communicationSocket = new MulticastSocket(LSSDP_PORT);
            communicationSocket.joinGroup(InetAddress.getByName(LSSDP_MULTICAST_ADDRESS));
            communicationSocket.setReuseAddress(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, String> getDataMapFromMessage(Scanner sc) {
        HashMap<String, String> dataMap = new HashMap<>();
        while (sc.hasNext()) {
            String message = sc.nextLine().toString();
            Log.d("DiscoveryManager_atul", "Scanner value msgs scanner next line\n" + message);

            try {
                //except for
                //A2DP_DEVICE_UUID
                if (message.contains("A2DP_DEVICE_UUID:")) {
                    String[] dataSplitArrA2dp = message.split("A2DP_DEVICE_UUID:");
                    if (dataSplitArrA2dp.length < 2) {
                        continue;
                    }
                    dataMap.put("A2DP_DEVICE_UUID", dataSplitArrA2dp[1]);
                } else if (message.contains("FW_VERSION:")) {
                    String[] dataSplitArrFwVersion = message.split("FW_VERSION:");
                    if (dataSplitArrFwVersion.length < 2) {
                        continue;
                    }
                    dataMap.put("FW_VERSION", dataSplitArrFwVersion[1]);
                } else {
                    String[] dataSplitArr = message.split(":");
                    if (dataSplitArr.length < 2) {
                        continue;
                    }
                    dataMap.put(dataSplitArr[0], dataSplitArr[1]);
                }
                // Log.d("DiscoveryManager Suma","Scanner value msgs scanner next line SplitArray\n"+dataSplitArr[0]+"one array\n"+dataSplitArr[1]);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dataMap;
    }

    private boolean isMavidDevice(String s) {
        s = s.trim();
        if (s != null && s.equalsIgnoreCase(DiscoveryManager.getManager().getAdvertiseeValidator())) {
            return true;
        }
        /* ideally the below line must be return false.
        But, for now validation is not necessary - Dated : 7FEB2018
        */
        return false;
    }


    public String getIPAddress() throws WifiException {
        try {
            InetAddress mAddress = getLocalV4Address(getActiveNetworkInterface());
            String ipAddress = mAddress.getHostAddress();
            return ipAddress;
        } catch (Exception e) {
            WifiException wifiException = new WifiException("Getting invalid IPAddress. Check if wifi is enabled");
            wifiException.setStackTrace(e.getStackTrace());
            throw wifiException;
        }
    }

    private void getMyIP() {
        //WifiManager wifiMan = (WifiManager).getSystemService(Context.WIFI_SERVICE);
        dhcp = manager.getDhcpInfo();
        final String address = Formatter.formatIpAddress(dhcp.ipAddress); // ipAddress - IP address of my device, assigned through dhcp
        InetAddress myIP = null;

        try {
            myIP = InetAddress.getByName(address);
            LibreLogger.d(this, "suma in getMyIp by name and address\n" + myIP);
        } catch (Exception e) {
            e.printStackTrace();
            LibreLogger.d(this, "suma in getMyIp by name and address exception\n" + e);

        }

    }

    public static InetAddress getLocalV4Address(NetworkInterface netif) {

        Enumeration addrs;
        try {
            addrs = netif.getInetAddresses();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
        while (addrs.hasMoreElements()) {
            InetAddress addr = (InetAddress) addrs.nextElement();
            if (addr instanceof Inet4Address && !addr.isLoopbackAddress())
                return addr;
        }
        return null;
    }

    public static NetworkInterface getActiveNetworkInterface() throws SocketException {

        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return null;
        }

        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = iface.getInetAddresses();
            Log.d("ip check", " in getActiveInterface " + iface.getName());

            /* Check if we have a non-local address. If so, this is the active
             * interface.
             *
             * This isn't a perfect heuristic: I have devices which this will
             * still detect the wrong interface on, but it will handle the
             * common cases of wifi-only and Ethernet-only.
             */
            //for wlan0 interface
            if ((iface.getName().startsWith("w") && iface.isUp())
                    //for softAP interface
                    || (iface.getName().startsWith("sof") && iface.isUp())
                    //for p2p interface
                    || (iface.getName().startsWith("p") && iface.isUp())) {
                //this is a perfect hack for getting wifi alone

                while (inetAddresses.hasMoreElements()) {
                    InetAddress addr = inetAddresses.nextElement();

                    if (!(addr.isLoopbackAddress() || addr.isLinkLocalAddress())) {
                        Log.d("LSSDP", "DisplayName" + iface.getDisplayName() + "Name" + iface.getName() + "addr" + addr + " Host Address" + addr.getHostAddress());

                        return iface;
                    }
                }
            }
        }

        return null;
    }

    public int getAvailablePort() {
        ServerSocket socket = null;
        boolean portTaken = false;
        int START = LSSDP_PORT;
        int END = LSSDP_PORT + 1000;
        Random random = new Random();
        int max = 65535;
        int min = 10000;
        int mRandomPort = random.nextInt(max - min + 1) + min;
        while (socket == null) {
            try {
                socket = new ServerSocket(mRandomPort++);
                Log.d("DiscoveryManager", "Socket Binded To Port Number" + socket.getLocalPort());

            } catch (IOException e) {
                e.printStackTrace();
                Log.d("DiscoveryManager", "Socket Failed Binded To Port Number" + (mRandomPort - 1));
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return socket.getLocalPort();
    }

    private static boolean isNewDevice(String startLine) {
        if (startLine.equals("NOTIFY")) {
            return true;
        }
        return false;
    }

    private String getStartLine(byte[] data) {
        Scanner sc = new Scanner(new String(data));
        return sc.nextLine().toString();
    }

    private DeviceInfo getDeviceInfoFromRawData(DatagramPacket datagramPacket) {
        DatagramDecoder decoder = new DatagramDecoder(datagramPacket);
        String ipAddress = decoder.getIpaddress().getHostAddress();
        String message = decoder.getMessage();
        return new DeviceInfo(ipAddress, message, "");
    }

    public MulticastSocket getCommunicationSocket() {
        return communicationSocket;
    }

    public String getAdvertisingMessage() {
        return advertisingMessage;
    }

    public void setAdvertisingMessage(String advertisingMessageParam) {
        advertisingMessage = advertisingMessageParam;
    }


    public void stopDiscovery() {
        datagramSocket.close();
        datagramSocket = null;
        communicationSocket.close();
        communicationSocket = null;
    }

    public void setAdvertiseeValidator(String advertiseeValidator) {
        this.advertiseeValidator = advertiseeValidator;
    }

    public String getAdvertiseeValidator() {
        return advertiseeValidator;
    }


    class ListeningThread implements Runnable {

        DeviceListener threadDeviceListener;
        DatagramSocket listeningSocket;

        public ListeningThread(DatagramSocket listeningSocket, DeviceListener deviceListener) {
            this.threadDeviceListener = deviceListener;
            this.listeningSocket = listeningSocket;
            Log.d("DiscoveryManager", "starting thread ");
        }

        @Override
        public void run() {
            Log.d("DiscoveryManager", "listening to the UDP clients ");
            while (isUDPServerSocketAlive) {
                byte[] responseBytes = new byte[10024];
                DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);
                try {
                    listeningSocket.receive(responsePacket);
                    String message = new String(responseBytes, 0, responsePacket.getLength());
                    Log.d("DiscoveryManager_atul", "Someone sent a UDP packet. " + message
                            + " from " + responsePacket.getAddress().getHostAddress());
                    if (message == null || message.isEmpty()) {
                        Log.d("DiscoveryManager", "Message is empty");
                        return;
                    }

                    if (getBrandName(message).equalsIgnoreCase("Libre")||getBrandName(message).equalsIgnoreCase("Hogar")||getBrandName(message).equalsIgnoreCase("SmartIR")) {
                        Scanner sc = new Scanner(message);
                        String messageHeader = sc.nextLine();
                        if (isMavidDevice(messageHeader)) {
                            HashMap<String, String> dataMap = getDataMapFromMessage(sc);
                            Log.d("DiscoveryManager", "Scanner value msgs\n" + sc);
                            notifyDeviceEvent(responsePacket.getAddress().getHostAddress(), dataMap, threadDeviceListener);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("excep1", e.getMessage());
                }
            }
        }
    }

    class MulticastListeningThread implements Runnable {

        DeviceListener deviceListener;
        MulticastSocket multicastSocket;

        public MulticastListeningThread(MulticastSocket multicastSocket, DeviceListener deviceListener) {
            this.deviceListener = deviceListener;
            this.multicastSocket = multicastSocket;
            Log.d("DiscoveryManager", "starting thread multicast");

        }

        @Override
        public void run() {
            Log.d("DiscoveryManager", "listening to the UDP clients multicast ");
            while (isUDPServerSocketAlive) {
                byte[] responseBytes = new byte[10024];
                DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);
                try {

                    Log.d("DiscoveryManager", "Message is " + responseBytes + "packet is" + responsePacket + "resopnse" + responseBytes.length);
                    communicationSocket.receive(responsePacket);
                    String message = new String(responseBytes, 0, responsePacket.getLength());
                    Log.d("DiscoveryManager", "Someone sent a UDP packet multicast " + message
                            + " from " + responsePacket.getAddress().getHostAddress());
                    if (message == null || message.isEmpty()) {
                        Log.d("DiscoveryManager", "Message is empty multicast");
                        return;
                    }
                    String data1 = getBrandName(message);
                    Log.d("DiscoveryManagerData", " data 1 is :::" + data1);
                    if (getBrandName(message).equalsIgnoreCase("Libre")||getBrandName(message).equalsIgnoreCase("Hogar")||getBrandName(message).equalsIgnoreCase("SmartIR")) {
                        Scanner sc = new Scanner(message);
                        Log.d("DiscoveryManager", " message header" + message);
                        String messageHeader = sc.nextLine();
                        if (isMavidDevice(messageHeader)) {
                            HashMap<String, String> dataMap = getDataMapFromMessage(sc);
                            notifyDeviceEvent(responsePacket.getAddress().getHostAddress(), dataMap, deviceListener);
                        }
                   }

                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getBrandName(String data) {
        String message[] = data.split("[\\r\\n]+");
        String brandValue = "";
        for (int i = 0; i < message.length; i++) {
            if (i == 3 && message[i].contains("BRAND")) {
                brandValue = message[i].replaceAll("BRAND:", "");
            }

        }
        return brandValue;
    }

    private void notifyDeviceEvent(String ipAddress, HashMap<String, String> dataMap, final DeviceListener deviceListener) {
        final DeviceInfo deviceInfo = new DeviceInfo(ipAddress, dataMap.get("FRIENDLY_NAME"), dataMap.get("USN"));
        LibreLogger.d(this,"suma in notifydevice event get friendlyName"+deviceInfo.getFriendlyName());

//        deviceInfoMavidList.add(deviceInfo);
//        if(deviceInfoMavidList.size()>0){
//           deviceInfo.setDeviceListAdded(true);
//        }
//        else{
//            deviceInfo.setDeviceListAdded(false);
//        }

        Log.d("atul_USN", dataMap.get("USN"));

        Log.d("DiscoveryManager_atul", "F:  " + dataMap.get("A2DP_STATUS") +
                "deviceName:  " + dataMap.get("A2DP_DEVICE") + " deviceUUId:  " + dataMap.get("A2DP_DEVICE_UUID"));

        if (dataMap.containsKey("A2DP_STATUS")) {
            if (dataMap.get("A2DP_STATUS").equals("CONNECTED")
                    && dataMap.get("A2DP_DEVICE") != null && dataMap.get("A2DP_DEVICE_UUID") != null) {

                Log.d("atul in post events", "connectionStatus:  " + dataMap.get("A2DP_STATUS") +
                        "deviceName:  " + dataMap.get("A2DP_DEVICE") + "deviceUUId:  " + dataMap.get("A2DP_DEVICE_UUID"));

                BusEventProgressUpdate.SendFirmwareProgressEvents busEventsGetA2DPStatus = new BusEventProgressUpdate.SendFirmwareProgressEvents();
                busEventsGetA2DPStatus.setA2dpStatus(dataMap.get("A2DP_STATUS"));
                busEventsGetA2DPStatus.setA2dpDeviceName(dataMap.get("A2DP_DEVICE"));
                busEventsGetA2DPStatus.setA2dpDeviceUUID(dataMap.get("A2DP_DEVICE_UUID"));
                busEventsGetA2DPStatus.setConnctedDeviceFriendlyName(dataMap.get("FRIENDLY_NAME"));
                EventBus.getDefault().post(busEventsGetA2DPStatus);
            }
        }

        if (dataMap.containsKey("FW_VERSION")) {
            if (dataMap.containsKey("FW_BSL_VERSION")) {
                deviceInfo.setBslInfoBeforeSplit(dataMap.get("FW_BSL_VERSION"));
            }
            deviceInfo.setFwVersion(dataMap.get("FW_VERSION"));
            LibreLogger.d(this, "suma in discoverymanager fw version" + deviceInfo.getFwVersion());
            deviceListener.newDeviceFound(deviceInfo);
            LibreLogger.d(this, "suma in notify device event in new device found fw version\n");

        }
//        else if (dataMap.containsKey("BSLPROGRESS")) {
//            if (!dataMap.get("BSLPROGRESS").equals("101")) {
//                deviceInfo.setBslProgressvalue(dataMap.get("BSLPROGRESS"));
//                isFwFoundProgress = false;
//                LibreLogger.d(this, "suma in discoverymanager bsl progress value" + deviceInfo.getBslProgressvalue());
//                deviceListener.checkFirmwareInfo(deviceInfo);
//            }
//            LibreLogger.d(this, "suma in discoverymanager bslprogress" + deviceInfo.getBslProgressvalue());
//        } else {
//            if (dataMap.containsKey("FWPROGRESS")) {
//                if (!dataMap.get("FWPROGRESS").equals("101")) {
//                    deviceInfo.setFwProgressValue(dataMap.get("FWPROGRESS"));
//                    LibreLogger.d(this, "suma in discoverymanager fw progress value" + deviceInfo.getFwProgressValue());
//                    isFwFoundProgress = false;
//                    deviceListener.checkFirmwareInfo(deviceInfo);
//                }
//            }
        // deviceListener.newDeviceFound(deviceInfo);


        if (dataMap.containsKey("BSLPROGRESS")) {

            if (!dataMap.get("BSLPROGRESS").equals("101")) {

                deviceInfo.setBslProgressvalue(dataMap.get("BSLPROGRESS"));

                LibreLogger.d(this, "atul in bslprogress" + deviceInfo.getBslProgressvalue() + "\n" + deviceInfo.getFriendlyName());

                if (EventBus.getDefault() != null) {

                    BusEventProgressUpdate.SendFirmwareProgressEvents events = new BusEventProgressUpdate.SendFirmwareProgressEvents();

                    LibreLogger.d(this, "atul in bslprogress" + deviceInfo.getBslProgressvalue() + "\n" + deviceInfo.getFriendlyName());

                    //sending bsl progress value
                    events.sendBLSProgressValeu(deviceInfo.getBslProgressvalue());
                    events.setDeviceIp(deviceInfo.getIpAddress());
                    EventBus.getDefault().post(events);

                }

                deviceListener.checkFirmwareInfo(deviceInfo);
            }
        } else if (dataMap.containsKey("FWPROGRESS")) {

            if (!dataMap.get("FWPROGRESS").equals("101")) {

                deviceInfo.setFwProgressValue(dataMap.get("FWPROGRESS"));

                if (EventBus.getDefault() != null) {
                    //sending fw progress value
                    BusEventProgressUpdate.SendFirmwareProgressEvents events = new BusEventProgressUpdate.SendFirmwareProgressEvents();

                    events.sendOTAFwProgressValue(deviceInfo.getFwProgressValue());
                    events.setDeviceIp(deviceInfo.getIpAddress());

                    EventBus.getDefault().post(events);

                }
                LibreLogger.d(this, "atul in fwprogress" + deviceInfo.getFwProgressValue() + "\n" + deviceInfo.getFriendlyName());

                deviceListener.checkFirmwareInfo(deviceInfo);
            }
        }
//        else{
//            deviceListener.newDeviceFound(deviceInfo);
//            LibreLogger.d(this,"suma in notify device event in new device found\n");
//        }

    }
}
