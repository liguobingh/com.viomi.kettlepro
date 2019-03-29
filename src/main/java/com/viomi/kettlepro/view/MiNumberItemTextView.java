package com.viomi.kettlepro.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by liguobin on 2019/1/3
 */

public class MiNumberItemTextView extends TextView {
    public MiNumberItemTextView(Context context) {
        super(context);
        init();
    }

    public MiNumberItemTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MiNumberItemTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.setTypeface(TypefaceCache.getFaceText(getContext(), 2));
    }
}
