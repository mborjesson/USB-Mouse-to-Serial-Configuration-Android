package com.martinborjesson.usbmousetoserialconfiguration.connection;

import com.martinborjesson.usbmousetoserialconfiguration.data.MouseSettings;

public interface ConnectionListener {
    public void onSetRefreshing(boolean refreshing);
    public void onSetMouseSettings(MouseSettings settings);
    public void onConnectionError(String message);
}
