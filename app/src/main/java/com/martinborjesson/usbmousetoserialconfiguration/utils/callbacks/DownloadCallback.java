package com.martinborjesson.usbmousetoserialconfiguration.utils.callbacks;

import java.io.IOException;
import java.io.InputStream;

public interface DownloadCallback {
    public Object onHandleStream(InputStream stream) throws IOException;
    public void onDownloadCompleted(Object data);
    public void onDownloadFailed(String message);
    public void onDownloadAfter();
}
