package com.t440s.call.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.t440s.call.R;
import com.t440s.call.customview.CustomImageView;
import com.t440s.call.widgets.SharedPreferencesManager;

public class SetupPasswordActivity extends AppCompatActivity {
    private CustomImageView mNumber1;
    private ImageView m1Pass;
    private ImageView m2Pass;
    private ImageView m3Pass;
    private ImageView m4Pass;
    private CustomImageView mNumber2;
    private CustomImageView mNumber3;
    private CustomImageView mNumber4;
    private CustomImageView mNumber5;
    private CustomImageView mNumber6;
    private CustomImageView mNumber7;
    private CustomImageView mNumber8;
    private CustomImageView mNumber9;
    private CustomImageView mNumber0;
    private TextView mDelete;
    private String pass = "";
    private int touchCount = 0;
    private String pass1 = "";
    private String pass2 = "";
    private int setUpCount = 0;

    private SharedPreferencesManager sharePref;
    private TextView mStatusTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locked);
        initView();
        getSupportActionBar().hide();

        mStatusTv.setText("Mật khẩu mới");
        mDelete.setText("Đặt lại");

        mNumber0.setOnItemClickedListener(new CustomImageView.onItemClickedListener() {
            @Override
            public void getText(String text) {
                touchNumber(text);
            }
        });

        mNumber1.setOnItemClickedListener(new CustomImageView.onItemClickedListener() {
            @Override
            public void getText(String text) {
                touchNumber(text);
            }
        });

        mNumber2.setOnItemClickedListener(new CustomImageView.onItemClickedListener() {
            @Override
            public void getText(String text) {
                touchNumber(text);
            }
        });

        mNumber3.setOnItemClickedListener(new CustomImageView.onItemClickedListener() {
            @Override
            public void getText(String text) {
                touchNumber(text);
            }
        });

        mNumber4.setOnItemClickedListener(new CustomImageView.onItemClickedListener() {
            @Override
            public void getText(String text) {
                touchNumber(text);
            }
        });

        mNumber5.setOnItemClickedListener(new CustomImageView.onItemClickedListener() {
            @Override
            public void getText(String text) {
                touchNumber(text);
            }
        });

        mNumber6.setOnItemClickedListener(new CustomImageView.onItemClickedListener() {
            @Override
            public void getText(String text) {
                touchNumber(text);
            }
        });
        mNumber7.setOnItemClickedListener(new CustomImageView.onItemClickedListener() {
            @Override
            public void getText(String text) {
                touchNumber(text);
            }
        });

        mNumber8.setOnItemClickedListener(new CustomImageView.onItemClickedListener() {
            @Override
            public void getText(String text) {
                touchNumber(text);
            }
        });

        mNumber9.setOnItemClickedListener(new CustomImageView.onItemClickedListener() {
            @Override
            public void getText(String text) {
                touchNumber(text);
            }
        });

        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAll();
            }
        });
    }

    public void touchNumber(String text) {
        if (setUpCount == 0) {
            touchCount++;
            pass1 += text;
            if (touchCount == 1)
                m1Pass.setVisibility(View.VISIBLE);
            if (touchCount == 2)
                m2Pass.setVisibility(View.VISIBLE);
            if (touchCount == 3)
                m3Pass.setVisibility(View.VISIBLE);
            if (touchCount == 4) {
                m4Pass.setVisibility(View.VISIBLE);
                touchCount = 0;
                touchCount = 0;
                new CountDownTimer(150, 150) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }
                    @Override
                    public void onFinish() {
                        setUpCount++;
                        delete();
                        mStatusTv.setText("Nhập lại mật khẩu mới");
                    }
                }.start();


            }
        }
        if (setUpCount == 1) {
            touchCount++;
            pass2 += text;
            if (touchCount == 1)
                m1Pass.setVisibility(View.VISIBLE);
            if (touchCount == 2)
                m2Pass.setVisibility(View.VISIBLE);
            if (touchCount == 3)
                m3Pass.setVisibility(View.VISIBLE);
            if (touchCount == 4) {
                touchCount = 0;
                m4Pass.setVisibility(View.VISIBLE);

                new CountDownTimer(150, 150) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }
                    @Override
                    public void onFinish() {
                        if (pass2.equals(pass1)) {
                            SharedPreferencesManager.setPassword(SetupPasswordActivity.this,pass2);
                            SharedPreferencesManager.setLocked(SetupPasswordActivity.this,true);
                            Toast.makeText(SetupPasswordActivity.this, "Đặt mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SetupPasswordActivity.this,LockedActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SetupPasswordActivity.this, "Mật khẩu nhập lại không đúng\nVui lòng thử lại.", Toast.LENGTH_SHORT).show();
                        }
                        deleteAll();
                        mStatusTv.setText("Mật khẩu mới");
                    }
                }.start();
            }
        }
    }

    public void delete() {
        m1Pass.setVisibility(View.GONE);
        m2Pass.setVisibility(View.GONE);
        m3Pass.setVisibility(View.GONE);
        m4Pass.setVisibility(View.GONE);

    }

    public void deleteAll(){
        pass1 = "";
        touchCount = 0;
        setUpCount = 0;
        pass2 = "";
        mStatusTv.setText("Nhập mật khẩu mới");
        delete();
    }

    private void initView() {
        mNumber1 = (CustomImageView) findViewById(R.id.number1);
        m1Pass = (ImageView) findViewById(R.id.pass_1);
        m2Pass = (ImageView) findViewById(R.id.pass_2);
        m3Pass = (ImageView) findViewById(R.id.pass_3);
        m4Pass = (ImageView) findViewById(R.id.pass_4);
        mNumber2 = (CustomImageView) findViewById(R.id.number2);
        mNumber3 = (CustomImageView) findViewById(R.id.number3);
        mNumber4 = (CustomImageView) findViewById(R.id.number4);
        mNumber5 = (CustomImageView) findViewById(R.id.number5);
        mNumber6 = (CustomImageView) findViewById(R.id.number6);
        mNumber7 = (CustomImageView) findViewById(R.id.number7);
        mNumber8 = (CustomImageView) findViewById(R.id.number8);
        mNumber9 = (CustomImageView) findViewById(R.id.number9);
        mNumber0 = (CustomImageView) findViewById(R.id.number0);
        mDelete = (TextView) findViewById(R.id.delete);
        mStatusTv = (TextView) findViewById(R.id.tv_status);
    }

}
