package com.t440s.audio.callrecorder.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.axet.androidlibrary.widgets.ErrorDialog;
import com.t440s.audio.app.MainApplication;
import com.t440s.audio.callrecorder.R;
import com.t440s.audio.callrecorder.activities.PreviewActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.TreeSet;

public class Recordings extends com.t440s.audio.app.Recordings {
    View refresh;
    public TextView progressText;
    public View progressEmpty;

    boolean toolbarFilterIn;
    boolean toolbarFilterOut;

    public Recordings(Context context, ListView list) {
        super(context, list);
        View empty = list.getEmptyView();
        refresh = empty.findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                load(false, null);
            }
        });
        progressText = (TextView) empty.findViewById(android.R.id.text1);
        progressEmpty = empty.findViewById(R.id.progress_empty);
    }

    @Override
    public void load(Uri mount, boolean clean, Runnable done) {
        progressText.setText(R.string.recording_list_is_empty);
        refresh.setVisibility(View.GONE);
        if (!com.t440s.audio.app.Storage.exists(getContext(), mount)) { // folder may not exist, do not show error
            scan(new ArrayList<com.t440s.audio.app.Storage.Node>(), clean, done);
            return;
        }
        try {
            super.load(mount, clean, done);
        } catch (RuntimeException e) {
            Log.e(TAG, "unable to load", e);
            refresh.setVisibility(View.VISIBLE);
            progressText.setText(ErrorDialog.toMessage(e));
            scan(new ArrayList<com.t440s.audio.app.Storage.Node>(), clean, done);
        }
    }

    @Override
    public String[] getEncodingValues() {
        return Storage.getEncodingValues(getContext());
    }

    @Override
    public void cleanDelete(TreeSet<String> delete, Uri f) {
        super.cleanDelete(delete, f);
        String p = CallApplication.getFilePref(f);
        delete.remove(p + CallApplication.PREFERENCE_DETAILS_CONTACT);
        delete.remove(p + CallApplication.PREFERENCE_DETAILS_CALL);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        LinearLayout s = (LinearLayout) v.findViewById(R.id.recording_status);
        ImageView i = (ImageView) v.findViewById(R.id.recording_call);
        final com.t440s.audio.app.Storage.RecordingUri f = getItem(position);
        Storage.RecordingUri u = getItem(position);
        String call = CallApplication.getCall(getContext(), u.uri);
        if (call == null || call.isEmpty()) {
            i.setVisibility(View.GONE);
        } else {
            switch (call) {
                case CallApplication.CALL_IN:
                    i.setVisibility(View.VISIBLE);
                    i.setImageResource(R.drawable.ic_call_received_black_24dp);
                    break;
                case CallApplication.CALL_OUT:
                    i.setVisibility(View.VISIBLE);
                    i.setImageResource(R.drawable.ic_call_made_black_24dp);
                    break;
            }
        }
        final boolean starb = MainApplication.getStar(getContext(), f.uri);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), PreviewActivity.class);
                intent.putExtra("uri", f.uri.toString());
                intent.putExtra("title", f.name);
                intent.putExtra("duration", MainApplication.formatDuration(getContext(), f.duration));
                intent.putExtra("size", MainApplication.formatSize(getContext(), f.size));
                intent.putExtra("last", MainApplication.SIMPLE.format(new Date(f.last)));
                intent.putExtra("star",starb);
                getContext().startActivity(intent);
            }
        });
        return v;
    }

    @Override
    protected boolean filter(Storage.RecordingUri f) {
        boolean include = super.filter(f);
        if (include) {
            if (!toolbarFilterIn && !toolbarFilterOut)
                return true;
            String call = CallApplication.getCall(getContext(), f.uri);
            if (call == null || call.isEmpty())
                return false;
            if (toolbarFilterIn)
                return call.equals(CallApplication.CALL_IN);
            if (toolbarFilterOut)
                return call.equals(CallApplication.CALL_OUT);
        }
        return include;
    }

    public void setToolbar(ViewGroup v) {

        super.setToolbar(v);
    }

    protected void selectToolbar() {
        super.selectToolbar();
    }

    protected void save() {
        super.save();
        final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor edit = shared.edit();
        edit.putBoolean(CallApplication.PREFERENCE_FILTER_IN, toolbarFilterIn);
        edit.putBoolean(CallApplication.PREFERENCE_FILTER_OUT, toolbarFilterOut);
        edit.commit();
    }

    protected void load() {
        super.load();
        final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(getContext());
        toolbarFilterIn = shared.getBoolean(CallApplication.PREFERENCE_FILTER_IN, false);
        toolbarFilterOut = shared.getBoolean(CallApplication.PREFERENCE_FILTER_OUT, false);
    }
}
