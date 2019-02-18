package com.t440s.audio.callrecorder.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.t440s.audio.callrecorder.R;

public class CustomImageView extends RelativeLayout  {
    Context context;
    ImageButton iconImgV;
    TextView titleTv;
    String title;

    public CustomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    public CustomImageView(Context context) {
        super(context);
        init();
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomImageView);
        title = typedArray.getString(R.styleable.CustomImageView_text);
        typedArray.recycle();
        init();
    }

    public void init() {
        if (isInEditMode()) return;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View myView = inflater.inflate(R.layout.custom_imageview, null);
        iconImgV = (ImageButton) myView.findViewById(R.id.iconImgV);
        titleTv = (TextView) myView.findViewById(R.id.titleTv);
        titleTv.setText(title);
        myView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        addView(myView);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);

    }

    public String getText(){
        Toast.makeText(context, title, Toast.LENGTH_SHORT).show();
        return title;
    }

    public interface getText{
        void getText(String text);
    }
}

