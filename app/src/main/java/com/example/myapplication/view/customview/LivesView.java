package com.example.myapplication.view.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.R;

public class LivesView extends LinearLayout {

    private TextView tvLivesCount;
    private int initialLives = 5;

    public LivesView(Context context) {
        super(context);
        init(context, null);
    }

    public LivesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LivesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.custom_lives_view, this, true);
        tvLivesCount = findViewById(R.id.tvLivesCount);

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.LivesView,
                    0, 0);
            try {
                initialLives = a.getInt(R.styleable.LivesView_initialLives, 5);
            } finally {
                a.recycle();
            }
        }
        setLives(initialLives);
    }

    public void setLives(int lives) {
        tvLivesCount.setText(String.valueOf(lives));
    }
}