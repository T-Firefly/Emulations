package com.firefly.emulationstation.settings;

/**
 * Created by rany on 17-11-21.
 */

public class SettingsItem {
    private int cardResId;
    private String name;
    private int xml;
    private String prefsKey;

    public SettingsItem() {
    }

    public SettingsItem(int cardResId, String name, int xml, String prefsKey) {
        this.cardResId = cardResId;
        this.name = name;
        this.xml = xml;
        this.prefsKey = prefsKey;
    }

    public int getCardResId() {
        return cardResId;
    }

    public void setCardResId(int cardResId) {
        this.cardResId = cardResId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getXml() {
        return xml;
    }

    public void setXml(int xml) {
        this.xml = xml;
    }

    public String getPrefsKey() {
        return prefsKey;
    }

    public void setPrefsKey(String prefsKey) {
        this.prefsKey = prefsKey;
    }
}
