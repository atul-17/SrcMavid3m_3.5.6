package com.libre.irremote.models;

import com.intrusoft.sectionedrecyclerview.Section;
import com.libre.irremote.BluetoothActivities.MavidSourceDeviceInfo;

import java.util.List;


public class ModelSectionHeadersBt implements Section<MavidSourceDeviceInfo> {

    private String sectionText;
    private List<MavidSourceDeviceInfo> mavidSourceDeviceInfoList;


    public String getSectionText() {
        return sectionText;
    }

    public ModelSectionHeadersBt(String sectionText, List<MavidSourceDeviceInfo> mavidSourceDeviceInfos) {
        this.sectionText = sectionText;
        this.mavidSourceDeviceInfoList = mavidSourceDeviceInfos;
    }

    public void setSectionText(String sectionText) {
        this.sectionText = sectionText;
    }


    @Override
    public List<MavidSourceDeviceInfo> getChildItems() {
        return mavidSourceDeviceInfoList;
    }

}
