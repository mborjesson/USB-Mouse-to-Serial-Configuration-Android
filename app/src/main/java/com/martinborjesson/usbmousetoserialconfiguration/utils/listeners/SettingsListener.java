package com.martinborjesson.usbmousetoserialconfiguration.utils.listeners;

import com.martinborjesson.usbmousetoserialconfiguration.data.MouseSettings;
import com.martinborjesson.usbmousetoserialconfiguration.data.Preset;
import com.martinborjesson.usbmousetoserialconfiguration.utils.callbacks.Callback;

import java.util.Collection;

public interface SettingsListener {
    // mouse settings
    void onSetMouseSettings(MouseSettings settings, Callback callback);
    void onRefreshMouseSettings(Callback callback);
    void onResetMouseSettings(Callback callback);
    MouseSettings getCachedMouseSettings();

    // presets
    void onPresetLoad(Preset preset, Callback callback);
    void onPresetsRefresh();
    void onPresetAdd(Preset preset);
    void onPresetRemove(Preset preset);
    void onPresetUpdate(Preset preset, Callback callback);
    Collection<Preset> getCachedPresets();

    boolean isRefreshing();
}
