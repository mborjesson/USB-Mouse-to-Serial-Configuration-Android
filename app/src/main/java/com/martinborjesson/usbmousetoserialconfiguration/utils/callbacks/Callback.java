package com.martinborjesson.usbmousetoserialconfiguration.utils.callbacks;

public interface Callback {
    public void onSuccess(Object data);
    public void onError(String message);
}
