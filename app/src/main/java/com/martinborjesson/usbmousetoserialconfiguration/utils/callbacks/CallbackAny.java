package com.martinborjesson.usbmousetoserialconfiguration.utils.callbacks;

abstract public class CallbackAny implements Callback {

    abstract public void onAny();

    @Override
    public void onSuccess(Object data) {
        onAny();
    }

    @Override
    public void onError(String message) {
        onAny();
    }
}
