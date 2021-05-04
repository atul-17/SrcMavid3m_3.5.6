package com.libre.irremote.models;

public class ModelAlexaLocalSupported {

    public String Locale;
    public String Language;
    public String Country;

    public boolean isChecked = false;

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public ModelAlexaLocalSupported(String locale, String language, String country) {
        Locale = locale;
        Language = language;
        Country = country;
    }
}
