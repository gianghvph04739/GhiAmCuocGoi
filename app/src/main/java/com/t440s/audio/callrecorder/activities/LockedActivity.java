package com.t440s.audio.callrecorder.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.t440s.audio.callrecorder.R;
import com.t440s.audio.callrecorder.customview.CustomImageView;

public class LockedActivity extends AppCompatActivity {

    private CustomImageView mNumber1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locked);
        initView();
        Toast.makeText(this, mNumber1.getText(), Toast.LENGTH_SHORT).show();
    }

    private void initView() {
        mNumber1 = (CustomImageView) findViewById(R.id.number1);
    }
}
