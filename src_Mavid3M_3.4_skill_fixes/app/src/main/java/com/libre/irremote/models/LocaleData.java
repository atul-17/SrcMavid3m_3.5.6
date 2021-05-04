package com.libre.irremote.models;

import java.util.ArrayList;

public class LocaleData {
    private String CurrentLocale;
    private ArrayList<String> LocalesList;

    public String getCurrentLocale() {
        return CurrentLocale;
    }

    public void setCurrentLocale(String currentLocale) {
        CurrentLocale = currentLocale;
    }

    public ArrayList<String> getLocalesList() {
        return LocalesList;
    }

    public void setLocalesList(ArrayList<String> localesList) {
        LocalesList = localesList;
    }


}
