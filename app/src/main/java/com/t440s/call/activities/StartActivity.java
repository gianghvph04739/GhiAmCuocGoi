package com.t440s.call.activities;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.t440s.call.R;
import com.t440s.call.app.DeviceAdminDemo;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_CODE = 0;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;
    private Button mBtnCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        initView();

        try {
            // Initiate DevicePolicyManager.
            mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            mAdminName = new ComponentName(this, DeviceAdminDemo.class);

            if (!mDPM.isAdminActive(mAdminName)) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Click on Activate button to secure your application.");
                startActivityForResult(intent, REQUEST_CODE);
            } else {
                // mDPM.lockNow();
                // Intent intent = new Intent(MainActivity.this,
                // TrackDeviceService.class);
                // startService(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (REQUEST_CODE == requestCode) {
            startActivity(new Intent(StartActivity.this, MainActivity.class));
            finish();
        }
    }

    private void initView() {
        mBtnCheck = (Button) findViewById(R.id.btnCheck);
        mBtnCheck.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCheck:
                // TODO 19/02/26
                startActivity(new Intent(StartActivity.this, MainActivity.class));
                finish();
                break;
            default:
                break;
        }
    }
}
