package com.t440s.call.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.t440s.call.R;

public class CustomImageView extends RelativeLayout  {
    Context context;
    ImageButton iconImgV;
    TextView titleTv;
    String title;
    RelativeLayout root;

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public CustomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public CustomImageView(Context context) {
        super(context);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomImageView);
        title = typedArray.getString(R.styleable.CustomImageView_text);
        typedArray.recycle();
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public void init() {
        if (isInEditMode()) return;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View myView = inflater.inflate(R.layout.custom_imageview, null);
        iconImgV = (ImageButton) myView.findViewById(R.id.iconImgV);
        root = (RelativeLayout) myView.findViewById(R.id.root);
        titleTv = (TextView) myView.findViewById(R.id.titleTv);
        titleTv.setText(title);
        iconImgV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickedListener != null) {
                    onItemClickedListener.getText(title);
                }
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

    private onItemClickedListener onItemClickedListener;

    public void setOnItemClickedListener(onItemClickedListener onItemClickedListener){
        this.onItemClickedListener = onItemClickedListener;
    }

    public interface onItemClickedListener{
        void getText(String text);
    }
}

