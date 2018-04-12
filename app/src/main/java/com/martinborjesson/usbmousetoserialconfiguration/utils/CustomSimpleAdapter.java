package com.martinborjesson.usbmousetoserialconfiguration.utils;

import android.content.Context;
import android.widget.SimpleAdapter;

import java.util.List;
import java.util.Map;

public class CustomSimpleAdapter extends SimpleAdapter {

    public CustomSimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        Map<String, ?> map = (Map<String, ?>) getItem(position);
        return (Boolean)map.get("enabled");
    }
}
