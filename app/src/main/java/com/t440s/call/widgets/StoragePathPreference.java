package com.t440s.call.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

import com.github.axet.androidlibrary.app.Storage;
import com.github.axet.androidlibrary.widgets.OpenFileDialog;
import com.github.axet.androidlibrary.widgets.OpenStorageChoicer;
import com.github.axet.androidlibrary.widgets.StoragePathPreferenceCompat;

import java.io.File;

public class StoragePathPreference extends EditTextPreference {
    public String def;
    public Storage storage = new Storage(getContext());

    public static String getText(Object o) {
        if (o instanceof com.github.axet.androidlibrary.widgets.StoragePathPreference)
            return ((com.github.axet.androidlibrary.widgets.StoragePathPreference) o).getText();
        if (o instanceof com.github.axet.androidlibrary.widgets.StoragePathPreferenceCompat)
            return ((com.github.axet.androidlibrary.widgets.StoragePathPreferenceCompat) o).getText();
        throw new RuntimeException("unknown class");
    }

    public static String getTitle(Object o) {
        if (o instanceof com.github.axet.androidlibrary.widgets.StoragePathPreference)
            return ((com.github.axet.androidlibrary.widgets.StoragePathPreference) o).getTitle().toString();
        if (o instanceof com.github.axet.androidlibrary.widgets.StoragePathPreferenceCompat)
            return ((com.github.axet.androidlibrary.widgets.StoragePathPreferenceCompat) o).getTitle().toString();
        throw new RuntimeException("unknown class");
    }

    public static boolean callChangeListener(Object o, String name) {
        if (o instanceof com.github.axet.androidlibrary.widgets.StoragePathPreference)
            return ((com.github.axet.androidlibrary.widgets.StoragePathPreference) o).callChangeListener(name);
        if (o instanceof com.github.axet.androidlibrary.widgets.StoragePathPreferenceCompat)
            return ((com.github.axet.androidlibrary.widgets.StoragePathPreferenceCompat) o).callChangeListener(name);
        throw new RuntimeException("unknown class");
    }

    public static void setText(Object o, String name) {
        if (o instanceof com.github.axet.androidlibrary.widgets.StoragePathPreference) {
            ((com.github.axet.androidlibrary.widgets.StoragePathPreference) o).setText(name);
            return;
        }
        if (o instanceof com.github.axet.androidlibrary.widgets.StoragePathPreferenceCompat) {
            ((StoragePathPreferenceCompat) o).setText(name);
            return;
        }
        throw new RuntimeException("unknown class");
    }

    public static String getDefault() {
        File ext = Environment.getExternalStorageDirectory();
        if (ext == null) // Android Studio pref editor
            return "/sdcard";
        return ext.getPath();
    }

    public static String getDefault(Object o) {
        if (o instanceof com.github.axet.androidlibrary.widgets.StoragePathPreference) {
            return ((com.github.axet.androidlibrary.widgets.StoragePathPreference) o).def;
        }
        throw new RuntimeException("unknown class");
    }

    public static String getPath(Object object) {
        String path = getText(object);

        if (path == null || path.isEmpty()) {
            path = getDefault();
        }

        return path;
    }

    public StoragePathPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public StoragePathPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StoragePathPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void showDialog(Bundle state) {
        String f = com.github.axet.androidlibrary.widgets.StoragePathPreference.getPath(this);
        Uri u = storage.getStoragePath(f);
        OpenStorageChoicer choicer = new OpenStorageChoicer(storage, OpenFileDialog.DIALOG_TYPE.FOLDER_DIALOG, false) {
            @Override
            public void onResult(Uri uri) {
                if (callChangeListener(uri.toString())) {
                    setText(uri.toString());
                }
            }
        };
        choicer.def = def;
        choicer.setTitle(getTitle().toString());
        choicer.show(u);
    }

    @Override
    public boolean callChangeListener(Object newValue) {
        updatePath(new File((String) newValue));
        return super.callChangeListener(newValue);
    }

    // load default value for sharedpropertiesmanager, or set it using xml.
    //
    // can't set dynamic values like '/sdcard'? he-he. so that what it for.
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        def = a.getString(index);
        File path = new File(getDefault(), def);
        return path.getPath();
    }

    void updatePath(File path) {
        File summ = storage.getStoragePath(path);
        setSummary(summ.toString());
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        super.onSetInitialValue(restoreValue, defaultValue);
        updatePath(new File(getPath(this)));
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }
}

