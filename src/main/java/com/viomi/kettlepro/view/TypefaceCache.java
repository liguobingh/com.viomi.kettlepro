package com.viomi.kettlepro.view;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by liguobin on 2019/01/3
 */

public class TypefaceCache {
    private static Typeface faceNumber, faceText, faceSubNumber;

    /**
     * @param context
     * @param type    0文字 1数字
     * @return
     */
    public static Typeface getFaceText(Context context, int type) {
        if (type == 0) {
            if (faceText == null)
                faceText = Typeface.createFromAsset(context.getAssets(), "fonts/MI_LanTing_Regular.ttf");
            return faceText;
        } else if (type == 1) {
            if (faceNumber == null)
                faceNumber = Typeface.createFromAsset(context.getAssets(), "fonts/KMedium.ttf");
            return faceNumber;
        } else {
            if (faceSubNumber == null)
                faceSubNumber = Typeface.createFromAsset(context.getAssets(), "fonts/MI_LanTing_Light.ttf");
            return faceSubNumber;
        }
    }
}
