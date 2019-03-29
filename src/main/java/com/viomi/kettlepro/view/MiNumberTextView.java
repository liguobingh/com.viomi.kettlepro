package com.viomi.kettlepro.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by liguobin on 2019/1/3
 */

public class MiNumberTextView extends TextView {
    public MiNumberTextView(Context context) {
        super(context);
        init();
    }

    public MiNumberTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MiNumberTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.setTypeface(TypefaceCache.getFaceText(getContext(), 1));
    }
}
