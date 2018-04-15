package com.martinborjesson.usbmousetoserialconfiguration.connection;

import com.martinborjesson.usbmousetoserialconfiguration.utils.callbacks.DownloadCallback;
import com.martinborjesson.usbmousetoserialconfiguration.utils.DownloadTask;
import com.martinborjesson.usbmousetoserialconfiguration.data.MouseSettings;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionManager {
    public void connect(Connection connection, ConnectionCallback callback) throws IOException {
        refresh(connection, callback);
    }

    public void reset(Connection connection, ConnectionCallback callback) {
        Map<String, Object> query = new HashMap<>();
        query.put("reset", null);
        download(connection, callback, query);
    }

    public void refresh(Connection connection, ConnectionCallback callback) {
        download(connection, callback,null);
    }

    public void set(Connection connection, MouseSettings settings, ConnectionCallback callback) {
        Map<String, Object> query = null;
        if (settings != null) {
            query = new HashMap<>();
            if (settings.getX() != null) {
                query.put("x", settings.getX());
            }
            if (settings.getY() != null) {
                query.put("y", settings.getY());
            }
            if (settings.getSwap() != null) {
                query.put("swap", settings.getSwap());
            }
        }
        download(connection, callback, query);
    }

    private void download(final Connection connection, final ConnectionCallback callback, Map<String, Object> query) {
        DownloadTask task = new DownloadTask(new DownloadCallback() {
            @Override
            public Object onHandleStream(InputStream stream) throws IOException {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                try {
                    String s = null;
                    StringBuilder sb = new StringBuilder();
                    while ((s = reader.readLine()) != null) {
                        sb.append(s);
                    }
                    try {
                        JSONObject json = new JSONObject(sb.toString());
                        return json;
                    } catch (JSONException e) {
                        throw new IOException(e);
                    }
                } finally {
                    reader.close();
                }
            }

            @Override
            public void onDownloadCompleted(Object data) {
                JSONObject json = (JSONObject) data;
                if (json != null) {
                    MouseSettings s = new MouseSettings();
                    try {
                        s.setPower(json.getBoolean("power"));
                        s.setVersion(json.getString("version"));

                        JSONObject info = json.optJSONObject("info");
                        if (info != null) {
                            s.setMouse(info.optString("mouse", null));
                            s.setProtocol(info.optString("protocol", null));
                        }

                        JSONObject settings = json.optJSONObject("settings");
                        if (settings != null) {
                            s.setX(settings.has("x") ? settings.getDouble("x") : null);
                            s.setY(settings.has("y") ? settings.getDouble("y") : null);
                            s.setSwap(settings.has("swap") ? settings.getBoolean("swap") : null);

                            s.setRate(settings.has("rate") ? settings.getDouble("rate") : null);
                        }
                        // update mouse settings
                        callback.onConnectionSuccess(s);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onDownloadFailed(String message) {
                callback.onConnectionFailed(message);
            }

            @Override
            public void onDownloadAfter() {
            }
        });
        URL url = connection.getURL();
        if (query != null) {
            try {
                URI uri = url.toURI();
                StringBuilder q = new StringBuilder();
                List<Map.Entry<String, Object>> entries = new ArrayList<>(query.entrySet());
                for (int i = 0; i < entries.size(); ++i) {
                    Map.Entry<String, Object> entry = entries.get(i);
                    q.append(entry.getKey());
                    if (entry.getValue() != null) {
                        q.append('=').append(entry.getValue());
                    }
                    if (i < entries.size() - 1) {
                        q.append('&');
                    }
                }
                String newQuery = uri.getQuery();
                if (newQuery != null) {
                    newQuery += "&";
                } else {
                    newQuery = "";
                }
                newQuery += q.toString();
                url = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), newQuery, uri.getFragment()).toURL();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        task.execute(url);
    }


}
