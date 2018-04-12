package com.martinborjesson.usbmousetoserialconfiguration.utils;

import android.os.AsyncTask;

import com.martinborjesson.usbmousetoserialconfiguration.utils.callbacks.DownloadCallback;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask extends AsyncTask<URL, Integer, Boolean> {
    final private DownloadCallback callback;
    private Object data;
    private String error;

    public DownloadTask(DownloadCallback callback) {
        this.callback = callback;
    }

    @Override
    protected Boolean doInBackground(URL... urls) {
        for (URL url : urls) {
            if (url != null) {
                try {
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setConnectTimeout(5000);
                    con.setReadTimeout(5000);
                    con.setRequestMethod("GET");
                    con.connect();
                    InputStream in = con.getInputStream();
                    if (in != null) {
                        data = callback.onHandleStream(in);
                        return true;
                    }
                } catch (IOException e) {
                    error = e.getMessage();
                }
            } else {
                error = "Missing URL";
            }
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (error != null) {
            callback.onDownloadFailed(error);
        } else if (data == null) {
            callback.onDownloadFailed("No data");
        } else {
            callback.onDownloadCompleted(data);
        }
        callback.onDownloadAfter();
    }
}
