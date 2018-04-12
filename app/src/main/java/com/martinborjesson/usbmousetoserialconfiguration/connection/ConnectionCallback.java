package com.martinborjesson.usbmousetoserialconfiguration.connection;

import com.martinborjesson.usbmousetoserialconfiguration.data.MouseSettings;

public interface ConnectionCallback {
    public void onConnectionSuccess(MouseSettings settings);
    public void onConnectionFailed(String message);
}
