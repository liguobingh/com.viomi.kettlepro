package com.viomi.kettlepro.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
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
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.viomi.kettlepro.R;
import com.viomi.kettlepro.UMGlobalParam;
import com.viomi.kettlepro.dev.UMBlueToothUpgrader;
import com.viomi.kettlepro.dev.UMBluetoothManager;
import com.viomi.kettlepro.interfaces.UMStatusInterface;
import com.viomi.kettlepro.utils.FileUtil;
import com.viomi.kettlepro.utils.PhoneUtil;
import com.viomi.kettlepro.utils.log;
import com.viomi.kettlepro.view.AllRulerCallback;
import com.viomi.kettlepro.view.MyTextView;
import com.viomi.kettlepro.view.VerticalRulerView;
import com.viomi.kettlepro.view.VerticalTimeRulerView;
import com.xiaomi.smarthome.bluetooth.XmBluetoothManager;
import com.xiaomi.smarthome.common.ui.dialog.MLAlertDialog;
import com.xiaomi.smarthome.device.api.IXmPluginHostActivity;
import com.xiaomi.smarthome.device.api.XmPluginBaseActivity;
import com.xiaomi.smarthome.device.api.XmPluginHostApi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.xiaomi.smarthome.bluetooth.XmBluetoothManager.STATE_CONNECTED;
import static com.xiaomi.smarthome.bluetooth.XmBluetoothManager.STATE_CONNECTING;
import static com.xiaomi.smarthome.bluetooth.XmBluetoothManager.STATE_DISCONNECTED;
import static com.xiaomi.smarthome.bluetooth.XmBluetoothManager.STATE_DISCONNECTING;
import static com.xiaomi.smarthome.bluetooth.XmBluetoothManager.STATE_UNKNOWN;

public class UMMainActivity extends XmPluginBaseActivity implements View.OnClickListener {
    private static final String TAG = UMMainActivity.class.getSimpleName();
    private final static int MENU_REQUEST_CODE = 1;
    private static final int REQUEST_MENUS_SECOND = 22;
    private static final int MSG_REFRESH_ICONS = 10;

    private SharedPreferences mSharedPreferences;
    private File mLicenseFile;
    private File mPrivacyFile;
    public final static byte MSG_WHAT_INIT_LICENSE = 100;//初始化隐私协议文件
    private View titleBar;

    public final static byte VERSION_LICENSE_ADD = 67;//添加隐私协议api
    private TextView tvStatus, tvMode, tvDuration, tvKeepTemp;// 副标题获取的水壶工作状态显示、工作模式显示、保温时间长度显示、保温温度显示
    private TextView tag1;// 显示“当前温度”、“蓝牙未连接”、“蓝牙未开启”
    private MyTextView tvTemp;
    private RelativeLayout rl_mode, rl_duration, rl_temp;
    private LinearLayout ll_mode;
    private TextView tvTitle, tvCoffee, tvRice, tvTea, tvMilk;
    private ImageView backBn, barMore, bluOff;
    private View v_top, v_bottom;
//    private VerticalTimeRulerView setTime;// “保温时长”设置竖条
    private VerticalRulerView setTemp;// “保温温度”设置竖条
    //    private MLAlertDialog mlAlertDialog;
    private AlertDialog mlAlertDialog;
    private Context mContext;

    private int mKeyChoose = UMGlobalParam.KEY_NULL;//按键选择
    private boolean isDataRefresh = false;
    private boolean mIsOnline;
    private int mTempCustom;// 显示在界面的设置温度数值
    private int mCurrentTemp;
    private boolean tip;
    private boolean bluStatus;// 蓝牙是否开启
    private int connState;// 蓝牙是否连接设备
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
    private int statusType = -2;// -1:没网络
    private Animation mHideAnimation, mShowAnimation;
    private boolean isMoving = false;
    private int isOnActivity = 1;// 判斷是否在主界面

    private MLAlertDialog timerDialog;
    private TextView selectedTimer;
    private SeekBar timerPickerView;
    private float selectTimerValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = UMMainActivity.this;
        titleBar = findViewById(R.id.title_bar);
        if (titleBar != null) {
            mHostActivity.setTitleBarPadding(titleBar);
        }
        initViews();
//        tvTitle.setText(getString(R.string.umtitle));
        tvTitle.setText(mDeviceStat.name);// 用户可能自主命名设备，实时更新设备名称。
//        log.d("@@@@@", "model:" + mDeviceStat.model);

        // 显示隐私授权条例
        if (mDeviceStat.model.equals(UMGlobalParam.MODEL_KETTLE_V2) || mDeviceStat.model.equals(UMGlobalParam.MODEL_KETTLE_V3)
                || mDeviceStat.model.equals(UMGlobalParam.MODEL_KETTLE_V5) || mDeviceStat.model.equals(UMGlobalParam.MODEL_KETTLE_V6) || mDeviceStat.model.equals(UMGlobalParam.MODEL_KETTLE_V7)) {
            String uid = mSharedPreferences.getString("uid", "");
            long time = mSharedPreferences.getLong("time", 0);
            if (uid.equals("") && time == 0) {
                boolean flag = XmPluginHostApi.instance().getApiLevel() >= VERSION_LICENSE_ADD;
//                Log.d(TAG, "yinsi=" + flag);
                if (flag) {
                    lisenseInit();
                } else {
                    if (mDeviceStat.model.equals(UMGlobalParam.MODEL_KETTLE_V5)) {
                        mHostActivity.showUserLicenseDialog(activity().getResources().getString(R.string.um_license_title),
                                getResources().getString(R.string.um_license_name),
                                Html.fromHtml(getResources().getString(R.string.um_license)),
                                getResources().getString(R.string.um_conceal_name),
                                Html.fromHtml(getResources().getString(R.string.um_ko_conceal)),
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mSharedPreferences.edit().putString("uid", XmPluginHostApi.instance().getAccountId()).apply();
                                        mSharedPreferences.edit().putLong("time", System.currentTimeMillis()).apply();
                                    }
                                });
                    } else {
                        mHostActivity.showUserLicenseDialog(activity().getResources().getString(R.string.um_license_title),
                                getResources().getString(R.string.um_license_name),
                                Html.fromHtml(getResources().getString(R.string.um_license)),
                                getResources().getString(R.string.um_conceal_name),
                                Html.fromHtml(getResources().getString(R.string.um_conceal)),
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mSharedPreferences.edit().putString("uid", XmPluginHostApi.instance().getAccountId()).apply();
                                        mSharedPreferences.edit().putLong("time", System.currentTimeMillis()).apply();
                                    }
                                });
                    }
                }
            }
        }
        initAnimator();
        UMBluetoothManager.getInstance().readMcuVersion();
        UMBluetoothManager.getInstance().openEachRecordNotify();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_REFRESH_ICONS:
//                    String str = (String) msg.obj;
//                    log.d("@@@@@", "str:" + str);
                    setImagesBackground();
                    break;
            }
        }
    };

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initViews() {
        mSharedPreferences = activity().getSharedPreferences(String.valueOf(mDeviceStat.did), Context.MODE_PRIVATE);
        tvTitle = (TextView) findViewById(R.id.title_bar_title);

//        setTime = (VerticalTimeRulerView) findViewById(R.id.set_time);
        setTemp = (VerticalRulerView) findViewById(R.id.set_temp);
        tvStatus = (TextView) findViewById(R.id.tv_status);
        tvTemp = (MyTextView) findViewById(R.id.tv_temp);
        tvMode = (TextView) findViewById(R.id.tv_mode);
        tvDuration = (TextView) findViewById(R.id.tv_duration);
        tvKeepTemp = (TextView) findViewById(R.id.tv_keep_temp);
        tag1 = (TextView) findViewById(R.id.tag1);
        bluOff = (ImageView) findViewById(R.id.blu_off);

        v_top = findViewById(R.id.v_top);
        v_bottom = findViewById(R.id.v_bottom);

        rl_mode = (RelativeLayout) findViewById(R.id.rl_mode);
        rl_duration = (RelativeLayout) findViewById(R.id.rl_duration);
        rl_temp = (RelativeLayout) findViewById(R.id.rl_temp);

        backBn = (ImageView) findViewById(R.id.title_bar_return);
        barMore = (ImageView) findViewById(R.id.title_bar_more);

        ll_mode = (LinearLayout) findViewById(R.id.ll_mode);
        tvCoffee = (TextView) findViewById(R.id.tv_coffee);
        tvRice = (TextView) findViewById(R.id.tv_rice);
        tvTea = (TextView) findViewById(R.id.tv_tea);
        tvMilk = (TextView) findViewById(R.id.tv_milk);

        tvCoffee.setOnClickListener(this);
        tvRice.setOnClickListener(this);
        tvTea.setOnClickListener(this);
        tvMilk.setOnClickListener(this);

        backBn.setOnClickListener(this);
        barMore.setOnClickListener(this);

        v_top.setOnClickListener(this);
        v_bottom.setOnClickListener(this);
        rl_mode.setOnClickListener(this);
        rl_duration.setOnClickListener(this);
        rl_temp.setOnClickListener(this);

        ll_mode.setTag(false);
//        setTime.setTag(false);
        setTemp.setTag(false);

//        ll_mode.setAlpha(0);
//        setTime.setAlpha(0);
//        setTemp.setAlpha(0);

        ll_mode.setVisibility(View.GONE);
//        setTime.setVisibility(View.GONE);
        setTemp.setVisibility(View.GONE);

        bluStatus = XmBluetoothManager.getInstance().isBluetoothOpen();
//        log.d("@@@@@", "bluStatus:" + bluStatus);
        connState = XmBluetoothManager.getInstance().getConnectStatus(mDeviceStat.mac);
//        log.d("@@@@@", "connState:" + connState);

        if (bluStatus == false) {
            tvStatus.setText(getString(R.string.um_status_close));
            tag1.setText(getString(R.string.um_status_bluetooth_close));
            tvTemp.setVisibility(View.GONE);
            bluOff.setVisibility(View.VISIBLE);
            bluOff.setImageDrawable(getDrawable(R.drawable.blu_off));
            tvMode.setText(getString(R.string.um_cur_temp_default));
        } else if (bluStatus == true && (connState == STATE_UNKNOWN || connState == STATE_CONNECTING || connState == STATE_DISCONNECTED || connState == STATE_DISCONNECTING)) {
            tvStatus.setText(getString(R.string.um_status_close));
            tag1.setText(getString(R.string.un_status_disconnected));
            tvTemp.setVisibility(View.GONE);
            bluOff.setVisibility(View.VISIBLE);
            bluOff.setImageDrawable(getDrawable(R.drawable.blu_disconected));
            tvMode.setText(getString(R.string.um_cur_temp_default));
        }
        setUpView();
        initRulerView();
    }

    private void setUpView() {
        UMBluetoothManager.getInstance().init(activity(), mDeviceStat, new UMStatusInterface() {
            @Override
            public void onStatusDataReceive(byte[] data) {
                if (bluStatus == true && connState == STATE_CONNECTED) {
//                    log.d("@@@@@", "更新界面运行了吗？");
                    tag1.setText(getString(R.string.um_cur_temp));
                    tvTemp.setVisibility(View.VISIBLE);
                    bluOff.setVisibility(View.GONE);
                    refreshView(data);
                } else {
                }
            }

            @Override
            public void isOnlineChange(boolean isOnline) {
                mIsOnline = isOnline;
                log.d(TAG, "mIsOnline=" + mIsOnline);
                if (!mIsOnline) {
                    setOfflineView();
                }
            }
        });
    }

    private void initRulerView() {
//        setTemp.setUnit(" ℃");// 设置“保温温度”显示单位
        setTemp.setMax(90);// 设置最高保温温度
        setTemp.setMin(40);// 设置最低保温温度
        setTemp.setNumber(60);//
        setTemp.setInterval(5);// 设置5度为一个间隔
        setTemp.setTextOffset(10);//
        int cur = (mTempCustom - 40) * 30;
//        setTemp.setCurTemp(cur);

//        setTime.setUnit(" h");// 设置“保温时长”显示单位
//        setTime.setMax(120);// 设置最长保温时长
//        setTime.setMin(5);// 设置最短保温时长
//        setTime.setNumber(20);//
//        setTime.setInterval(4);// 设置0.5小时为一个间隔
//        setTime.setTextOffset(10);//
//        setTime.setRuleScrollEndListener(new VerticalRulerView.RulerScrollEndCallback() {
//            @Override
//            public void onRulerScrollEnd(int length, int value) {
//                float time = (float) value / (float) 10;
//                setKeepWarmTime(time);
//            }
//        });
        setTemp.setRuleScrollEndListener(new VerticalRulerView.RulerScrollEndCallback() {
            @Override
            public void onRulerScrollEnd(int length, int value) {
                setKeepWarmTemp(value);
            }
        });
    }

    // 设置背景“云彩图片”的色彩变化
    private void setImagesBackground() {
        if (bluStatus != true || connState != STATE_CONNECTED) {
            if (statusType != -1) {
                tvTemp.refresView(R.color.text_gray_start, R.color.text_gray_end);
                refreshBackground(R.drawable.icon_gray_bg);
                statusType = -1;
            }
        } else {

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
            }
        }
    }

    private void refreshBackground(int resId) {
        for (int i = 0; i < resIconIds.length; i++) {
            ((ImageView) findViewById(resIconIds[i])).setImageResource(resId);
        }
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

    private void setKeepWarmTime(float time) {
//        tvDuration.setText("" + time);
        Log.i("XXX", "time:" + time);
        UMBluetoothManager.getInstance().setKeepWarmTime(time);
        startTimer();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.title_bar_return:
                finish();
                break;
            case R.id.title_bar_more:
                ArrayList<IXmPluginHostActivity.MenuItemBase> menus = new ArrayList<>();
                // 添加“功能设置”选项
                IXmPluginHostActivity.StringMenuItem stringMenu = new IXmPluginHostActivity.StringMenuItem();
                stringMenu.name = getString(R.string.um_device_fun);
                menus.add(stringMenu);
                // 添加“通用设置”选项
                IXmPluginHostActivity.StringMenuItem stringMenuItem = new IXmPluginHostActivity.StringMenuItem();
                stringMenuItem.name = getString(R.string.um_common_setting);
                menus.add(stringMenuItem);

                Intent intent = new Intent();
                intent.putExtra("scence_enable", false);
                intent.putExtra("common_setting_enable", false);
                mHostActivity.openMoreMenu2(menus, true, MENU_REQUEST_CODE, intent);
                break;
            case R.id.v_top:
                hideViews(view);
                break;
            case R.id.v_bottom:
                hideViews(view);
                break;
            case R.id.rl_mode:
                if (mKeyChoose == 255) {
                    showWindow(ll_mode);
                } else if (mKeyChoose == 1 || mKeyChoose == 2) {
                    if (ll_mode.getVisibility() == View.GONE && setTemp.getVisibility() == View.GONE) {
//                        if (mlAlertDialog == null) {
                            LayoutInflater layoutInflater = LayoutInflater.from(activity());
                            view = layoutInflater.inflate(R.layout.um_dialog_settemp_tip, null);

                            TextView buttonCancel = (TextView) view.findViewById(R.id.button_cancel);
                            TextView buttonOk = (TextView) view.findViewById(R.id.button_confirm);
                            mlAlertDialog = new AlertDialog.Builder(activity()).setView(view).create();
//                            Window dialogWindow = mlAlertDialog.getWindow();
//                            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//                            lp.width = PhoneUtil.dipToPx(activity(), 400);
//                            dialogWindow.setAttributes(lp);

                            buttonCancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mlAlertDialog.dismiss();
                                }
                            });
                            buttonOk.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mlAlertDialog.dismiss();
                                    showWindow(ll_mode);
//                                    log.d("@@@@@", "模式选择框");
                                }
                            });
//                        }
                        mlAlertDialog.show();
                    }
                }
                break;
            case R.id.rl_duration:
//                setTime.resetMeasure();
//                showWindow(setTime);
                showTimerDialog((int)selectTimerValue * 10);
                break;
            case R.id.rl_temp:
                if (mKeyChoose == 255) {
//                    setTemp.resetMeasure();
                    showWindow(setTemp);
                } else if (mKeyChoose == 1 || mKeyChoose == 2) {
                    if (ll_mode.getVisibility() == View.GONE && setTemp.getVisibility() == View.GONE) {
//                        if (mlAlertDialog == null) {
                            LayoutInflater layoutInflater = LayoutInflater.from(activity());
                            view = layoutInflater.inflate(R.layout.um_dialog_settemp_tip, null);

                            TextView buttonCancel = (TextView) view.findViewById(R.id.button_cancel);
                            TextView buttonOk = (TextView) view.findViewById(R.id.button_confirm);
                            mlAlertDialog = new AlertDialog.Builder(activity()).setView(view).create();
                            buttonCancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mlAlertDialog.dismiss();
                                }
                            });
                            buttonOk.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mlAlertDialog.dismiss();
//                                    setTemp.resetMeasure();
                                    showWindow(setTemp);
//                                    log.d("@@@@@", "温度设置框");
                                }
                            });
//                        }
                        mlAlertDialog.show();
                    }
                }
                break;
            case R.id.tv_coffee:
                setKeepWarmTemp(90);
                hideViews(view);
                break;
            case R.id.tv_tea:
                setKeepWarmTemp(80);
                hideViews(view);
                break;
            case R.id.tv_rice:
                setKeepWarmTemp(70);
                hideViews(view);
                break;
            case R.id.tv_milk:
                setKeepWarmTemp(50);
                hideViews(view);
                break;
        }
    }

    private void setKeepWarmTemp(final int temp) {
        int flag = mKeyChoose;
        UMBluetoothManager.getInstance().onSetup(mHeatModel, temp);
        startTimer();
//        if (flag == UMGlobalParam.KEY_NULL) {
//            UMBluetoothManager.getInstance().onSetup(mHeatModel, temp);
//            startTimer();
//        } else if (flag == UMGlobalParam.KEY_BOIL) {
//            UMBluetoothManager.getInstance().onSetup(mHeatModel, temp);
//            startTimer();
//        } else
        if (flag == UMGlobalParam.KEY_KEEP_WARM && mTempCustom != temp) {
            tvKeepTemp.setText("" + mTempCustom);
            UMBluetoothManager.getInstance().onSetup(mHeatModel, temp);
            startTimer();
//            if (mlAlertDialog == null) {
//                LayoutInflater layoutInflater = LayoutInflater.from(activity());
//                View view = layoutInflater.inflate(R.layout.um_dialog_work_tip, null);
//
//                TextView buttonCancel = (TextView) view.findViewById(R.id.button_cancel);
//                TextView buttonOk = (TextView) view.findViewById(R.id.button_confirm);
//                mlAlertDialog = new AlertDialog.Builder(activity()).setView(view).create();
//                Window dialogWindow = mlAlertDialog.getWindow();
//                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//                lp.width = PhoneUtil.dipToPx(activity(), 400);
//                dialogWindow.setAttributes(lp);
//
//                buttonCancel.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        tvKeepTemp.setText("" + mTempCustom);
//                        mlAlertDialog.dismiss();
//                    }
//                });
//                buttonOk.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        UMBluetoothManager.getInstance().onSetup(mHeatModel, temp);
//                        mlAlertDialog.dismiss();
//                        startTimer();
//                        mIgnoreChange = true;
//                        switchButton2.setChecked(true);
//                        mIgnoreChange = false;
//                    }
//                });
//            }
//            mlAlertDialog.show();

//            mlAlertDialog = new MLAlertDialog.Builder(activity()).setMessage(getString(R.string.toast_keep_warm_temp_set))
//                    .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
////                            mUmTempSeekbarView.setTemp(mTempCustom);
//                            tvKeepTemp.setText("" + mTempCustom);
//                        }
//                    })
//                    .setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            UMBluetoothManager.getInstance().onSetup(mHeatModel, temp);
//                            startTimer();
//                        }
//                    }).create();
//            mlAlertDialog.show();
        }
    }

    private void showWindow(View view) {
        if (isMoving) return;
        if (ll_mode.getVisibility() == View.VISIBLE && ll_mode.getId() != view.getId()) {
            stopViewAnim(ll_mode);
            return;
        }
//        else if (setTime.getVisibility() == View.VISIBLE && setTime.getId() != view.getId()) {
//            stopViewAnim(setTime);
//            return;
//        }
        else if (setTemp.getVisibility() == View.VISIBLE && setTemp.getId() != view.getId()) {
            stopViewAnim(setTemp);
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
        }
//        else if (setTime.getVisibility() == View.VISIBLE && setTime.getId() != view.getId()) {
//            stopViewAnim(setTime);
//            return;
//        }
        else if (setTemp.getVisibility() == View.VISIBLE && setTemp.getId() != view.getId()) {
            stopViewAnim(setTemp);
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
//                if (!isGone(setTime)) {
//                    setTime.setTag(true);
//                }
//
//                if (!isGone(setTemp)) {
//                    setTemp.setTag(true);
//                }
////                if (ll_mode.getAlpha() != 0) {
////                    ll_mode.setAlpha(0);
////                }
////                if (setTime.getAlpha() != 0) {
////                    setTime.setAlpha(0);
////                }
////                if (setTemp.getAlpha() != 0) {
////                    setTemp.setAlpha(0);
////                }
//
//
////                if (ll_mode.getVisibility() == View.VISIBLE) {
////                    ll_mode.setVisibility(View.GONE);
////                    ll_mode.clearAnimation();
////                }
////                if (setTime.getVisibility() == View.VISIBLE) {
////                    setTime.setVisibility(View.GONE);
////                    setTime.clearAnimation();
////                }
////                if (setTemp.getVisibility() == View.VISIBLE) {
////                    setTemp.setVisibility(View.GONE);
////                    setTemp.clearAnimation();
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

//            if (setTime.getVisibility() == View.VISIBLE) {
//                setTime.setVisibility(View.GONE);
////                setTime.clearAnimation();
//            }

            if (setTemp.getVisibility() == View.VISIBLE) {
                setTemp.setVisibility(View.GONE);
//                setTemp.clearAnimation();
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
//        log.d("@@@@@", "========data:" + data);
        if (data == null) {
            log.d("@@@@@", "MSG_WHAT_STATUS_VARY,data null!");
            return;
        } else if (data.length < 7) {
            log.d("@@@@@", "MSG_WHAT_STATUS_VARY,data length is " + data.length + ",not correct!");
            return;
        }

        // 状态模式
        status = data[0];
//        log.d("@@@@@", "status = " + status);

        // 按键选择
        mKeyChoose = data[1] & 0xff;
//        log.d("@@@@@", "mKeyChoose = " + mKeyChoose);

        // 是否完成
        boolean isCompleted = false;
        if (data[2] == 0) {
            isCompleted = false;
        } else if (data[2] == 1) {
            isCompleted = true;
        }

        // 异常
        mError = data[3];
/*        mError = UMGlobalParam.ERROR_PARCH;
        status = UMGlobalParam.STATUS_ABNORMAL;
        setErrorText(mError);*/

        // 自定义温度
        int mTemp = data[4] & 0xff;
        mTempCustom = mTemp;
//        log.d("@@@@@", "mTempCustom = " + mTempCustom);

        // 实际温度显示
        int currentTemp = data[5] & 0xff;
//        log.d("@@@@@", "currentTemp = " + currentTemp);
        if (currentTemp < 0) {
            currentTemp = 0;
        } else if (currentTemp > 100) {
            currentTemp = 100;
        }
        tvTemp.setText("" + currentTemp);
        mCurrentTemp = currentTemp;

        // 自定义模式
/*        mHeatModel = data[6];
        boolean isBiol;
        if (mHeatModel == UMGlobalParam.MODEL_KEEP_WARM_BOIL) {
            isBiol = true;
        } else {
            isBiol = false;
        }*/

        // 保温消耗时间、煮沸模式特殊模式、保温时间
        float mSetTime = UMGlobalParam.MAX_KEEP_WARM_TIME;// 最大的保温时间
        boolean boilModeSelect = true;
        int mConsumeTime = 0;

        if (data.length >= 11) {
            mConsumeTime = data[7] & 0xff + (data[8] & 0xff) * 256;
//            log.d(TAG, "mConsumeTime=" + mConsumeTime);
            if (data[9] == 0) {
                boilModeSelect = false;
//                log.d("@@@@@", "boilModeSelect11111 = " + boilModeSelect);
            } else if (data[9] == 1) {
                boilModeSelect = true;
//                log.d("@@@@@", "boilModeSelect22222 = " + boilModeSelect);
            }
//            log.d("@@@@@", "boilModeSelect = " + boilModeSelect);
            mSetTime = (float) ((int) (data[10] & 0xff) * 0.5);
            if (mSetTime > UMGlobalParam.MAX_KEEP_WARM_TIME) {
                mSetTime = UMGlobalParam.MAX_KEEP_WARM_TIME;
            } else if (mSetTime < UMGlobalParam.MIN_KEEP_WARM_TIME) {
                mSetTime = UMGlobalParam.MIN_KEEP_WARM_TIME;
            }

            // 提壶记忆功能开关
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
//        log.d("@@@@@", "stringhuoqu:" + string);
        if (!TextUtils.isEmpty(string)) {
//            log.d("@@@@@", "string:" + string);
            mHandler.sendMessage(mHandler.obtainMessage(MSG_REFRESH_ICONS, string));
//            setImagesBackground(string);
        } else {
            String statusStr = getString(R.string.um_status_net_abnormal);
//            log.d("@@@@@", "statusStr:" + statusStr);
            mHandler.sendMessage(mHandler.obtainMessage(MSG_REFRESH_ICONS, statusStr));
//            setImagesBackground(string);
        }

        // 副标题模式设置
        if (mKeyChoose == 255 && status == 0) {
            tvStatus.setText(getString(R.string.um_status_close));
        } else if (mKeyChoose == 1) {// 按下“煮沸”键
            if (status == 1 && currentTemp < 98) {
                tvStatus.setText(getString(R.string.um_status_heating));
            } else if (status == 1 && 98 <= currentTemp && currentTemp <= 100) {
                tvStatus.setText(getString(R.string.um_status_boiling));
            }
        } else if (mKeyChoose == 2) {// 按下“保温”键
            if (status == 1) {
                tvStatus.setText(getString(R.string.um_status_heating));
                if (status == 1 && 98 <= currentTemp && currentTemp <= 100) {
                    tvStatus.setText(getString(R.string.um_status_boiling));
                }
            } else if (status == 2 || status == 3) {
                tvStatus.setText(getString(R.string.um_status_keep_warm));
            }
        }

        tvMode.setText(getModeString(mTemp));
        tvDuration.setText("" + mSetTime);
        Log.i("XXX", "mSetTime:" + mSetTime);
        selectTimerValue = mSetTime;
        Log.i("XXX", "mSetTime:" + mSetTime + " selectTimerValue:" + selectTimerValue);

        tvKeepTemp.setText("" + mTemp);
//        if (!isDataRefresh) {
//        setTime.setNumber(((int) mSetTime) * 10);
        setTemp.setNumber(mTemp);
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

    // 获取几种默认模式
    private String getModeString(int temp) {
        String mode;
//        mode =  getString(R.string.um_cur_temp_default);
        if (temp == 50) {
            mode = getString(R.string.text_milk);// 奶粉
        } else if (temp == 70) {
            mode = getString(R.string.text_cereal);// 婴儿米粉
        } else if (temp == 80) {
            mode = getString(R.string.text_tea);// 白茶
        } else if (temp == 90) {
            mode = getString(R.string.text_coffee);// 咖啡
        } else {
            mode = getString(R.string.um_custom_mode);// 自定义
        }
        return mode;
    }

    // 副标题状态显示
    private String getStatusString(boolean isOnline, int status, boolean isCompleted, int currentTemp, int setTemp, int mConsumeTime) {
        String statusStr = getString(R.string.um_status_close);

        if (bluStatus == true && connState == STATE_CONNECTED) {
//        if (isOnline) {
            if (status == UMGlobalParam.STATUS_IDLE) {// “空闲中”状态
                statusStr = getString(R.string.um_status_close);
//                log.d("@@@@@", "getStatusString1111----statusStr=" + statusStr);
            } else if (status == UMGlobalParam.STATUS_HEATING) {// “加热中”状态
                statusStr = getString(R.string.um_status_heating);
//                log.d("@@@@@", "getStatusString2222----statusStr=" + statusStr);
            } else if (status == UMGlobalParam.STATUS_KEEP_WARM_NOT__BOIL) {// “保温中”状态（未煮沸）
                statusStr = getString(R.string.um_status_keep_warm);
//                log.d("@@@@@", "getStatusString3333----statusStr=" + statusStr);
            } else if (status == UMGlobalParam.STATUS_KEEP_WARM_BOIL) {// “保温中”状态（煮沸）
                statusStr = getString(R.string.um_status_keep_warm);
//                log.d("@@@@@", "getStatusString4444----statusStr=" + statusStr);
            } else {
                statusStr = getString(R.string.um_status_abnormal);
            }
        } else if (bluStatus != true || connState != STATE_CONNECTED) {
            statusStr = getString(R.string.un_status_disconnected);
        }
//        log.d("@@@@@", "getStatusString5555----statusStr=" + statusStr);
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
//                    log.d(TAG, "menu=" + name);
                    if (!TextUtils.isEmpty(name)) {
                        if (name.equals(getString(R.string.um_common_setting))) {
                            openSubMenu1();
                        } else if (name.equals(getString(R.string.um_device_fun))) {
                            startActivity(null, UMDeviceFunctionActivity.class.getName());
                        }
                    }
                }
                break;
            case REQUEST_MENUS_SECOND:
                // 撤销用户协议
                if (data != null && data.getExtras() != null) {
                    String result_data = data.getStringExtra("result_data");
                    if ("removedLicense".equals(result_data)) {
                        mSharedPreferences.edit().putString("uid", "").apply();
                        mSharedPreferences.edit().putLong("time", 0).apply();
                        UMGlobalParam.getInstance().clear();
                        activity().finish();
                    }
//                    isOnActivity = 0;
//                    Log.d("@@@@@", "isOnActivity = " + isOnActivity);
//                    UMBluetoothManager.getInstance().isOnMainActivity(isOnActivity);
                }
                break;
        }
    }

//    private void openSubMenu() {
//        ArrayList<IXmPluginHostActivity.MenuItemBase> item = new ArrayList<IXmPluginHostActivity.MenuItemBase>();
//        IXmPluginHostActivity.IntentMenuItem intentMenuItem1 = new IXmPluginHostActivity.IntentMenuItem();
//        intentMenuItem1.name = getString(R.string.um_device_fun);
//        intentMenuItem1.intent = mHostActivity.getActivityIntent(activity().getPackageName(), UMDeviceFunctionActivity.class.getName());
////        intentMenuItem1.intent.putExtra("status", mMemoryStatus);
//        item.add(intentMenuItem1);
//
////        IXmPluginHostActivity.IntentMenuItem intentMenuItem = new IXmPluginHostActivity.IntentMenuItem();
////        intentMenuItem.name=getString(R.string.um_device_fun);
////        intentMenuItem.intent= mHostActivity.getActivityIntent(activity().getPackageName(),UMDeviceFunctionActivity.class.getName());
//    }

    //传入升级管理
    private void openSubMenu1() {
        ArrayList<IXmPluginHostActivity.MenuItemBase> items = new ArrayList<IXmPluginHostActivity.MenuItemBase>();
        IXmPluginHostActivity.IntentMenuItem intentMenuItem = new IXmPluginHostActivity.IntentMenuItem();
        intentMenuItem.name = getString(R.string.um_device_info);
        intentMenuItem.intent = mHostActivity.getActivityIntent(null, UMDeviceInfoActivity.class.getName());
        intentMenuItem.intent.putExtra("mac", mDeviceStat.mac);
        items.add(intentMenuItem);

        IXmPluginHostActivity.BleMenuItem bleMenu = IXmPluginHostActivity.BleMenuItem.newUpgraderItem(new UMBlueToothUpgrader());
        items.add(bleMenu);
        Intent commonSettingIntent = new Intent();

        if (XmPluginHostApi.instance().getApiLevel() >= VERSION_LICENSE_ADD) {
            if (mDeviceStat.model.equals(UMGlobalParam.MODEL_KETTLE_V2) || mDeviceStat.model.equals(UMGlobalParam.MODEL_KETTLE_V3)
                    || mDeviceStat.model.equals(UMGlobalParam.MODEL_KETTLE_V5)
                    || mDeviceStat.model.equals(UMGlobalParam.MODEL_KETTLE_V7)) {
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
            if (mDeviceStat.model.equals(UMGlobalParam.MODEL_KETTLE_V2) || mDeviceStat.model.equals(UMGlobalParam.MODEL_KETTLE_V3)
                    || mDeviceStat.model.equals(UMGlobalParam.MODEL_KETTLE_V7)) {
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
        isOnActivity = 0;
        Log.d("@@@@@", "isOnActivity = " + isOnActivity);
        UMBluetoothManager.getInstance().isOnMainActivity(isOnActivity);
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
////                if (setTime.getVisibility() == View.VISIBLE) {
////                    setTime.setVisibility(View.GONE);
////                }
////                if (setTemp.getVisibility() == View.VISIBLE) {
////                    setTemp.setVisibility(View.GONE);
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
//        log.d("@@@@@", "==========TranslationX:" + view.getTranslationX() + "\n" + "X:" + view.getX());
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

    private void lisenseInit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                copyLicenseFile();
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(MSG_WHAT_INIT_LICENSE);
                }
            }
        }).start();
    }

    /***
     * 复制隐私协议文件到sdcard里
     */
    private void copyLicenseFile() {
        String licenseFileName = "license.html";
        String privacyFileName = "private.html";
        if (mDeviceStat.model.equals(UMGlobalParam.MODEL_KETTLE_V7)) {
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

    @Override
    public void onResume() {
        super.onResume();
        UMBluetoothManager.getInstance().runUpdateInfo();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    private void showTimerDialog(int progress) {
        if (timerDialog == null) {
            View view = LayoutInflater.from(this).inflate(R.layout.keeptime_picker_dialog, null);
            timerDialog = new MLAlertDialog.Builder(activity()).setView(view).show();
            Button cancelButton = (Button) view.findViewById(R.id.cancel_btn);
            Button certainButton = (Button) view.findViewById(R.id.certain_btn);
            selectedTimer = (TextView)view.findViewById(R.id.selectedTimer);
            timerPickerView = (SeekBar) view.findViewById(R.id.timer_picker);
            timerPickerView.setProgress(progress);
            setSelectedTimerTextView(progress);
            timerDialog.setCancelable(true);   //设置按钮是否可以按返回键取消,false则不可以取消
            timerDialog.setCanceledOnTouchOutside(true);  //设置弹出框失去焦点是否隐藏,即点击屏蔽其它地方是否隐藏
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (timerDialog != null) {
                        timerDialog.dismiss();
                    }
                }
            });
            certainButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (timerDialog != null) {
                        timerDialog.dismiss();
                    }

                    String timerValue = selectedTimer.getText().toString();
                    if (!timerValue.equals("--") && !TextUtils.isEmpty(timerValue)) {
                        selectTimerValue = Float.parseFloat(timerValue);
                        setKeepWarmTime(selectTimerValue);
                    }
                }
            });
            timerPickerView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    setSelectedTimerTextView(progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            timerDialog.show();
        } else {
            timerDialog.show();
        }
    }

    private void setSelectedTimerTextView(int progress) {
        int tenPlace = progress / 10;
        if (tenPlace <= 0) {
            selectTimerValue = 0.5f;
            selectedTimer.setText("0.5");
        } else {
            int del = progress % (tenPlace * 10);
            int digitPlace = del < 5 ? 0 : 5;
            selectTimerValue = (float)(tenPlace * 10 + digitPlace) / 10;
            selectedTimer.setText(tenPlace + "." + digitPlace);
        }
    }
}
