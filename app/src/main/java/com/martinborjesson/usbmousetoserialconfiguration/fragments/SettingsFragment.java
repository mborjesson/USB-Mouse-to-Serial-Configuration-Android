package com.martinborjesson.usbmousetoserialconfiguration.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
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
import com.martinborjesson.usbmousetoserialconfiguration.R;
import com.martinborjesson.usbmousetoserialconfiguration.connection.ConnectionListener;
import com.martinborjesson.usbmousetoserialconfiguration.utils.listeners.SettingsListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SettingsFragment extends Fragment implements ConnectionListener {
    private final List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
    private CustomSimpleAdapter adapter = null;
    private SwipeRefreshLayout swipeRefreshLayout = null;
    private FloatingActionButton resetButton = null;

    private SettingsListener listener;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
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

    @Override
    public void onSetMouseSettings(MouseSettings settings) {
        data.clear();

        Map<String, Object> xmul = new HashMap<>();
        xmul.put("title", getString(R.string.settings_x_title));
        xmul.put("message", getString(R.string.settings_multiplier_message));
        if (settings.getX() != null) {
            xmul.put("value", settings.getX());
            xmul.put("enabled", true);
        } else {
            xmul.put("value", getString(R.string.unknown));
            xmul.put("enabled", false);
        }
        xmul.put("key", "x");
        xmul.put("type", "number");
        data.add(xmul);

        Map<String, Object> ymul = new HashMap<>();
        ymul.put("title", getString(R.string.settings_y_title));
        ymul.put("message", getString(R.string.settings_multiplier_message));
        if (settings.getY() != null) {
            ymul.put("value", settings.getY());
            ymul.put("enabled", true);
        } else {
            ymul.put("value", getString(R.string.unknown));
            ymul.put("enabled", false);
        }
        ymul.put("key", "y");
        ymul.put("type", "number");
        data.add(ymul);

        Map<String, Object> swap = new HashMap<>();
        swap.put("title", getString(R.string.settings_swap_title));
        if (settings.getSwap() != null) {
            swap.put("value", settings.getSwap().booleanValue() ? getString(R.string.yes) : getString(R.string.no));
            swap.put("enabled", true);
        } else {
            swap.put("value", getString(R.string.unknown));
            swap.put("enabled", false);
        }
        swap.put("key", "swap");
        swap.put("type", "boolean");
        data.add(swap);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (resetButton != null) {
            resetButton.setEnabled(settings.isPower());
        }
    }

    @Override
    public void onConnectionError(String message) {
    }

    public void reset() {
        listener.onResetMouseSettings(new Callback() {
            @Override
            public void onSuccess(Object data) {
                MouseSettings s = (MouseSettings) data;
                if (s.isPower()) {
                    Toast.makeText(getActivity(), R.string.message_settings_reset_to_default, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {

            }
        });
    }

    public void refresh() {
        listener.onRefreshMouseSettings(null);
    }

    public void update(MouseSettings settings) {
        listener.onSetMouseSettings(settings, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.settings_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        swipeRefreshLayout.setRefreshing(listener.isRefreshing());

        final ListView list = (ListView) view.findViewById(R.id.settings_list);

        adapter = new CustomSimpleAdapter(getActivity(), data,
                android.R.layout.simple_list_item_2,
                new String[] {"title", "value"},
                new int[] {android.R.id.text1,
                        android.R.id.text2});
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Map<String, Object> d = data.get(position);
                final String type = (String)d.get("type");
                final String key = (String)d.get("key");

                if (type != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle((String)d.get("title"));
                    if (d.containsKey("message")) {
                        builder.setMessage((String)d.get("message"));
                    }

                    if ("number".equalsIgnoreCase(type)) {
                        final EditText input = new EditText(getActivity());
                        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                        input.setRawInputType(Configuration.KEYBOARD_12KEY);
                        input.setText(String.valueOf(d.get("value")));
                        input.setSelection(input.getText().length());
                        builder.setView(input);

                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MouseSettings s = new MouseSettings();
                                Double value = Double.valueOf(input.getText().toString());
                                if ("x".equalsIgnoreCase(key)) {
                                    s.setX(value);
                                } else if ("y".equalsIgnoreCase(key)) {
                                    s.setY(value);
                                }
                                update(s);
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
                                    button.setEnabled(input.getText().length() != 0);

                                    input.addTextChangedListener(new TextWatcher() {
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

                    } else if ("boolean".equalsIgnoreCase(type)) {

                        final boolean[] checked = {((String)d.get("value")).equalsIgnoreCase("yes")};
                        builder.setMultiChoiceItems(new String[]{"Enable"}, new boolean[]{checked[0]}, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                checked[0] = isChecked;
                            }
                        });

                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MouseSettings s = new MouseSettings().setSwap(checked[0]);
                                update(s);
                            }
                        });
                        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                        dialog.show();
                    }
                }
            }
        });

        resetButton = (FloatingActionButton) view.findViewById(R.id.settings_reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });

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
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
