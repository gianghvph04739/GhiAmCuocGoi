package com.t440s.call.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.axet.androidlibrary.app.MediaPlayerCompat;
import com.github.axet.androidlibrary.services.StorageProvider;
import com.github.axet.androidlibrary.widgets.PopupShareActionProvider;
import com.github.axet.androidlibrary.widgets.ProximityPlayer;
import com.github.axet.androidlibrary.widgets.ProximityShader;
import com.t440s.call.R;
import com.audio.libary.app.MainApplication;
import com.t440s.call.app.Storage;

import java.io.IOException;

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mSaveTv;
    private Button mDeleteTv;
    private Button mShareTv;
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
    private Storage storage;
    private ImageView mStarImg;
    private boolean star;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        initView();
        storage = new Storage(this);
        Intent i = getIntent();
        uri = Uri.parse(i.getStringExtra("uri"));
        last = i.getStringExtra("last");
        duration = i.getStringExtra("duration");
        size = i.getStringExtra("size");
        name = i.getStringExtra("title");
        star = i.getBooleanExtra("star", false);
        getSupportActionBar().hide();

        mToolbar.setNavigationIcon(R.drawable.ic_back);
        mToolbar.setTitle(getString(R.string.detail_record));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (star == true)
            mStarImg.setImageResource(R.drawable.ic_star_black_24dp);
        else
            mStarImg.setImageResource(R.drawable.ic_star_border_black_24dp);

        mNameTv.setText(name);
        mSizeTv.setText(size);
        mTimeTv.setText(duration);
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayerCompat.createMediaPlayer(this, uri, null);
            if (getPrefCall()) {
                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                tm.listen(pscl, PhoneStateListener.LISTEN_CALL_STATE);
            }
        }
        if (mMediaPlayer == null) {
            Toast.makeText(this, com.t440s.call.R.string.file_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

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

        mSaveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PreviewActivity.this, "Done", Toast.LENGTH_SHORT).show();
                star = !star;
                updateStar();
                if (star == true)
                    mStarImg.setImageResource(R.drawable.ic_star_black_24dp);
                else
                    mStarImg.setImageResource(R.drawable.ic_star_border_black_24dp);

            }
        });

        mShareTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareRecords();
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
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
        }
        super.onBackPressed();
    }

    private void initView() {
        mSaveTv = (Button) findViewById(R.id.tv_save);
        mDeleteTv = (Button) findViewById(R.id.tv_delete);
        mShareTv = (Button) findViewById(R.id.tv_share);
        mAvtImg = (ImageView) findViewById(R.id.img_avt);
        mNameTv = (TextView) findViewById(R.id.tv_name);
        mTimeTv = (TextView) findViewById(R.id.tv_time);
        mSizeTv = (TextView) findViewById(R.id.tv_size);
        mPlayBtn = (ImageView) findViewById(R.id.btn_play);
        mPlayer = (LinearLayout) findViewById(R.id.player);
        mPlayerSb = (SeekBar) findViewById(R.id.sb_player);
        mStarImg = (ImageView) findViewById(R.id.img_star);
        mToolbar = (Toolbar) findViewById(R.id.toolbar2);
    }

    protected void playerPlay(final Uri f) {
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

    public void shareRecords() {
        String name = "Recordings";
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(getPackageName(), 0);
            name = info.loadLabel(pm).toString();
        } catch (PackageManager.NameNotFoundException e) {

        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(Storage.getTypeByName(name));
        intent.putExtra(Intent.EXTRA_EMAIL, "");
        intent.putExtra(Intent.EXTRA_STREAM, StorageProvider.getProvider().share(uri));
        intent.putExtra(Intent.EXTRA_SUBJECT, name);
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shared_via, name));
        PopupShareActionProvider.show(this, mShareTv, intent);
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
                builder.setTitle(com.t440s.call.R.string.delete_recording);
                builder.setMessage("...\\" + name + "\n\n" + getString(com.t440s.call.R.string.are_you_sure));
                builder.setPositiveButton(com.t440s.call.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        playerStop(); // in case if playback got started twice during delete animation
                        dialog.cancel();
                        Storage.delete(PreviewActivity.this, uri);
                        Toast.makeText(PreviewActivity.this, getString(R.string.deletefinal), Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(PreviewActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    }
                });
                builder.setNegativeButton(com.t440s.call.R.string.no, new DialogInterface.OnClickListener() {
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

    public void updateStar() {
        boolean b = !MainApplication.getStar(this, uri);
        MainApplication.setStar(this, uri, b);
    }

    public void showDialog(AlertDialog.Builder e) {
        e.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_save:
                // TODO 19/02/19
                break;
            case R.id.tv_delete:
                // TODO 19/02/19
                break;
            case R.id.tv_share:
                // TODO 19/02/19
                break;
            default:
                break;
        }
    }
}
