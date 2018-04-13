package com.martinborjesson.usbmousetoserialconfiguration;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.martinborjesson.usbmousetoserialconfiguration.connection.Connection;
import com.martinborjesson.usbmousetoserialconfiguration.connection.ConnectionCallback;
import com.martinborjesson.usbmousetoserialconfiguration.connection.ConnectionListener;
import com.martinborjesson.usbmousetoserialconfiguration.connection.ConnectionManager;
import com.martinborjesson.usbmousetoserialconfiguration.data.MouseSettings;
import com.martinborjesson.usbmousetoserialconfiguration.data.Preset;
import com.martinborjesson.usbmousetoserialconfiguration.fragments.InformationFragment;
import com.martinborjesson.usbmousetoserialconfiguration.fragments.PresetsFragment;
import com.martinborjesson.usbmousetoserialconfiguration.fragments.SettingsFragment;
import com.martinborjesson.usbmousetoserialconfiguration.fragments.StartFragment;
import com.martinborjesson.usbmousetoserialconfiguration.utils.callbacks.Callback;
import com.martinborjesson.usbmousetoserialconfiguration.utils.callbacks.CallbackAny;
import com.martinborjesson.usbmousetoserialconfiguration.utils.listeners.PresetsListener;
import com.martinborjesson.usbmousetoserialconfiguration.utils.listeners.SettingsListener;
import com.martinborjesson.usbmousetoserialconfiguration.view.CustomViewPager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SettingsListener, ConnectionCallback, ConnectionListener {

    private final List<Connection> connections = new ArrayList<>();
    private Connection activeConnection = null;
    private ConnectionManager connectionManager = new ConnectionManager();
    private MouseSettings mouseSettings = null;
    private boolean refreshing = false;

    //private boolean power = true;

    private void openConnection(final Connection connection) {
        activeConnection = connection;
        mouseSettings = null;

        final ViewPager viewPager = (ViewPager) findViewById(R.id.navigation_viewpager);
        viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                if (position == 0) {
                    return InformationFragment.newInstance();
                } else if (position == 1) {
                    return SettingsFragment.newInstance();
                } else if (position == 2) {
                    return PresetsFragment.newInstance();
                }
                return null;
            }

            @Override
            public int getCount() {
                return 3;
            }
        });
        getSupportActionBar().setTitle(connection.getName());

        final NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        ((TextView)navView.getHeaderView(0).findViewById(R.id.nav_header_subtitle)).setText(connection.getName());
        invalidateOptionsMenu();

        setViewPagerEnabled(false);

        // refresh all
        onRefreshMouseSettings(null);
        onPresetsRefresh();
    }

    private void closeConnection() {
        activeConnection = null;
        mouseSettings = null;

        final BottomNavigationView bnv = (BottomNavigationView) findViewById(R.id.navigation_buttons);
        for (int i = 0; i < bnv.getMenu().size(); ++i) {
            bnv.getMenu().getItem(i).setEnabled(false);
        }

        final ViewPager viewPager = (ViewPager) findViewById(R.id.navigation_viewpager);
        viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return StartFragment.newInstance();
            }

            @Override
            public int getCount() {
                return 1;
            }
        });
        getSupportActionBar().setTitle(R.string.app_name);
        final NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        ((TextView)navView.getHeaderView(0).findViewById(R.id.nav_header_subtitle)).setText("");
        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); ++i) {
            menu.getItem(i).setEnabled(activeConnection != null);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void loadConnections() {
        File file = new File(getFilesDir(), "connections.dat");
        if (file.exists()) {
            try {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
                try {
                    connections.addAll((List<Connection>) in.readObject());
                } finally {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.message_could_not_load_connections), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void saveConnections() {
        File file = new File(getFilesDir(), "connections.dat");
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            try {
                out.writeObject(connections);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshConnections() {
        Collections.sort(connections);
        final NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        //navView.getHeaderView(0).setVisibility(View.GONE);

        MenuItem connectionMenu = navView.getMenu().findItem(R.id.menu_connections);
        connectionMenu.getSubMenu().clear();
        for (final Connection con : connections) {
            MenuItem mi = connectionMenu.getSubMenu().add(con.getName());
            mi.setIcon(R.drawable.ic_menu_connection);
            mi.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    openConnection(con);

                    return false;
                }
            });
        }
    }

    private void removeConnection(Connection connection) {
        connections.remove(connection);
        saveConnections();
        refreshConnections();
        if (activeConnection == connection) {
            closeConnection();
        }
    }

    private void editConnection(final Connection connection) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(connection != null ? getString(R.string.connection_edit) : getString(R.string.connection_new));

        LinearLayout layout = new LinearLayout(this);

        //Setup Layout Attributes
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(params);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText nameInput = new EditText(this);
        nameInput.setMaxLines(1);
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        nameInput.setHint(getString(R.string.name));
        if (connection != null) {
            nameInput.setText(connection.getName());
            nameInput.setSelection(nameInput.getText().length());
        }
        layout.addView(nameInput);

        final EditText hostInput = new EditText(this);
        hostInput.setMaxLines(1);
        hostInput.setInputType(InputType.TYPE_CLASS_TEXT);
        hostInput.setHint(getString(R.string.connection_hint_host));
        if (connection != null) {
            hostInput.setText(connection.getURL().toString());
            hostInput.setSelection(hostInput.getText().length());
        }
        layout.addView(hostInput);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // does nothing
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setView(layout);

        final AlertDialog alertDialog = builder.create();

        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                if (button != null) {
                    button.setEnabled(nameInput.getText().length() != 0 && hostInput.getText().length() != 0);

                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                URL url = new URL(hostInput.getText().toString());
                                Connection con = connection;
                                if (con == null) {
                                    con = new Connection();
                                    connections.add(con);
                                }
                                con.setName(nameInput.getText().toString());
                                con.setURL(url);
                                saveConnections();
                                refreshConnections();
                                openConnection(con);
                                alertDialog.dismiss();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this, getString(R.string.message_invalid_host).replace("%s", e.getMessage()), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    nameInput.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            button.setEnabled(s.length() != 0 && hostInput.getText().length() != 0);
                        }
                    });
                    hostInput.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            button.setEnabled(s.length() != 0 && nameInput.getText().length() != 0);
                        }
                    });
                }
            }
        });

        alertDialog.show();

    }

    private void setViewPagerEnabled(boolean enabled) {
        final CustomViewPager viewPager = (CustomViewPager) findViewById(R.id.navigation_viewpager);

        if (!enabled) {
            viewPager.setScrollEnabled(false);
            viewPager.setCurrentItem(0, true);

            final BottomNavigationView bnv = (BottomNavigationView) findViewById(R.id.navigation_buttons);
            for (int i = 0; i < bnv.getMenu().size(); ++i) {
                bnv.getMenu().getItem(i).setEnabled(false);
            }
            bnv.getMenu().getItem(0).setChecked(true);
        } else {
            viewPager.setScrollEnabled(true);
            final BottomNavigationView bnv = (BottomNavigationView) findViewById(R.id.navigation_buttons);
            for (int i = 0; i < bnv.getMenu().size(); ++i) {
                bnv.getMenu().getItem(i).setEnabled(true);
            }
        }
    }

    @Override
    public void onSetRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment f : fragments) {
                if (f.getActivity() != null && f.isAdded() && f instanceof ConnectionListener) {
                    ((ConnectionListener) f).onSetRefreshing(refreshing);
                }
            }
        }
    }

    @Override
    public void onSetMouseSettings(MouseSettings settings) {
        //settings.setPower(power);

        if (!settings.isPower()) {
            if (mouseSettings == null || mouseSettings.isPower()) {
                setViewPagerEnabled(false);
            }
        } else {
            if (mouseSettings == null || !mouseSettings.isPower()) {
                setViewPagerEnabled(true);
            }
        }

        mouseSettings = settings;

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment f : fragments) {
                if (f.getActivity() != null && f.isAdded() && f instanceof ConnectionListener) {
                    ((ConnectionListener) f).onSetMouseSettings(settings);
                }
            }
        }
    }

    @Override
    public void onConnectionError(String message) {
        mouseSettings = null;

        setViewPagerEnabled(false);
        //Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment f : fragments) {
                if (f.getActivity() != null && f.isAdded() && f instanceof ConnectionListener) {
                    ((ConnectionListener) f).onConnectionError(message);
                }
            }
        }
    }

    private void setPresets(Collection<Preset> presets) {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment f : fragments) {
                if (f.getActivity() != null && f.isAdded() && f instanceof PresetsListener) {
                    ((PresetsListener) f).onSetPresets(presets);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        try {
            ((TextView)navigationView.getHeaderView(0).findViewById(R.id.nav_header_title)).setText(getString(R.string.app_name) + " " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        final ViewPager viewPager = (ViewPager) findViewById(R.id.navigation_viewpager);

        final BottomNavigationView bnv = (BottomNavigationView) findViewById(R.id.navigation_buttons);
        bnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_information:
                        viewPager.setCurrentItem(0);
                        return true;
                    case R.id.nav_settings:
                        viewPager.setCurrentItem(1);
                        return true;
                    case R.id.nav_presets:
                        viewPager.setCurrentItem(2);
                        return true;
                }
                return false;
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                final BottomNavigationView bnv = (BottomNavigationView) findViewById(R.id.navigation_buttons);
                bnv.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        loadConnections();
        refreshConnections();
        closeConnection();

        if (connections.size() == 1) {
            openConnection(connections.get(0));
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.connection_refresh) {
            onRefreshMouseSettings(null);
            // debug stuff
            //power = !power;
            //System.out.println("Power: " + power);
            return true;
        } else if (id == R.id.connection_remove) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.remove_connection_title).replace("%s", activeConnection.getName()));
            builder.setMessage(getString(R.string.remove_connection_message));
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    removeConnection(activeConnection);
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
            return true;
        } else if (id == R.id.connection_edit) {
            editConnection(activeConnection);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        onRefreshMouseSettings(null);
        super.onResume();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.menu_add_connection) {
            editConnection(null);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPresetLoad(Preset preset, Callback callback) {
        onSetMouseSettings(preset.getSettings(), callback);
    }

    @Override
    public void onPresetsRefresh() {
        if (activeConnection != null) {
            setPresets(activeConnection.getPresets());
        }
    }

    @Override
    public void onPresetAdd(Preset preset) {
        if (activeConnection != null) {
            activeConnection.addPreset(preset);
            saveConnections();
            onPresetsRefresh();
        }
    }

    @Override
    public void onPresetRemove(Preset preset) {
        if (activeConnection != null) {
            activeConnection.removePreset(preset);
            saveConnections();
            onPresetsRefresh();
        }
    }

    @Override
    public void onPresetUpdate(Preset preset, Callback callback) {
        if (activeConnection != null) {
            saveConnections();
            onPresetsRefresh();
            if (callback != null) {
                callback.onSuccess(null);
            }
        }
    }

    @Override
    public Collection<Preset> getCachedPresets() {
        if (activeConnection != null) {
            return activeConnection.getPresets();
        }
        return new ArrayList<>();
    }

    private class ConnectionCallbackWrapper implements ConnectionCallback {
        final private ConnectionCallback source;
        final private Callback[] callbacks;

        public ConnectionCallbackWrapper(ConnectionCallback source, Callback ... callbacks) {
            this.source = source;
            this.callbacks = callbacks;
        }

        @Override
        public void onConnectionSuccess(MouseSettings settings) {
            source.onConnectionSuccess(settings);
            if (callbacks != null) {
                for (Callback callback : callbacks) {
                    if (callback != null) {
                        callback.onSuccess(settings);
                    }
                }
            }
        }

        @Override
        public void onConnectionFailed(String message) {
            source.onConnectionFailed(message);
            if (callbacks != null) {
                for (Callback callback : callbacks) {
                    if (callback != null) {
                        callback.onError(message);
                    }
                }
            }
        }
    }

    @Override
    public void onSetMouseSettings(MouseSettings settings, final Callback callback) {
        if (activeConnection != null) {
            connectionManager.set(activeConnection, settings, new ConnectionCallbackWrapper(this, callback));
        }
    }

    @Override
    public void onRefreshMouseSettings(final Callback callback) {
        if (activeConnection != null) {
            onSetRefreshing(true);
            connectionManager.refresh(activeConnection, new ConnectionCallbackWrapper(this, callback, new CallbackAny() {
                @Override
                public void onAny() {
                    onSetRefreshing(false);
                }
            }));
        }
    }

    @Override
    public void onResetMouseSettings(Callback callback) {
        if (activeConnection != null) {
            connectionManager.reset(activeConnection, new ConnectionCallbackWrapper(this, callback));
        }
    }

    @Override
    public MouseSettings getCachedMouseSettings() {
        return mouseSettings;
    }

    @Override
    public boolean isRefreshing() {
        return refreshing;
    }

    @Override
    public void onConnectionSuccess(MouseSettings settings) {
        onSetMouseSettings(settings);
    }

    @Override
    public void onConnectionFailed(String message) {
        onConnectionError(message);
    }
}
