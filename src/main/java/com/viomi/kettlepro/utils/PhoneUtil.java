package com.viomi.kettlepro.utils;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;


/**
 * Created by young2 on 2016/12/15.
 */

public class PhoneUtil {

    //转换dip为px
    public static int dipToPx(Context context, float dip) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dip*scale + 0.5f*(dip>=0?1:-1));
    }

    //转换px为dip
    public static float pxTodip(Context context, int px) {
        float scale = context.getResources().getDisplayMetrics().density;
        return px/scale + 0.5f*(px>=0?1:-1);
    }

    public static int spToPx(Context context, float sp) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * fontScale + 0.5f);
    }

    public static int pxToSp(Context context, float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /***
     * 手机型号
     * @return
     */
    public static  String getPhoneModel(){
        return  android.os.Build.MODEL;
    }

    /***
     * 系统版本号
     * @return
     */
    public static String getSystemVersion(){
        return    android.os.Build.VERSION.RELEASE;
    }


    /**
     * 获取屏幕高度(px)
     */
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }
    /**
     * 获取屏幕宽度(px)
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static String getScreen(Context context) {
        String str = getScreenWidth(context) + "*" + getScreenHeight(context);
        return str;
    }

    public static SpannableStringBuilder getRadiusGradientSpan(String string, int startColor, int endColor) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(string);
        LinearGradientFontSpan span = new LinearGradientFontSpan(startColor, endColor);
        spannableStringBuilder.setSpan(span, 0, spannableStringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableStringBuilder;

    }
}
