package com.martinborjesson.usbmousetoserialconfiguration.data;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class Preset implements Serializable, Comparable<Preset> {
    static final long serialVersionUID = 86809384590834095L;
    private String name;
    private MouseSettings settings;

    public Preset(String name, MouseSettings settings) {
        this.name = name;
        this.settings = settings;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSettings(MouseSettings settings) {
        this.settings = settings;
    }

    public MouseSettings getSettings() {
        return settings;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(@NonNull Preset o) {
        return name.compareTo(o.name);
    }
}
