package com.viomi.kettlepro.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.viomi.kettlepro.R;
import com.viomi.kettlepro.UMGlobalParam;
import com.viomi.kettlepro.dev.UMBlueToothUpgrader;
import com.viomi.kettlepro.dev.UMBluetoothManager;
import com.viomi.kettlepro.interfaces.UMStatusInterface;
import com.viomi.kettlepro.utils.FileUtil;
import com.viomi.kettlepro.utils.log;
import com.viomi.kettlepro.view.AllRulerCallback;
import com.viomi.kettlepro.view.MyTextView;
import com.viomi.kettlepro.view.VerticalRulerView;
import com.viomi.kettlepro.view.VerticalTimeRulerView;
import com.xiaomi.smarthome.common.ui.dialog.MLAlertDialog;
import com.xiaomi.smarthome.device.api.IXmPluginHostActivity;
import com.xiaomi.smarthome.device.api.XmPluginBaseActivity;
import com.xiaomi.smarthome.device.api.XmPluginHostApi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class UMMainActivity extends XmPluginBaseActivity implements View.OnClickListener {
    private static final String TAG = UMMainActivity.class.getSimpleName();
    private final static int MENU_REQUEST_CODE = 1;
    private static final int REQUEST_MENUS_SECOND = 22;
    private static final int MSG_REFRESH_ICONS = 100;

    private SharedPreferences mSharedPreferences;
    private File mLicenseFile;
    private File mPrivacyFile;
    public final static byte MSG_WHAT_INIT_LICENSE = 100;//初始化隐私协议文件

    public final static byte VERSION_LICENSE_ADD = 67;//添加隱私協議api
    private TextView tvStatus, tvMode, tvDuration, tvKeep_temp;
    private MyTextView tvTemp;
    private RelativeLayout rl_mode, rl_duration, rl_temp;
    private LinearLayout ll_mode;
    private TextView tv_title, tv_coffee, tv_rice, tv_tea, tv_custom;
    private ImageView backBn, barMore;
    private View v_top, v_bottom;
    private VerticalRulerView vr_temp;
    private VerticalTimeRulerView vr_time;
    private MLAlertDialog mlAlertDialog;
    private Context mContext;

    private int mKeyChoose = UMGlobalParam.KEY_NULL;//按键选择
    private boolean isDataRefresh = false;
    private boolean mIsOnline = true;
    private int mTempCustom;//设置温度
    private int mCurrentTemp;
    private @UMGlobalParam.HeatModel
    int mHeatModel;//设定模式
//    private @UMGlobalParam.MemoryStatus
//    int mMemoryStatus;//记忆开关

    //状态显示
    @UMGlobalParam.Status
    int status;//状态模式

    private @UMGlobalParam.Errors
    int mError;//异常

    private Timer mTimer;
    private TimerTask mTimeTask;
    private boolean mDataRefresh = true;//数据是否刷新，水壶500ms左右，上报一次数据，设置后1000ms才开始读数据
    private int[] resIds = new int[]{R.id.iv_arrow01, R.id.iv_arrow02, R.id.iv_arrow03};

    private int[] resStartAnimIds = new int[]{
            R.animator.icon_circle_bg01_start,
            R.animator.icon_circle_bg02_start,
            R.animator.icon_circle_bg03_start,
            R.animator.icon_circle_bg04_start,
            R.animator.icon_circle_bg05_start};

    private int[] resStopAnimIds = new int[]{
            R.animator.icon_circle_bg01_end,
            R.animator.icon_circle_bg02_end,
            R.animator.icon_circle_bg03_end,
            R.animator.icon_circle_bg04_end,
            R.animator.icon_circle_bg05_end};

    private int[] resIconIds = new int[]{
            R.id.iv_anim1,
            R.id.iv_anim2,
            R.id.iv_anim3,
            R.id.iv_anim4,
            R.id.iv_anim5};

    private Animator[] animatorStarts, animatorStops;
    private PopupWindow popupWindow;
    private int statusType = -2;//-1:沒網絡
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_REFRESH_ICONS:
                    String str = (String) msg.obj;
                    setImagesBackground(str);
                    break;
            }
        }
    };
    private Animation mHideAnimation, mShowAnimation;
    private boolean isMoving = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = UMMainActivity.this;
        View title_bar = findViewById(R.id.title_bar);
        if (title_bar != null) {
            mHostActivity.setTitleBarPadding(title_bar);
        }
        initAnimator();
        initViews();
    }

    private void setImagesBackground(String str) {
        if (str.equals(getString(R.string.um_status_net_abnormal)) ||
                str.equals(getString(R.string.um_status_abnormal))) {
            tvStatus.setText("--");
            if (statusType != -1) {
                tvTemp.refresView(R.color.text_gray_start, R.color.text_gray_end);
                refreshBackground(R.drawable.icon_gray_bg);
                statusType = -1;
            }
        } else {
            tvStatus.setText(str);
            if (mCurrentTemp <= 60) {
                if (statusType != 1) {
                    tvTemp.refresView(R.color.text_bule_start, R.color.text_bule_end);
                    refreshBackground(R.drawable.icon_bule_bg);
                    statusType = 1;
                }
            } else if (mCurrentTemp >= 61 && mCurrentTemp <= 80) {
                if (statusType != 2) {
                    tvTemp.refresView(R.color.text_yellow_start, R.color.text_yellow_end);
                    refreshBackground(R.drawable.icon_yellow_bg);
                    statusType = 2;
                }
            } else {
                if (statusType != 0) {
                    tvTemp.refresView(R.color.text_red_start, R.color.text_red_end);
                    refreshBackground(R.drawable.icon_red_bg);
                    statusType = 0;
                }
                if (mCurrentTemp >= 98) {
                    tvStatus.setText("沸腾中");
                }
            }
        }
//        if (str.equals(getString(R.string.um_status_net_abnormal)) ||
//                str.equals(getString(R.string.um_status_abnormal))) {//异常
//            if (statusType != -1) {
//                tvTemp.refresView(R.color.text_gray_start, R.color.text_gray_end);
//                refreshBackground(R.drawable.icon_gray_bg);
//                statusType = -1;
//            }
//        } else if (str.equals(getString(R.string.um_status_heating))) {//加热中
//            if (statusType != 1) {
//                tvTemp.refresView(R.color.text_red_start, R.color.text_red_end);
//                refreshBackground(R.drawable.icon_red_bg);
//                statusType = 1;
//            }
//        } else if (str.equals(getString(R.string.um_status_keep_warm_temp_down)) ||
//                str.equals(getString(R.string.um_status_close))) {//降温,空闲中
//            if (statusType != 2) {
//                tvTemp.refresView(R.color.text_bule_start, R.color.text_bule_end);
//                refreshBackground(R.drawable.icon_bule_bg);
//                statusType = 2;
//            }
//        } else {//保溫中，已保溫
//            if (statusType != 0) {
//                tvTemp.refresView(R.color.text_yellow_start, R.color.text_yellow_end);
//                refreshBackground(R.drawable.icon_yellow_bg);
//                statusType = 0;
//            }
//        }
    }

    private void refreshBackground(int resId) {
        for (int i = 0; i < resIconIds.length; i++) {
            ((ImageView) findViewById(resIconIds[i])).setImageResource(resId);
        }
    }


    private void initAnimator() {
        animatorStarts = new Animator[resStartAnimIds.length];
        animatorStops = new Animator[resStopAnimIds.length];
        for (int i = 0; i < resStartAnimIds.length; i++) {
            AnimatorSet animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(this, resStartAnimIds[i]);
            animatorSet.setTarget(findViewById(resIconIds[i]));
            animatorSet.setInterpolator(new AccelerateInterpolator());//设置end时的弹跳插入器
            animatorStarts[i] = animatorSet;
        }

        for (int i = 0; i < resStopAnimIds.length; i++) {
            AnimatorSet animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(this, resStopAnimIds[i]);
            animatorSet.setTarget(findViewById(resIconIds[i]));
            animatorSet.setInterpolator(new AccelerateInterpolator());//设置end时的弹跳插入器
            animatorStops[i] = animatorSet;
        }
    }

    private void initViews() {
        mSharedPreferences = activity().getSharedPreferences(String.valueOf(mDeviceStat.did), Context.MODE_PRIVATE);
        tv_title = (TextView) findViewById(R.id.title_bar_title);
        vr_time = (VerticalTimeRulerView) findViewById(R.id.rv_time);
        vr_temp = (VerticalRulerView) findViewById(R.id.rv_temp);
//        rlAnim = (RelativeLayout) findViewById(R.id.rl_anim);
//        ivAnim = (ImageView) findViewById(R.id.iv_anim);
        tvStatus = (TextView) findViewById(R.id.tv_status);
        tvTemp = (MyTextView) findViewById(R.id.tv_temp);
        tvMode = (TextView) findViewById(R.id.tv_mode);
        tvDuration = (TextView) findViewById(R.id.tv_duration);
        tvKeep_temp = (TextView) findViewById(R.id.tv_keep_temp);

        v_top = findViewById(R.id.v_top);
        v_bottom = findViewById(R.id.v_bottom);

        rl_mode = (RelativeLayout) findViewById(R.id.rl_mode);
        rl_duration = (RelativeLayout) findViewById(R.id.rl_duration);
        rl_temp = (RelativeLayout) findViewById(R.id.rl_temp);

        backBn = (ImageView) findViewById(R.id.title_bar_return);
        barMore = (ImageView) findViewById(R.id.title_bar_more);

        ll_mode = (LinearLayout) findViewById(R.id.ll_mode);
        tv_coffee = (TextView) findViewById(R.id.tv_coffee);
        tv_rice = (TextView) findViewById(R.id.tv_rice);
        tv_tea = (TextView) findViewById(R.id.tv_tea);
        tv_custom = (TextView) findViewById(R.id.tv_custom);

        tv_coffee.setOnClickListener(this);
        tv_rice.setOnClickListener(this);
        tv_tea.setOnClickListener(this);
        tv_custom.setOnClickListener(this);

        backBn.setOnClickListener(this);
        barMore.setOnClickListener(this);

        v_top.setOnClickListener(this);
        v_bottom.setOnClickListener(this);
        rl_mode.setOnClickListener(this);
        rl_duration.setOnClickListener(this);
        rl_temp.setOnClickListener(this);

        ll_mode.setTag(false);
        vr_time.setTag(false);
        vr_temp.setTag(false);

//        ll_mode.setAlpha(0);
//        vr_time.setAlpha(0);
//        vr_temp.setAlpha(0);
        ll_mode.setVisibility(View.GONE);
        vr_time.setVisibility(View.GONE);
        vr_temp.setVisibility(View.GONE);
        setUpView();
        initRulerView();
        UMBluetoothManager.getInstance().readMcuVersion();
    }

    @Override
    public void onResume() {
        super.onResume();
        UMBluetoothManager.getInstance().runUpdateInfo();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);

    }

    private Animation getAnim() {
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.setFillAfter(true);
        animationSet.setFillBefore(false);
        animationSet.setDuration(600);
        RotateAnimation rotate = new RotateAnimation(0,
                45,
                Animation.RELATIVE_TO_PARENT,
                0.5F,
                Animation.RELATIVE_TO_PARENT, 0.5F);
//        TranslateAnimation translate = new TranslateAnimation();
        animationSet.addAnimation(rotate);
//        animationSet.addAnimation(translate);
        return animationSet;
    }


    private void startAnim() {
        openAnimators(animatorStarts);
    }

    private void stopAnim() {
        openAnimators(animatorStops);
    }


    private void openAnimators(Animator[] animators) {
        if (animators != null && animators.length != 0) {
            for (int i = 0; i < animators.length; i++) {
                animators[i].start();
            }
        }
    }

    private void initRulerView() {
        vr_temp.setUnit("℃");
        vr_temp.setMax(90);
        vr_temp.setMin(40);
        vr_temp.setNumber(60);
        vr_temp.setInterval(10);
        vr_temp.setTextOffset(20);

        vr_time.setUnit("h");
        vr_time.setMax(120);
        vr_time.setMin(5);
        vr_time.setNumber(20);
        vr_time.setInterval(120);
        vr_time.setTextOffset(10);

        vr_time.setRuleListener(new AllRulerCallback() {
            @Override
            public void onRulerSelected(int length, int value) {
                float time = (float) value / (float) 10;
                setKeepWarmTime(time);
            }
        });

        vr_temp.setRuleListener(new AllRulerCallback() {
            @Override
            public void onRulerSelected(int length, int value) {
                setKeepWarmTemp(value);
            }
        });
    }


    private void setKeepWarmTime(float time) {
//        tvDuration.setText("" + time);
        UMBluetoothManager.getInstance().setKeepWarmTime(time);
        startTimer();
    }

    private void setKeepWarmTemp(final int temp) {
//        tvKeep_temp.setText("" + temp);
        int flag = mKeyChoose;
        Log.d(TAG, "onTempChange,getKeyChoose=" + flag + ",UMGlobalParam.KEY_NULL=" + UMGlobalParam.KEY_NULL);
        if (flag == UMGlobalParam.KEY_NULL) {
            // UMCustomToast.showToast(activity(),getString(R.string.toast_idle_temp_set), Toast.LENGTH_SHORT);
            UMBluetoothManager.getInstance().onSetup(mHeatModel, temp);
            startTimer();
        } else if (flag == UMGlobalParam.KEY_BOIL) {
            UMBluetoothManager.getInstance().onSetup(mHeatModel, temp);
            startTimer();
        } else if (flag == UMGlobalParam.KEY_KEEP_WARM && mTempCustom != temp) {
            mlAlertDialog = new MLAlertDialog.Builder(activity()).setMessage(getString(R.string.toast_keep_warm_temp_set))
                    .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                            mUmTempSeekbarView.setTemp(mTempCustom);
                            tvKeep_temp.setText("" + mTempCustom);
                        }
                    })
                    .setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            UMBluetoothManager.getInstance().onSetup(mHeatModel, temp);
                            startTimer();

                        }
                    }).create();
            mlAlertDialog.show();
        }
    }

    private void setUpView() {
//        animEnter = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_right);
//        animExit = AnimationUtils.loadAnimation(mContext, R.anim.slide_out_right);
//        animEnter.setAnimationListener(animEnterListener);
//        animExit.setAnimationListener(animationListener);
        UMBluetoothManager.getInstance().init(activity(), mDeviceStat, new UMStatusInterface() {
            @Override
            public void onStatusDataReceive(byte[] data) {
                refreshView(data);
            }

            @Override
            public void isOnlineChange(boolean isOnline) {
                Log.d(TAG, "isOnline=" + isOnline);
                mIsOnline = isOnline;
                if (!mIsOnline) {
                    setOfflineView();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.title_bar_return:
                finish();
                break;
            case R.id.title_bar_more:
                ArrayList<IXmPluginHostActivity.MenuItemBase> menus = new
                        ArrayList<>();
                ////插件自定义菜单，可以在public void onActivityResult(int requestCode, int resultCode, Intent data) 中接收用户点击的菜单项，String result = data.getStringExtra("menu");
                IXmPluginHostActivity.StringMenuItem stringMenuItem = new IXmPluginHostActivity.StringMenuItem();
                stringMenuItem.name = getString(R.string.um_common_setting);
                menus.add(stringMenuItem);
                Intent intent = new Intent();
                intent.putExtra("scence_enable", false);
                intent.putExtra("common_setting_enable", false);
                mHostActivity.openMoreMenu2(menus, true, MENU_REQUEST_CODE, intent);
                break;
            case R.id.v_top:
            case R.id.v_bottom:
                hideViews(view);
                break;
            case R.id.rl_mode:
                showWindow(ll_mode);
                break;
            case R.id.rl_duration:
                vr_time.resetMeasure();
                showWindow(vr_time);
                break;
            case R.id.rl_temp:
                vr_temp.resetMeasure();
                showWindow(vr_temp);
                break;
            case R.id.tv_coffee:
                setKeepWarmTemp(90);
                break;
            case R.id.tv_rice:
                setKeepWarmTemp(70);
                break;
            case R.id.tv_tea:
                setKeepWarmTemp(80);
                break;
            case R.id.tv_custom:
                if (mCurrentTemp != 0) {
                    setKeepWarmTemp(mCurrentTemp);
                }
                break;
        }
    }

    private void showWindow(View view) {
        if (isMoving) return;
        if (ll_mode.getVisibility() == View.VISIBLE && ll_mode.getId() != view.getId()) {
            stopViewAnim(ll_mode);
            return;
        } else if (vr_time.getVisibility() == View.VISIBLE && vr_time.getId() != view.getId()) {
            stopViewAnim(vr_time);
            return;
        } else if (vr_temp.getVisibility() == View.VISIBLE && vr_temp.getId() != view.getId()) {
            stopViewAnim(vr_temp);
            return;
        }
        if (view.getVisibility() == View.VISIBLE) {
            stopViewAnim(view);
        } else {
            startViewAnim(view);
        }
    }

    private void hideViews(View view) {
        if (isMoving) return;
        if (ll_mode.getVisibility() == View.VISIBLE && ll_mode.getId() != view.getId()) {
            stopViewAnim(ll_mode);
            return;
        } else if (vr_time.getVisibility() == View.VISIBLE && vr_time.getId() != view.getId()) {
            stopViewAnim(vr_time);
            return;
        } else if (vr_temp.getVisibility() == View.VISIBLE && vr_temp.getId() != view.getId()) {
            stopViewAnim(vr_temp);
            return;
        }
    }

    private boolean isGone(View view) {
        boolean flag = (boolean) view.getTag();
        return flag;
    }

    private void startViewAnim(final View view) {
        setShowAnimation(view, 1000);
//        view.setAlpha(0f);
//        view.setVisibility(View.VISIBLE);
//        view.animate().alpha(1f)
//                .setDuration(1000)
//                .setListener(null).start();


//        view.clearAnimation();
//        view.setVisibility(View.VISIBLE);
//        Animation animEnter = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_right);
//        animEnter.setAnimationListener(animEnterListener);
//        view.startAnimation(animEnter);

//        Log.i("info","====================Alpha:"+view.getAlpha());
//        view.setVisibility(View.VISIBLE);
//        view.setAlpha(1f);
//        final ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.view_enter);
//        anim.setTarget(view);
//        anim.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animator) {
//                isMoving=true;
//                startAnim();
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animator) {
//                isMoving=false;
////                view.setTag(false);
////                anim.removeAllListeners();
////                anim.removeAllUpdateListeners();
////                anim.cancel();
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animator) {
//
//            }
//        });
//        anim.start();
//        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                final float value = (Float) valueAnimator.getAnimatedValue();
//                hideArraws((float) (1.0 - value));
//            }
//        });
    }


    private void stopViewAnim(final View view) {
        setHideAnimation(view, 600);
//        view.animate().translationX(300).setDuration(1000).setListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animator) {
//                view.setVisibility(View.GONE);
//                view.setTranslationX(0);
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animator) {
//
//            }
//        }).start();


//        view.clearAnimation();
//        Animation animExit = AnimationUtils.loadAnimation(mContext, R.anim.slide_out_right);
//        animExit.setAnimationListener(animationListener);
//        view.startAnimation(animExit);

//        ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.view_exit);
//        anim.setTarget(view);
//        anim.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animator) {
//                stopAnim();
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animator) {
//                showArraws();
//                if (!isGone(ll_mode)) {
//                    ll_mode.setTag(true);
//                }
//
//                if (!isGone(vr_time)) {
//                    vr_time.setTag(true);
//                }
//
//                if (!isGone(vr_temp)) {
//                    vr_temp.setTag(true);
//                }
////                if (ll_mode.getAlpha() != 0) {
////                    ll_mode.setAlpha(0);
////                }
////                if (vr_time.getAlpha() != 0) {
////                    vr_time.setAlpha(0);
////                }
////                if (vr_temp.getAlpha() != 0) {
////                    vr_temp.setAlpha(0);
////                }
//
//
////                if (ll_mode.getVisibility() == View.VISIBLE) {
////                    ll_mode.setVisibility(View.GONE);
////                    ll_mode.clearAnimation();
////                }
////                if (vr_time.getVisibility() == View.VISIBLE) {
////                    vr_time.setVisibility(View.GONE);
////                    vr_time.clearAnimation();
////                }
////                if (vr_temp.getVisibility() == View.VISIBLE) {
////                    vr_temp.setVisibility(View.GONE);
////                    vr_temp.clearAnimation();
////                }
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animator) {
//
//            }
//        });
//        anim.start();
    }

    private Animation.AnimationListener animationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
//            stopAnim();
            showArraws();
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (ll_mode.getVisibility() == View.VISIBLE) {
                ll_mode.setVisibility(View.GONE);
//                ll_mode.clearAnimation();
            }

            if (vr_time.getVisibility() == View.VISIBLE) {
                vr_time.setVisibility(View.GONE);
//                vr_time.clearAnimation();
            }

            if (vr_temp.getVisibility() == View.VISIBLE) {
                vr_temp.setVisibility(View.GONE);
//                vr_temp.clearAnimation();
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private Animation.AnimationListener animEnterListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
//            startAnim();
        }

        @Override
        public void onAnimationEnd(Animation animation) {
//            hideArraws();
//            switch (choseType) {
//                case 1:
//
//                    break;
//                case 2:
//
//                    break;
//                case 3:
//
//                    break;
//            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private void showArraws() {
        for (int i = 0; i < resIds.length; i++) {
//            findViewById(resIds[i]).setVisibility(View.VISIBLE);
            findViewById(resIds[i]).setAlpha((float) 1.0);
        }
    }

    private void hideArraws(float alpha) {
        for (int i = 0; i < resIds.length; i++) {
//            findViewById(resIds[i]).setVisibility(View.GONE);
            findViewById(resIds[i]).setAlpha(alpha);
        }
    }

    private void setOfflineView() {

    }

    /****
     * 刷新状态界面
     * @param data 接收蓝牙通知数据
     */
    private void refreshView(byte[] data) {
        Log.i("info", "===================data:" + data);
        if (data == null) {
            Log.e(TAG, "MSG_WHAT_STATUS_VARY,data null!");
            return;
        }
        if (data.length < 7) {
            Log.e(TAG, "MSG_WHAT_STATUS_VARY,data length is " + data.length + ",not correct!");
            return;
        }

        //状态显示
//        @UMGlobalParam.Status int status = data[0];
        status = data[0];

        //按键选择
        mKeyChoose = data[1] & 0xff;

        //是否完成
        boolean isCompleted = false;
        if (data[2] == 0) {
            isCompleted = false;
        } else if (data[2] == 1) {
            isCompleted = true;
        }

        //异常
        mError = data[3];
        //      mError=UMGlobalParam.ERROR_PARCH;
//        status=UMGlobalParam.STATUS_ABNORMAL;
        //	setErrorText(mError);

        //自定义温度
        int mTemp = data[4] & 0xff;
        mTempCustom = mTemp;

        //实际温度显示
        int currentTemp = data[5] & 0xff;
        if (currentTemp < 0) {
            currentTemp = 0;
        } else if (currentTemp > 100) {
            currentTemp = 100;
        }
        tvTemp.setText("" + currentTemp);
        mCurrentTemp = currentTemp;

        //自定义模式
        mHeatModel = data[6];
        boolean isBiol;
        if (mHeatModel == UMGlobalParam.MODEL_KEEP_WARM_BOIL) {
            isBiol = true;
        } else {
            isBiol = false;
        }

        //保温消耗时间、煮沸模式特殊模式、保温时间
        float mSetTime = UMGlobalParam.MAX_KEEP_WARM_TIME;
        boolean boilModeSelect = true;
        int mConsumeTime = 0;

        if (data.length >= 11) {
            mConsumeTime = data[7] & 0xff + (data[8] & 0xff) * 256;
            log.d(TAG, "mConsumeTime=" + mConsumeTime);

            if (data[9] == 0) {
                boilModeSelect = false;
            } else if (data[9] == 1) {
                boilModeSelect = true;
            }

            mSetTime = (float) ((int) (data[10] & 0xff) * 0.5);
            if (mSetTime > UMGlobalParam.MAX_KEEP_WARM_TIME) {
                mSetTime = UMGlobalParam.MAX_KEEP_WARM_TIME;
            } else if (mSetTime < UMGlobalParam.MIN_KEEP_WARM_TIME) {
                mSetTime = UMGlobalParam.MIN_KEEP_WARM_TIME;
            }
//            //提壶记忆功能开关
            if (data.length >= 12) {
//                mMemoryStatus = data[11];
//                if (data[11] == 0) {
////                    mIsLiftUpHold = false;
//                } else if (data[11] == 1) {
////                    mIsLiftUpHold = true;
//                }
            }
        }
        String string = getStatusString(mIsOnline, status, isCompleted, currentTemp, mTempCustom, mConsumeTime);
        if (!TextUtils.isEmpty(string)) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_REFRESH_ICONS, string));
        } else {
            String statusStr = getString(R.string.um_status_net_abnormal);
            mHandler.sendMessage(mHandler.obtainMessage(MSG_REFRESH_ICONS, statusStr));
        }

        tvDuration.setText("" + mSetTime);
        tvKeep_temp.setText("" + mTemp);
        tvMode.setText(getModeString(mTemp));
//        if (!isDataRefresh) {
        vr_time.setNumber(((int) mSetTime) * 10);
        vr_temp.setNumber(mTemp);
        isDataRefresh = true;
//        }


//        mBubbleAnimation.setTemp(currentTemp);
//        onSpecialStatusShow(mIsOnline,status,mError,currentTemp);
        if (!mDataRefresh) {
            return;
        }
//        mUmTimeSeekbarView.setTime(mSetTime);
//        //弹出工作中修改确认窗口时，不刷新设置温度
//        if(mlAlertDialog==null||(!mlAlertDialog.isShowing())){
//            mUmTempSeekbarView.setTemp(mTemp);
//        }
//
//        mUMModelSetView.setModel(isBiol);
//        mUMModelSetView.setBoilModeSelect(boilModeSelect);
//
//        if((!mIsLiftUpHold)&&mLiftUpSwitchButton.isChecked()){
//            mIgnoreChange=true;
//            mLiftUpSwitchButton.setChecked(false);
//            mIgnoreChange=false;
//        }else if(mIsLiftUpHold&&(!mLiftUpSwitchButton.isChecked())){
//            mIgnoreChange=true;
//            mLiftUpSwitchButton.setChecked(true);
//            mIgnoreChange=false;
//        }
    }

    private String getModeString(int temp) {
        String mode = "自定义";
        if (temp == 40) {
            mode = getString(R.string.text_protiotics);
        } else if (temp == 70) {
            mode = getString(R.string.text_cereal);
        } else if (temp == 80) {
            mode = getString(R.string.text_tea);
        } else if (temp == 90) {
            mode = getString(R.string.text_coffee);
        }
        return mode;
    }

    //状态显示
    private String getStatusString(boolean isOnline, int status, boolean isCompleted, int currentTemp, int setTemp, int mConsumeTime) {
        String statusStr = "";
        log.d(TAG, "isOnline=" + isOnline + ",status=" + status + ",isCompleted=" + isCompleted);
        if (isOnline) {
            if (status == UMGlobalParam.STATUS_IDLE) {
                statusStr = getString(R.string.um_status_close);
//                mBubbleAnimation.setBubblesRun(false);
            } else if (status == UMGlobalParam.STATUS_HEATING) {
//                mBubbleAnimation.setBubblesRun(true);
                statusStr = getString(R.string.um_status_heating);
            } else if (status == UMGlobalParam.STATUS_KEEP_WARM_NOT__BOIL) {
//                mBubbleAnimation.setBubblesRun(false);
                if (mConsumeTime <= 0) {
                    statusStr = getString(R.string.um_status_keep_warm);
                } else {
                    if (mConsumeTime < 60) {
                        statusStr = getString(R.string.um_status_keep_warm1) + mConsumeTime + getString(R.string.um_status_keep_warm_m);
                    } else {
                        int hour = mConsumeTime / 60;
                        int minute = mConsumeTime % 60;
                        if (minute == 0) {
                            statusStr = getString(R.string.um_status_keep_warm1) + hour + getString(R.string.um_status_keep_warm_h);
                        } else {
                            statusStr = getString(R.string.um_status_keep_warm1) + hour + getString(R.string.um_status_keep_warm_h)
                                    + minute + getString(R.string.um_status_keep_warm_m);
                        }
                    }
                }
            } else if (status == UMGlobalParam.STATUS_KEEP_WARM_BOIL) {
//                mBubbleAnimation.setBubblesRun(false);
                if ((!isCompleted) && currentTemp > setTemp) {
                    statusStr = getString(R.string.um_status_keep_warm_temp_down);
                } else {
                    if (mConsumeTime <= 0) {
                        statusStr = getString(R.string.um_status_keep_warm);
                    } else {
                        if (mConsumeTime < 60) {
                            statusStr = getString(R.string.um_status_keep_warm1) + mConsumeTime + getString(R.string.um_status_keep_warm_m);
                        } else {
                            int hour = mConsumeTime / 60;
                            int minute = mConsumeTime % 60;
                            if (minute == 0) {
                                statusStr = getString(R.string.um_status_keep_warm1) + hour + getString(R.string.um_status_keep_warm_h);
                            } else {
                                statusStr = getString(R.string.um_status_keep_warm1) + hour + getString(R.string.um_status_keep_warm_h)
                                        + minute + getString(R.string.um_status_keep_warm_m);
                            }
                        }

                    }
                }
            } else {
//                mBubbleAnimation.setBubblesRun(false);
                statusStr = getString(R.string.um_status_abnormal);
            }
        }
        return statusStr;
    }

    public void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimeTask != null) {
            mTimeTask.cancel();
            mTimeTask = null;
        }
    }

    public void startTimer() {
        stopTimer();
        mDataRefresh = false;
        mTimer = new Timer();
        mTimeTask = new TimerTask() {
            @Override
            public void run() {
                mDataRefresh = true;
            }
        };
        mTimer.schedule(mTimeTask, 1400);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case MENU_REQUEST_CODE:
                if (data != null) {
                    String name = data.getStringExtra("menu");
                    log.d(TAG, "menu=" + name);
                    if (!TextUtils.isEmpty(name)) {
                        if (name.equals(getString(R.string.um_common_setting))) {
                            openSubMenu1();
                        }
                    }
                }
                break;
            case REQUEST_MENUS_SECOND:
                tv_title.setText(mDeviceStat.name);
                // 撤销用户协议
                if (data != null && data.getExtras() != null) {
                    String result_data = data.getStringExtra("result_data");
                    if ("removedLicense".equals(result_data)) {
                        mSharedPreferences.edit().putString("uid", "").apply();
                        mSharedPreferences.edit().putLong("time", 0).apply();
                        UMGlobalParam.getInstance().clear();
                        activity().finish();
                    }
                }
                break;
        }
    }

    /***
     * 复制隐私协议文件到sdcard里
     */
    private void copyLicenseFile() {

        String licenseFileName = "license.html";
        String privacyFileName = "private.html";
        if (mDeviceStat.model.equals(UMGlobalParam.MODEL_KETTLE_V5)) {
            privacyFileName = "private_ko.html";
        }
        String rootPath = activity().getFilesDir().getAbsolutePath();
        mLicenseFile = new File(rootPath, mDeviceStat.model + "_license.html");
        mPrivacyFile = new File(rootPath, mDeviceStat.model + "_privacy.html");
        try {
            log.d(TAG, "copyLicenseFile mLicenseFile:" + mLicenseFile.getAbsolutePath());
            log.d(TAG, "copyLicenseFile mPrivacyFile:" + mPrivacyFile.getAbsolutePath());
            FileUtil.copyFromAssets(getAssets(), licenseFileName, mLicenseFile.getAbsolutePath(), true);
            FileUtil.copyFromAssets(getAssets(), privacyFileName, mPrivacyFile.getAbsolutePath(), true);
        } catch (IOException e) {
            Log.e(TAG, "copyLicenseFile error！msg=:" + e.getMessage());
            e.printStackTrace();
        }
    }

    /***
     * 隐私文件是否存在
     * @return
     */
    private boolean isLicenseFileExist() {
        String rootPath = activity().getFilesDir().getAbsolutePath();
        File licenseFile = new File(rootPath, mDeviceStat.model + "_license.html");
        File privacyFile = new File(rootPath, mDeviceStat.model + "_privacy.html");
        boolean licenseFileFlag = FileUtil.isFileExist(licenseFile.getAbsolutePath());
        boolean privacyFileFlag = FileUtil.isFileExist(privacyFile.getAbsolutePath());
        if (licenseFileFlag & privacyFileFlag) {
            mLicenseFile = licenseFile;
            mPrivacyFile = privacyFile;
            return true;
        } else {
            return false;
        }
    }

    //传入升级管理
    private void openSubMenu1() {
        ArrayList<IXmPluginHostActivity.MenuItemBase> items = new ArrayList<IXmPluginHostActivity.MenuItemBase>();
        IXmPluginHostActivity.IntentMenuItem intentMenuItem = new IXmPluginHostActivity.IntentMenuItem();
        intentMenuItem.name = getString(R.string.um_device_info);
        intentMenuItem.intent = mHostActivity.getActivityIntent(null, UMDeviceInfoActivity.class.getName());
        intentMenuItem.intent.putExtra("mac", mDeviceStat.mac);
        items.add(intentMenuItem);

        IXmPluginHostActivity.IntentMenuItem intentMenuItem1 = new IXmPluginHostActivity.IntentMenuItem();
        intentMenuItem1.name = getString(R.string.um_device_fun);
        intentMenuItem1.intent = mHostActivity.getActivityIntent(null, UMDeviceFunctionActivity.class.getName());
        intentMenuItem1.intent.putExtra("mac", mDeviceStat.mac);
//        intentMenuItem1.intent.putExtra("status", mMemoryStatus);
        items.add(intentMenuItem1);

        IXmPluginHostActivity.BleMenuItem bleMenu = IXmPluginHostActivity.BleMenuItem.newUpgraderItem(new UMBlueToothUpgrader());
        items.add(bleMenu);
        Intent commonSettingIntent = new Intent();

        if (XmPluginHostApi.instance().getApiLevel() >= VERSION_LICENSE_ADD) {
            if (mDeviceStat.model.equals(UMGlobalParam.MODEL_KETTLE_V2) || mDeviceStat.model.equals(UMGlobalParam.MODEL_KETTLE_V3)
                    || mDeviceStat.model.equals(UMGlobalParam.MODEL_KETTLE_V5)) {
                if (mLicenseFile == null || mPrivacyFile == null) {//没加载过隐私协议文件
                    if (!isLicenseFileExist()) {//检测是否已经有隐私协议文件，有的话加载,没的话复制一份然后加载，先不考虑io操作阻塞
                        copyLicenseFile();
                    }
                }
                log.d(TAG, "openMoreMenu mLicenseFile:" + mLicenseFile.getAbsolutePath());
                log.d(TAG, "openMoreMenu mPrivacyFile:" + mPrivacyFile.getAbsolutePath());
                commonSettingIntent.putExtra("enableRemoveLicense", true);
                commonSettingIntent.putExtra("licenseContentUri", mLicenseFile.getAbsolutePath());
                commonSettingIntent.putExtra("privacyContentUri", mPrivacyFile.getAbsolutePath());
//           commonSettingIntent.putExtra("licenseContentHtml",getHtmlContent("private_ko.html"));
//           commonSettingIntent.putExtra("privacyContentHtml",getHtmlContent("private_ko.html"));
            }
        } else {
            if (mDeviceStat.model.equals(UMGlobalParam.MODEL_KETTLE_V2) || mDeviceStat.model.equals(UMGlobalParam.MODEL_KETTLE_V3)) {
                commonSettingIntent.putExtra("enableRemoveLicense", true);
                commonSettingIntent.putExtra("licenseContent", Html.fromHtml(activity().getResources().getString(R.string.um_license)));
                commonSettingIntent.putExtra("privacyContent", Html.fromHtml(activity().getResources().getString(R.string.um_conceal)));
            } else if (mDeviceStat.model.equals(UMGlobalParam.MODEL_KETTLE_V5) || mDeviceStat.model.equals(UMGlobalParam.MODEL_KETTLE_V6)) {
                commonSettingIntent.putExtra("enableRemoveLicense", true);
                commonSettingIntent.putExtra("licenseContent", Html.fromHtml(activity().getResources().getString(R.string.um_license)));
                commonSettingIntent.putExtra("privacyContent", Html.fromHtml(activity().getResources().getString(R.string.um_ko_conceal)));
            }
        }

        hostActivity().openMoreMenu(items, true, REQUEST_MENUS_SECOND, commonSettingIntent);
    }

    private void showMode(final Context context, View parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.pop_mode, null);
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, false);

        popupWindow.setAnimationStyle(R.style.popwin_anim_style);

        int[] location = new int[2];
        parent.getLocationOnScreen(location);

        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(getDrawableFromRes(R.drawable.bg_popwindow));

        popupWindow.showAtLocation(view, Gravity.BOTTOM | Gravity.RIGHT, 0, 0);
    }

    private Drawable getDrawableFromRes(int resId) {
        Resources res = getResources();
        Bitmap bmp = BitmapFactory.decodeResource(res, resId);
        return new BitmapDrawable(bmp);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UMBluetoothManager.getInstance().disconnect();
        UMBluetoothManager.getInstance().close();
        stopTimer();
    }

    public void setHideAnimation(final View view, int duration) {
//        if (null == view || duration < 0) {
//            return;
//        }
//
//        if (null != mHideAnimation) {
//            mHideAnimation.cancel();
//        }
//        // 监听动画结束的操作
////        mHideAnimation = new AlphaAnimation(1.0f, 0.0f);
//        mHideAnimation = new TranslateAnimation(0, 120, 0f, 0f);
//        mHideAnimation.setDuration(duration);
//        mHideAnimation.setFillAfter(false);
//        mHideAnimation.setFillBefore(false);
////        mHideAnimation = AnimationUtils.loadAnimation(mContext, R.anim.slide_out_right);
//        mHideAnimation.setAnimationListener(new Animation.AnimationListener() {
//
//            @Override
//            public void onAnimationStart(Animation arg0) {
//                stopAnim();
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation arg0) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation arg0) {
//                Log.i("info", "==================TranslationX:" + view.getTranslationX());
//                view.requestLayout();
//                view.invalidate();
//                view.setVisibility(View.GONE);
//                view.setTag(true);
//
////                if (ll_mode.getVisibility() == View.VISIBLE) {
////                    ll_mode.setVisibility(View.GONE);
////                }
////                if (vr_time.getVisibility() == View.VISIBLE) {
////                    vr_time.setVisibility(View.GONE);
////                }
////                if (vr_temp.getVisibility() == View.VISIBLE) {
////                    vr_temp.setVisibility(View.GONE);
////                }
//            }
//        });
//        view.startAnimation(mHideAnimation);

        ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.view_exit);
        anim.setTarget(view);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                isMoving = true;
                stopAnim();
                showArraws();
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isMoving = false;
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        anim.start();

    }

    public void setShowAnimation(final View view, int duration) {
        if (null == view || duration < 0) {
            return;
        }
        if (null != mShowAnimation) {
            mShowAnimation.cancel();
        }
        mShowAnimation = new AlphaAnimation(0.0f, 1.0f);
        mShowAnimation.setDuration(duration);
//        mShowAnimation.setFillAfter(true);
        view.setVisibility(View.VISIBLE);
        Log.i("info", "==========TranslationX:" + view.getTranslationX() + "\n"
                + "X:" + view.getX());
        if (view.getTranslationX() != 0) {
            view.setTranslationX(0);
        }

        mShowAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {
                isMoving = true;
                startAnim();
                hideArraws(0);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {

            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                isMoving = false;
            }
        });
        view.startAnimation(mShowAnimation);
    }
}
