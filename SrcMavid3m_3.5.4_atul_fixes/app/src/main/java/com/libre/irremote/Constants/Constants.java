package com.libre.irremote.Constants;

/**
 * Created by bhargav on 6/2/18.
 */

public class Constants {
    public interface ACTION {
        String MAIN_ACTION = "com.libre.Scanning.action.main";
        String PREV_ACTION = "com.libre.Scanning.action.prev";
        String PLAY_ACTION = "com.libre.Scanning.action.play";
        String NEXT_ACTION = "com.libre.Scanning.action.next";
        String STARTFOREGROUND_ACTION = "com.libre.Scanning.action.startforeground";
        String STOPFOREGROUND_ACTION = "com.libre.Scanning.action.stopforeground";
    }

    public static String WAC_SSID = "LSConfigure";
    public static String WAC_SSID2 = "MicrodotConfigure";
    public static String WAC_SSID3 = "WaveConfigure";
    public static String WAC_SSID4 = "WAVEConfigure";
    public static String WAC_SSID5 = "SmartIR";

    public static String FAIL_SAFE_WAC_SSID = "MAVID_SAC_AP";

    public static String BASEURL = "https://open.eu.librewireless.com";

    public static String BASEURL2 = "http://3.227.163.55:81";

    public String appliancesType;

    public String makeType;


    public interface INTENTS {
        String SCAN_LIST = "_scanList";
        String SAC_PARAMS = "_sacParams";
        String MACADDRESS = "_macAddress";
        String IP_ADDRESS = "_ipaddress";
        String BT_SOURCE_SCANLIST = "btSourceScanList";
        String ALEXA_LOGIN_STATUS = "_alexaLoginStatus";

    }

    public interface RestApis {
        String EMAIL_VERIFICATION = "/v1/account/app/emailcode/";
        String SIGN_UP = "/v1/account/app/signup/";
        String LOGIN = "/v1/oauth/app/token/";
        String REFRESH_TOKEN = "/v1/oauth/app/token/";
        String LOGOUT = "/v1/oauth/app/logout/";

        String APPLIANCESLIST = "/getdata?appliances=";

        String MANUFACTURESLIST = "http://3.227.163.55:81/getdata?appliances=";


        String ADD_DEVICE_TO_USER = "/v1/deviceanduser/";


    }


    public interface AlexaSkillLinking {

        String BASE_URL = "api.amazonalexa.com";

        String END_POINT_PATH = "/v1/alexaApiEndpoint";


        String SKILL_ID = "amzn1.ask.skill.14aecddf-40bf-4c15-9065-f7aec5ae9831";


        /**
         * will make it live in the future
         */
        String SKILL_STAGE = "development";

        String REDIRECT_URI = "https://127.0.0.1";

    }


    public interface ErrorCodes {
        String SUCCESS = "0";
        String ERR_PARAS_ERROR = "3";
        String ERR_SERVER_ERROR = "4";
        String ERR_ACCOUNT_EMAIL_ALREADY_REGISTERED = "2001";
        String ERR_VERIFY_CODE_WRONG = "2003";
        String ERR_ACCOUNT_NOT_REGISTERED = "2006";
        String ERR_SAVE_USER_TO_DB = "2009";
        String ERR_OAUTH_TYPE_NOT_SUPPORT = "2206";
        String ERR_VERIFY_CODE_TYPE_NOT_SUPPORT = "2207";

    }

    public interface dataIds {
        String CLIENT_SECRET = "2xwSAVbre00bSfE2tCF1YfbJ8YleNT9A8NX1cwLF1HvgA7E8OZWBifVIjgPsDJmx3O2TgrB6mVITLtcNsfjTClxz1y3R6XOeX2d8GbD06VNYYdLhYExAxkCxcPkfAVXz";
        String CLIENT_ID = "Ph5kSIQIQlVoosAASyNin9ZnXYWve5ybdv0m7eIU";
    }


    public static final int HTTP_POST_DONE = 0x01;
    public static final int CONNECTED_TO_MAIN_SSID = 0x02;
    public static final int LSSDP_NEW_NODE_FOUND = 0x03;
    public static final int CONNECTED_SAC_DEVICE = 0x04;

    public static final int HTTP_POST_DONE_SUCCESSFULLY = 12121;
    public static final int HTTP_POST_FAILED = 12133;
    public static int CONNECTED_TO_SAC_DEVICE = 12122;
    public static final int HTTP_POST_SOMETHING_WRONG = 12111;
    public static final int HTTP_POST_SOMETHINGWRONG = 12111;
    public static final int WIFI_CONNECTING_NOTIFICATION = 0x6890;
    public static final int WIFI_CONNECTED_NOTIFICATION = 0x6990;
    public static final int READ_ALEXA_STATUS_IN_INTERVALS = 0x2342;

    public static final int CONNECTED_TO_MAIN_SSID_FAIL = 0x1234;
    public static final int CONNECTED_TO_MAIN_SSID_SUCCESS = 0x1333;
    public static final int TIMEOUT_FOR_SEARCHING_DEVICE = 0x1340;
    public static final int SEARCHING_FOR_DEVICE = 0x1337;
    public static final int CONFIGURED_DEVICE_FOUND = 0x1338;
    public static final int CONNECTED_TO_DIFFERENT_SSID = 0x1339;
    public static final int ALEXA_NEXT_PREV_HANDLER = 121212;
    public static final int ALEXA_NEXT_PREV_TIMEOUT = 3000;
    public static final int ALEXA_CHECK_TIMEOUT = 12169;
    public static final int ALEXA_NEXT_PREV_INIT = 15122;
    public static final int OTA_UPDATE_RESPONSE_NOT_RECEIVED = 0x34234;
    public static final int ZIGBEE_WRITE_RESPONSE_NOT_RECEIVED = 0x34234;

    public static int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 0x1001;
    public static final int HOTSPOT_CONNECTION_TIMEOUT = 0x6790;
    public static final int AUTO_OTA_UPDATE_RESPONSE_NOT_RECEIVED = 0x34234;
    public static final int BSL_UPDATE_RESPONSE_NOT_RECEIVED = 0x34534;
    public static final int FAILED_TOGET_RESPONSE = 0x3453;
    public static final int TIMEOUT_FOR_FW_PROGRESS_UPDATE = 0x3454;


    public static final  int BLE_HOTSPOT_DID_NOT_GET_ANY_RESPONSE =  0x05;

    /**
     * As of now for this release the app compaitiblity is 2
     * Needs to be updated appropriately for each customer release
     *
     * <p>
     * if it is equal == then allow for normal funtionality
     *
     * if App_Compatibility_Version lesser than the device version then --> app needs to be updated
     *
     * if App_Compatibility_Version greater than the app version then --> device needs to be updated
     *
     */
    public static final int App_Compatibility_Version = 2;


}
