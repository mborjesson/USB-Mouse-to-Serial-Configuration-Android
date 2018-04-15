package com.martinborjesson.usbmousetoserialconfiguration.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.martinborjesson.usbmousetoserialconfiguration.utils.callbacks.Callback;
import com.martinborjesson.usbmousetoserialconfiguration.utils.CustomSimpleAdapter;
import com.martinborjesson.usbmousetoserialconfiguration.data.MouseSettings;
import com.martinborjesson.usbmousetoserialconfiguration.R;
import com.martinborjesson.usbmousetoserialconfiguration.connection.ConnectionListener;
import com.martinborjesson.usbmousetoserialconfiguration.utils.listeners.SettingsListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InformationFragment extends Fragment implements ConnectionListener {
    private final List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
    private CustomSimpleAdapter adapter = null;
    private SwipeRefreshLayout swipeRefreshLayout = null;

    private SettingsListener listener;

    public InformationFragment() {
        // Required empty public constructor
    }

    public static InformationFragment newInstance() {
        InformationFragment fragment = new InformationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSetRefreshing(boolean refreshing) {
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing() != refreshing) {
            swipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    private String getProtocolName(String protocol) {
        if ("microsoft".equalsIgnoreCase(protocol)) {
            return getString(R.string.info_protocol_microsoft);
        } else if ("logitech".equalsIgnoreCase(protocol)) {
            return getString(R.string.info_protocol_logitech);
        } else if ("wheel".equalsIgnoreCase(protocol)) {
            return getString(R.string.info_protocol_wheel);
        } else if ("mousesystems".equalsIgnoreCase(protocol)) {
            return getString(R.string.info_protocol_mousesystems);
        }
        return getString(R.string.unknown);
    }

    @Override
    public void onSetMouseSettings(MouseSettings settings) {
        data.clear();

        Map<String, Object> power = new HashMap<>();
        power.put("title", getString(R.string.info_power_title));
        power.put("value", settings.isPower() ? getString(R.string.on) : getString(R.string.off));
        power.put("enabled", true);
        data.add(power);
        Map<String, Object> mouse = new HashMap<>();
        mouse.put("title", getString(R.string.info_mouse_title));
        mouse.put("value", settings.getMouse() != null ? settings.getMouse() : getString(R.string.unknown));
        mouse.put("enabled", true);
        data.add(mouse);
        Map<String, Object> version = new HashMap<>();
        version.put("title", getString(R.string.info_version_title));
        version.put("value", settings.getVersion());
        version.put("enabled", true);
        data.add(version);
        if (settings.getProtocol() != null) {
            Map<String, Object> protocol = new HashMap<>();
            protocol.put("title", getString(R.string.info_protocol_title));
            protocol.put("value", getProtocolName(settings.getProtocol()));
            protocol.put("enabled", true);
            data.add(protocol);
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onConnectionError(String message) {
        data.clear();

        Map<String, Object> power = new HashMap<>();
        power.put("title", getString(R.string.message_connection_error));
        power.put("value", message);
        power.put("enabled", true);
        data.add(power);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void refresh() {
        listener.onRefreshMouseSettings(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_information, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.information_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        swipeRefreshLayout.setRefreshing(listener.isRefreshing());

        final ListView list = (ListView) view.findViewById(R.id.information_list);

        adapter = new CustomSimpleAdapter(getActivity(), data,
                android.R.layout.simple_list_item_2,
                new String[] {"title", "value"},
                new int[] {android.R.id.text1,
                        android.R.id.text2});
        list.setAdapter(adapter);

        MouseSettings settings = listener.getCachedMouseSettings();
        if (settings != null) {
            onSetMouseSettings(settings);
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SettingsListener) {
            listener = (SettingsListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SettingsListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
