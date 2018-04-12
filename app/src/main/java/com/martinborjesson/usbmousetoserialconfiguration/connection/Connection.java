package com.martinborjesson.usbmousetoserialconfiguration.connection;

import android.support.annotation.NonNull;

import com.martinborjesson.usbmousetoserialconfiguration.data.Preset;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Connection implements Serializable, Comparable<Connection> {
    private String name;
    private URL url;
    private List<Preset> presets = new ArrayList<>();
    static final long serialVersionUID = 5998345903450L;

    public Connection() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setURL(URL url) {
        this.url = url;
    }

    public URL getURL() {
        return url;
    }

    public void addPreset(Preset preset) {
        presets.add(preset);
    }

    public void removePreset(Preset preset) {
        presets.remove(preset);
    }

    public List<Preset> getPresets() {
        return new ArrayList<>(presets);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[] { name, url });
    }

    @Override
    public int compareTo(@NonNull Connection o) {
        return name.compareTo(o.name);
    }
}
