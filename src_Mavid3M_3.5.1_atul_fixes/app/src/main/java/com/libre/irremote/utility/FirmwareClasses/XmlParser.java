package com.libre.irremote.utility.FirmwareClasses;

import com.libre.libresdk.Util.LibreLogger;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by suma on 9/9/18.
 */

public class XmlParser {
    String newFirmwareVersion;
    private String fw_version = "";
    private String firmwarepath = "";
    private String bsl_version="";
    private String urlString = null;
    private boolean urlFailure=false;
    private XmlPullParserFactory xmlPullParserFactoryObject;
    public volatile boolean parsingComplete = true;
    public XmlParser() {

    }

    public void setNewFw_versionAvailable(String fw_version) {
        this.fw_version = fw_version;
    }

    public void setFirmwarePath(String firmwarepath) {
        this.firmwarepath = firmwarepath;
    }

   public void setBsl_version(String bsl_version){
        this.bsl_version=bsl_version;
   }
    public String getBsl_Version(){
      return bsl_version;
    }
    public XmlParser(String url) {
        this.urlString = url;
    }

    public String getNewFw_versionAvailable() {
        return fw_version;
    }

    public String getFirmwarePath() {
        return firmwarepath;
    }

    public void parseXmlAndStoreIt(XmlPullParser myParser) {
        int event;
        String text = null;
        try {
            event = myParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String name = myParser.getName();
                switch (event) {
                    case XmlPullParser.START_TAG:
                        break;
                    case XmlPullParser.TEXT:
                        text = myParser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if (name != null) {
                            switch (name) {
                                case "fw_version":
                                    setNewFw_versionAvailable(text);
                                    LibreLogger.d(this, "suma get to know Firmware_version:" + getNewFw_versionAvailable());
                                    break;
                                case "fw_path":
                                    setFirmwarePath(text);
                                    LibreLogger.d(this, "suma get to know Firmware_path:" + getFirmwarePath());
                                    break;
                                case "bsl_version":
                                    setBsl_version(text);
                                    LibreLogger.d(this, "suma get to know Bsl_Firmware_version:" + getBsl_Version());
                                    break;
                            }
                        }
                        break;
                }
                event = myParser.next();
            }
            parsingComplete = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchXml(final DownloadMyXmlListener DownloadListener) {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection connect = (HttpURLConnection) url.openConnection();
                    connect.setReadTimeout(10000);
                    connect.setConnectTimeout(15000);
                    connect.setRequestMethod("GET");
                    connect.setDoInput(true);
                    connect.connect();
                    int responseCode = connect.getResponseCode();
                    LibreLogger.d(this,"suma in xml parser response code"+responseCode);
                    InputStream stream = connect.getInputStream();
                    xmlPullParserFactoryObject = XmlPullParserFactory.newInstance();
                    XmlPullParser myparser = xmlPullParserFactoryObject.newPullParser();
                    myparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    myparser.setInput(stream, null);
                    parseXmlAndStoreIt(myparser);
                    stream.close();
                    DownloadListener.success(getNewFw_versionAvailable(),getBsl_Version());
                    //DownloadListener.success(getBsl_Version());
                } catch (Exception e) {

                    parsingComplete = false;
                    e.printStackTrace();
                    DownloadListener.failure(e);
                    }
            }
        });
        thread.start();
    }
}
