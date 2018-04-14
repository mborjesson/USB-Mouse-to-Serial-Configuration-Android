package com.martinborjesson.usbmousetoserialconfiguration.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.martinborjesson.usbmousetoserialconfiguration.utils.callbacks.Callback;
import com.martinborjesson.usbmousetoserialconfiguration.utils.CustomSimpleAdapter;
import com.martinborjesson.usbmousetoserialconfiguration.data.MouseSettings;
import com.martinborjesson.usbmousetoserialconfiguration.data.Preset;
import com.martinborjesson.usbmousetoserialconfiguration.utils.listeners.PresetsListener;
import com.martinborjesson.usbmousetoserialconfiguration.R;
import com.martinborjesson.usbmousetoserialconfiguration.connection.ConnectionListener;
import com.martinborjesson.usbmousetoserialconfiguration.utils.listeners.SettingsListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PresetsFragment extends Fragment implements ConnectionListener, PresetsListener {
    private final List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
    private SwipeRefreshLayout swipeRefreshLayout = null;
    private ListView listView = null;
    private CustomSimpleAdapter adapter = null;
    private FloatingActionButton addButton = null;

    private MouseSettings settings;

    private SettingsListener listener;

    public PresetsFragment() {
        // Required empty public constructor
    }

    public static PresetsFragment newInstance() {
        PresetsFragment fragment = new PresetsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private String toString(MouseSettings settings) {
        List<String> strings = new ArrayList<>();
        if (settings.getX() != null) {
            strings.add(getString(R.string.preset_format_x).replace("%s", String.valueOf(settings.getX())));
        }
        if (settings.getY() != null) {
            strings.add(getString(R.string.preset_format_y).replace("%s", String.valueOf(settings.getY())));
        }
        if (settings.getSwap() != null) {
            strings.add(getString(R.string.preset_format_swap).replace("%s", (settings.getSwap().booleanValue() ? getString(R.string.yes) : getString(R.string.no))));
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < strings.size(); ++i) {
            sb.append(strings.get(i));
            if (i < strings.size()-1) {
                sb.append(", ");
            }
        }

        return sb.toString();
    }

    @Override
    public void onSetPresets(Collection<Preset> presets) {
        data.clear();

        for (Preset preset : presets) {
            Map<String, Object> map = new HashMap<>();
            map.put("title", preset.getName());
            map.put("value", toString(preset.getSettings()));
            map.put("data", preset);
            map.put("enabled", true);
            data.add(map);
        }

        Collections.sort(data, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return String.valueOf(o1.get("title")).compareTo(String.valueOf(o2.get("title")));
            }
        });

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void refresh() {
        listener.onRefreshMouseSettings(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_presets, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.presets_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        swipeRefreshLayout.setRefreshing(listener.isRefreshing());

        listView = (ListView) view.findViewById(R.id.presets_list);

        adapter = new CustomSimpleAdapter(getActivity(), data,
                android.R.layout.simple_list_item_2,
                new String[] {"title", "value"},
                new int[] {android.R.id.text1,
                        android.R.id.text2});
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Preset preset = (Preset) ((Map<?, ?>) parent.getItemAtPosition(position)).get("data");
                if (listener != null) {
                    listener.onPresetLoad(preset, new Callback() {
                        @Override
                        public void onSuccess(Object data) {
                            MouseSettings s = (MouseSettings) data;
                            if (s.isPower()) {
                                Toast.makeText(getActivity(), getString(R.string.preset_loaded).replace("%s", preset.getName()), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(String message) {
                        }
                    });
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Preset preset = (Preset) ((Map<?, ?>) parent.getItemAtPosition(position)).get("data");
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(preset.getName());
                builder.setItems(new String[] { getString(R.string.update), getString(R.string.rename), getString(R.string.remove) }, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            if (settings != null) {
                                preset.setSettings(settings);
                                listener.onPresetUpdate(preset, new Callback() {

                                    @Override
                                    public void onSuccess(Object data) {
                                        Toast.makeText(getActivity(), getString(R.string.preset_updated).replace("%s", preset.getName()), Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onError(String message) {

                                    }
                                });
                            }
                        } else if (which == 1) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle(R.string.preset_rename_title);

                            final EditText nameInput = new EditText(getActivity());
                            nameInput.setMaxLines(1);
                            nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
                            nameInput.setHint(R.string.preset_name);
                            nameInput.setText(preset.getName());
                            nameInput.setSelection(nameInput.getText().length());
                            builder.setView(nameInput);

                            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    preset.setName(nameInput.getText().toString());
                                    listener.onPresetUpdate(preset, null);
                                }
                            });
                            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            final AlertDialog alertDialog = builder.create();
                            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    final Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                    if (button != null) {
                                        button.setEnabled(nameInput.getText().length() != 0);

                                        nameInput.addTextChangedListener(new TextWatcher() {
                                            @Override
                                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                            }

                                            @Override
                                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                                            }

                                            @Override
                                            public void afterTextChanged(Editable s) {
                                                button.setEnabled(s.length() != 0);
                                            }
                                        });
                                    }
                                }
                            });

                            alertDialog.show();

                        } else if (which == 2) {
                            listener.onPresetRemove(preset);
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });

        addButton = (FloatingActionButton) view.findViewById(R.id.presets_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.preset_new_title);
                builder.setMessage(R.string.preset_new_message);

                final EditText nameInput = new EditText(getActivity());
                nameInput.setMaxLines(1);
                nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
                nameInput.setHint(R.string.preset_name);
                builder.setView(nameInput);

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (settings != null) {
                            listener.onPresetAdd(new Preset(nameInput.getText().toString(), settings));
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                final AlertDialog alertDialog = builder.create();
                alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        final Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        if (button != null) {
                            button.setEnabled(nameInput.getText().length() != 0);

                            nameInput.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {

                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                    button.setEnabled(s.length() != 0);
                                }
                            });
                        }
                    }
                });

                alertDialog.show();
            }
        });

        MouseSettings settings = listener.getCachedMouseSettings();
        if (settings != null) {
            onSetMouseSettings(settings);
        }

        Collection<Preset> presets = listener.getCachedPresets();
        if (presets != null) {
            onSetPresets(presets);
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

    @Override
    public void onSetRefreshing(boolean refreshing) {
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing() != refreshing) {
            swipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    public void onSetMouseSettings(MouseSettings settings) {
        this.settings = settings;
        if (addButton != null) {
            addButton.setEnabled(settings.isPower());
        }
        if (listView != null) {
            listView.setEnabled(settings.isPower());
        }
    }

    @Override
    public void onConnectionError(String message) {

    }
}
