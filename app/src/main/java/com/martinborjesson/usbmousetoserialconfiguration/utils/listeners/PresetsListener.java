package com.martinborjesson.usbmousetoserialconfiguration.utils.listeners;

import com.martinborjesson.usbmousetoserialconfiguration.data.Preset;

import java.util.Collection;

public interface PresetsListener {
    public void onSetPresets(Collection<Preset> presets);
}
