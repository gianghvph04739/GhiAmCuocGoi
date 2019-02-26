package com.t440s.call.activities;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.axet.androidlibrary.services.StorageProvider;
import com.github.axet.androidlibrary.widgets.AppCompatThemeActivity;
import com.github.axet.androidlibrary.widgets.ErrorDialog;
import com.github.axet.androidlibrary.widgets.OptimizationPreferenceCompat;
import com.t440s.call.R;
import com.t440s.call.app.CallApplication;
import com.t440s.call.app.DeviceAdminDemo;
import com.t440s.call.app.MixerPaths;
import com.t440s.call.app.Recordings;
import com.t440s.call.app.Storage;
import com.t440s.call.services.RecordingService;
import com.t440s.call.widgets.MixerPathsPreferenceCompat;
import com.t440s.call.widgets.SharedPreferencesManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatThemeActivity implements SharedPreferences.OnSharedPreferenceChangeListener, NavigationView.OnNavigationItemSelectedListener {
    public final static String TAG = MainActivity.class.getSimpleName();

    public static String SHOW_PROGRESS = MainActivity.class.getCanonicalName() + ".SHOW_PROGRESS";
    public static String SET_PROGRESS = MainActivity.class.getCanonicalName() + ".SET_PROGRESS";
    public static String SHOW_LAST = MainActivity.class.getCanonicalName() + ".SHOW_LAST";
    public static final int RESULT_CALL = 1;

    public static final String[] PERMISSIONS = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
    };

    public static final String[] MUST = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
    };
    boolean show;
    Boolean recording;
    int encoding;
    String phone;
    long sec;
    MenuItem resumeCall;
    Recordings recordings;
    Storage storage;
    ListView list;
    Handler handler = new Handler();

    @BindView(R.id.nav_switch_record)
    Switch nav_record;
    @BindView(R.id.nav_lockpass)
    Switch nav_lockpass;
    @BindView(R.id.nav_privacy)
    TextView nav_privacy;
    @BindView(R.id.nav_info)
    TextView nav_info;
    @BindView(R.id.nav_exit)
    TextView nav_exit;

    MenuItem call;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String a = intent.getAction();
            if (a.equals(SHOW_PROGRESS)) {
                encoding = -1;
                show = intent.getBooleanExtra("show", false);
                recording = (Boolean) intent.getExtras().get("recording");
                sec = intent.getLongExtra("sec", 0);
                phone = intent.getStringExtra("phone");
            }
            if (a.equals(SET_PROGRESS)) {
                encoding = intent.getIntExtra("set", 0);
            }
            if (a.equals(SHOW_LAST)) {
                last();
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        PackageManager m = getPackageManager();
        String s = getPackageName();
        PackageInfo p = null;
        try {
            p = m.getPackageInfo(s, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        s = p.applicationInfo.dataDir;
        Log.e("Folder", s);


        if (OptimizationPreferenceCompat.needKillWarning(this, CallApplication.PREFERENCE_NEXT))
            OptimizationPreferenceCompat.buildKilledWarning(this, true, CallApplication.PREFERENCE_OPTIMIZATION).show();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        list = (ListView) findViewById(R.id.list);
        View empty = findViewById(R.id.empty_list);
        list.setEmptyView(empty);

        storage = new Storage(this);

        IntentFilter ff = new IntentFilter();
        ff.addAction(SHOW_PROGRESS);
        ff.addAction(SET_PROGRESS);
        ff.addAction(SHOW_LAST);
        registerReceiver(receiver, ff);

        recordings = new Recordings(this, list);
        list.setAdapter(recordings);
        recordings.setToolbar((ViewGroup) findViewById(R.id.recording_toolbar));

        RecordingService.startIfEnabled(this);

        final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
        if (shared.getBoolean("warning", true)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(R.layout.warning);
            builder.setCancelable(false);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor edit = shared.edit();
                    edit.putBoolean("warning", false);
                    edit.commit();
                }
            });
            final AlertDialog d = builder.create();
            d.setOnShowListener(new DialogInterface.OnShowListener() {
                Button b;
                SwitchCompat sw1, sw2, sw3, sw4;

                @Override
                public void onShow(DialogInterface dialog) {
                    b = d.getButton(DialogInterface.BUTTON_POSITIVE);
                    b.setEnabled(false);
                    Window w = d.getWindow();
                    sw1 = (SwitchCompat) w.findViewById(R.id.recording);
                    sw1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked)
                                sw1.setClickable(false);
                            update();
                        }
                    });
                    sw2 = (SwitchCompat) w.findViewById(R.id.quality);
                    sw2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked)
                                sw2.setClickable(false);
                            update();
                        }
                    });
                    sw3 = (SwitchCompat) w.findViewById(R.id.taskmanagers);
                    sw3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                sw3.setClickable(false);
                            }
                            update();
                        }
                    });
                    sw4 = (SwitchCompat) w.findViewById(R.id.mixedpaths_switch);
                    final MixerPaths m = new MixerPaths();
                    if (!m.isCompatible() || m.isEnabled()) {
                        View v = w.findViewById(R.id.mixedpaths);
                        v.setVisibility(View.GONE);
                        sw4.setChecked(true);
                    } else {
                        sw4.setChecked(m.isEnabled());
                        sw4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked)
                                    sw4.setClickable(false);
                                m.load();
                                if (isChecked && !m.isEnabled())
                                    MixerPathsPreferenceCompat.show(MainActivity.this);
                                update();
                            }
                        });
                    }
                }

                void update() {
                    b.setEnabled(sw1.isChecked() && sw2.isChecked() && sw3.isChecked() && sw4.isChecked());
                }
            });
            d.show();
        }
        changeSwitch();
    }

    @OnClick(R.id.nav_exit)
    void exit() {
        System.exit(0);
        finish();
    }

    @OnClick(R.id.nav_info)
    void info() {
        Toast.makeText(this, "info", Toast.LENGTH_SHORT).show();
        closeDrawer();
    }

    @OnClick(R.id.nav_privacy)
    void privacy() {
        Toast.makeText(this, "Privacy", Toast.LENGTH_SHORT).show();
        closeDrawer();
    }

    @OnClick(R.id.nav_setting)
    void setting() {
        startActivity(new Intent(this, SettingsActivity.class));
        closeDrawer();
    }

    public void changeSwitch() {
        boolean b = RecordingService.isEnabled(MainActivity.this);
        boolean islock = SharedPreferencesManager.isLocked(MainActivity.this);
        nav_record.setChecked(b);
        nav_lockpass.setChecked(islock);
        nav_lockpass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked == false) {
                    SharedPreferencesManager.setLocked(MainActivity.this, false);
                } else {
                    startActivity(new Intent(MainActivity.this, SetupPasswordActivity.class));
                }
            }
        });

        nav_record.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked == true && !Storage.permitted(MainActivity.this, PERMISSIONS, RESULT_CALL)) {
//                    Log.e("Here:", "onCheckedChanged");
//                    Storage.permitted(MainActivity.this, PERMISSIONS, RESULT_CALL);
//                }
//                onOptionsItemSelected(call);

                call.setChecked(!call.isChecked());
                if (call.isChecked() && !Storage.permitted(MainActivity.this, PERMISSIONS, RESULT_CALL)) {
                    resumeCall = call;
                }
                call(call.isChecked());

            }
        });
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
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);

        call = menu.findItem(R.id.action_call);
        boolean b = RecordingService.isEnabled(this);
        call.setChecked(b);


        MenuItem m = menu.findItem(R.id.action_show_folder);
        Intent ii = StorageProvider.openFolderIntent(this, storage.getStoragePath());
        m.setIntent(ii);
        if (!StorageProvider.isFolderCallable(this, ii, StorageProvider.getProvider().getAuthority()))
            m.setVisible(false);

        MenuItem search = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                recordings.search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                recordings.search(newText);
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                recordings.searchClose();
                recordings.notifyDataSetChanged();
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        if (id == R.id.action_call) {
            item.setChecked(!item.isChecked());
            if (item.isChecked() && !Storage.permitted(MainActivity.this, PERMISSIONS, RESULT_CALL)) {
                resumeCall = item;
                return true;
            }
            call(item.isChecked());
            return true;
        }

        if (id == R.id.action_show_folder) {
            Intent intent = item.getIntent();
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void call(boolean b) {
        final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = shared.edit();
        edit.putBoolean(CallApplication.PREFERENCE_CALL, b);
        edit.commit();
        if (b) {
            RecordingService.startService(MainActivity.this);
            Toast.makeText(this, R.string.recording_enabled, Toast.LENGTH_SHORT).show();
        } else {
            RecordingService.stopService(this);
            Toast.makeText(MainActivity.this, R.string.recording_disabled, Toast.LENGTH_SHORT).show();
        }
    }

    void closeDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    protected void onPause() {
        super.onPause();
        invalidateOptionsMenu();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        invalidateOptionsMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        invalidateOptionsMenu();
        try {
            storage.migrateLocalStorage();
        } catch (RuntimeException e) {
            ErrorDialog.Error(this, e);
        }

        Runnable done = new Runnable() {
            @Override
            public void run() {
                recordings.progressText.setVisibility(View.VISIBLE);
                recordings.progressEmpty.setVisibility(View.GONE);
            }
        };
        recordings.progressText.setVisibility(View.GONE);
        recordings.progressEmpty.setVisibility(View.VISIBLE);

        recordings.load(false, done);

        updateHeader();
    }

    public static void showProgress(Context context, boolean show, String phone, long sec, Boolean recording) {
        Intent intent = new Intent(SHOW_PROGRESS);
        intent.putExtra("show", show);
        intent.putExtra("recording", recording);
        intent.putExtra("sec", sec);
        intent.putExtra("phone", phone);
        context.sendBroadcast(intent);
    }

    public static void setProgress(Context context, int p) {
        Intent intent = new Intent(SET_PROGRESS);
        intent.putExtra("set", p);
        context.sendBroadcast(intent);
    }

    public static void last(Context context) {
        Intent intent = new Intent(SHOW_LAST);
        context.sendBroadcast(intent);
    }

    public static void startActivity(Context context) {
        Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(i);
    }

    public static void setSolid(Drawable background, int color) {
        if (background instanceof ShapeDrawable) {
            ShapeDrawable shapeDrawable = (ShapeDrawable) background;
            shapeDrawable.getPaint().setColor(color);
        } else if (background instanceof GradientDrawable) {
            GradientDrawable gradientDrawable = (GradientDrawable) background;
            gradientDrawable.setColor(color);
        } else if (background instanceof ColorDrawable) {
            ColorDrawable colorDrawable = (ColorDrawable) background;
            if (Build.VERSION.SDK_INT >= 11)
                colorDrawable.setColor(color);
        }
    }

    public static String join(String... args) {
        StringBuilder bb = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (bb.length() != 0)
                bb.append(args[0]);
            bb.append(args[i]);
        }
        return bb.toString();
    }

    @Override
    public int getAppTheme() {
        return CallApplication.getTheme(this, R.style.RecThemeLight_NoActionBar, R.style.RecThemeDark_NoActionBar);
    }


    void last() {
        Runnable done = new Runnable() {
            @Override
            public void run() {
                final int selected = getLastRecording();
                recordings.progressText.setVisibility(View.VISIBLE);
                recordings.progressEmpty.setVisibility(View.GONE);
                if (selected != -1) {
                    recordings.select(selected);
                    list.smoothScrollToPosition(selected);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            list.setSelection(selected);
                        }
                    });
                }
            }
        };
        recordings.progressText.setVisibility(View.GONE);
        recordings.progressEmpty.setVisibility(View.VISIBLE);
        recordings.load(false, done);
    }

    int getLastRecording() {
        final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
        String last = shared.getString(CallApplication.PREFERENCE_LAST, "");
        last = last.toLowerCase();
        for (int i = 0; i < recordings.getCount(); i++) {
            Storage.RecordingUri f = recordings.getItem(i);
            String n = Storage.getName(this, f.uri).toLowerCase();
            if (n.equals(last)) {
                SharedPreferences.Editor edit = shared.edit();
                edit.putString(CallApplication.PREFERENCE_LAST, "");
                edit.commit();
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RESULT_CALL:
                if (Storage.permitted(this, MUST)) {
                    try {
                        storage.migrateLocalStorage();
                    } catch (RuntimeException e) {
                        ErrorDialog.Error(this, e);
                    }
                    recordings.load(false, null);
                    if (resumeCall != null) {
                        call(call.isChecked());
                        resumeCall = null;
                    }
                }
                else {
                    if (!Storage.permitted(this, MUST)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Permissions");
                        builder.setMessage("Call permissions must be enabled manually");
                        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Storage.showPermissions(MainActivity.this);
                            }
                        });
                        builder.show();
                        resumeCall = null;
                    }
                }
        }
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        handler.post(new Runnable() {
            @Override
            public void run() {
                list.smoothScrollToPosition(recordings.getSelected());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recordings.close();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    void updateHeader() {
        Uri f = storage.getStoragePath();
        long free = Storage.getFree(this, f);
        long sec = Storage.average(this, free);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(CallApplication.PREFERENCE_STORAGE)) {
            recordings.load(true, null);
        }
    }
}
