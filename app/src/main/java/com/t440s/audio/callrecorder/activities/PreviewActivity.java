package com.t440s.audio.callrecorder.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.axet.androidlibrary.app.MediaPlayerCompat;
import com.github.axet.androidlibrary.widgets.ProximityPlayer;
import com.github.axet.androidlibrary.widgets.ProximityShader;
import com.t440s.audio.app.Storage;
import com.t440s.audio.callrecorder.R;

import java.io.IOException;

public class PreviewActivity extends AppCompatActivity {

    private TextView mSaveTv;
    private TextView mEditTv;
    private TextView mDeleteTv;
    private TextView mShareTv;
    private ImageView mAvtImg;
    private TextView mNameTv;
    private TextView mTimeTv;
    private TextView mSizeTv;
    private ImageView mPlayBtn;
    private LinearLayout mPlayer;
    private PhoneStateListener pscl;
    protected ProximityShader proximity;
    boolean isPlaying = false;
    private Uri uri;
    private String last, duration, size, name;
    public MediaPlayer mMediaPlayer;
    private SeekBar mPlayerSb;
    private Handler mSeekbarUpdateHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        initView();

        Intent i = getIntent();
        uri = Uri.parse(i.getStringExtra("uri"));
        last = i.getStringExtra("last");
        duration = i.getStringExtra("duration");
        size = i.getStringExtra("size");
        name = i.getStringExtra("title");

        mNameTv.setText(name);
        mSizeTv.setText(size);
        mTimeTv.setText(duration);


        Toast.makeText(this, i.getStringExtra("uri"), Toast.LENGTH_SHORT).show();

        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPlaying == false) {
                    playerPlay(uri);
                    isPlaying = true;
                    mPlayBtn.setImageResource(R.drawable.ic_media_pause_light);
                } else {
                    playerPause();
                    mPlayBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    isPlaying = false;
                }
            }
        });

        mPlayerSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    mMediaPlayer.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mDeleteTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete();
            }
        });

    }

    private Runnable mUpdateSeekbar = new Runnable() {
        @Override
        public void run() {
            mPlayerSb.setProgress(mMediaPlayer.getCurrentPosition());
            mSeekbarUpdateHandler.postDelayed(this, 50);
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        playerStop();
    }

    private void initView() {
        mSaveTv = (TextView) findViewById(R.id.tv_save);
        mEditTv = (TextView) findViewById(R.id.tv_edit);
        mDeleteTv = (TextView) findViewById(R.id.tv_delete);
        mShareTv = (TextView) findViewById(R.id.tv_share);
        mAvtImg = (ImageView) findViewById(R.id.img_avt);
        mNameTv = (TextView) findViewById(R.id.tv_name);
        mTimeTv = (TextView) findViewById(R.id.tv_time);
        mSizeTv = (TextView) findViewById(R.id.tv_size);
        mPlayBtn = (ImageView) findViewById(R.id.btn_play);
        mPlayer = (LinearLayout) findViewById(R.id.player);
        mPlayerSb = (SeekBar) findViewById(R.id.sb_player);
    }

    protected void playerPlay(final Uri f) {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayerCompat.createMediaPlayer(this, f, null);
            if (getPrefCall()) {
                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                tm.listen(pscl, PhoneStateListener.LISTEN_CALL_STATE);
            }
        }
        if (mMediaPlayer == null) {
            Toast.makeText(this, com.t440s.audio.R.string.file_not_found, Toast.LENGTH_SHORT).show();
            return;
        }
        mMediaPlayer.start();
        mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);

        if (proximity == null) {
            proximity = new ProximityPlayer(this) {
                @Override
                public void prepare() {
                    try {
                        int pos = mMediaPlayer.getCurrentPosition();
                        mMediaPlayer.release();
                        mMediaPlayer = new MediaPlayer();
                        if (Build.VERSION.SDK_INT >= 21) {
                            AudioAttributes.Builder b = new AudioAttributes.Builder();
                            switch (streamType) {
                                case AudioManager.STREAM_MUSIC:
                                    b.setUsage(AudioAttributes.USAGE_MEDIA);
                                    b.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
                                    break;
                                case AudioManager.STREAM_VOICE_CALL:
                                    b.setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION);
                                    b.setContentType(AudioAttributes.CONTENT_TYPE_SPEECH);
                                    break;
                            }
                            b.setLegacyStreamType(streamType);
                            final AudioAttributes aa = b.build();
                            mMediaPlayer.setAudioAttributes(aa);
                        } else {
                            mMediaPlayer.setAudioStreamType(streamType);
                        }
                        mMediaPlayer.setDataSource(PreviewActivity.this, f);
                        mMediaPlayer.prepare();
                        mMediaPlayer.seekTo(pos);
                        mMediaPlayer.start();
                        mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);
                    } catch (IOException e) {
                        playerStop();
                    }
                }
            };
            proximity.create();
        }
        mPlayerSb.setMax(mMediaPlayer.getDuration());

    }

    public boolean getPrefCall() {
        return false;
    }

    protected void playerPause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);

        }
    }

    protected void playerStop() {
        if (proximity != null) {
            proximity.close();
            proximity = null;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (pscl != null) {
            TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            tm.listen(pscl, PhoneStateListener.LISTEN_NONE);
            pscl = null;
        }
    }

    private void delete() {
        final Runnable delete = new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(PreviewActivity.this);
                builder.setTitle(com.t440s.audio.R.string.delete_recording);
                builder.setMessage("...\\" + name + "\n\n" + getString(com.t440s.audio.R.string.are_you_sure));
                builder.setPositiveButton(com.t440s.audio.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        playerStop(); // in case if playback got started twice during delete animation
                        dialog.cancel();
                        Storage.delete(PreviewActivity.this, uri);
                        Toast.makeText(PreviewActivity.this, getString(R.string.deletefinal), Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(PreviewActivity.this,MainActivity.class);
                        startActivity(i);
                        finish();
                    }
                });
                builder.setNegativeButton(com.t440s.audio.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                showDialog(builder);
            }
        };
        delete.run();
    }

    public void showDialog(AlertDialog.Builder e) {
        e.show();
    }

}
