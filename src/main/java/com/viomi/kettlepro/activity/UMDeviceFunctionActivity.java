package com.viomi.kettlepro.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.viomi.kettlepro.UMGlobalParam.HeatModel;
import com.viomi.kettlepro.R;
import com.viomi.kettlepro.UMGlobalParam;
import com.viomi.kettlepro.dev.UMBluetoothManager;
import com.viomi.kettlepro.interfaces.UMStatusInterface;
import com.viomi.kettlepro.utils.PhoneUtil;
import com.viomi.kettlepro.view.EaseSwitchButton;
import com.viomi.kettlepro.view.UMSwitchButton;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by young2 on 2016/12/10.
 */

public class UMDeviceFunctionActivity extends UMBaseActivity {
    private final static String TAG = UMDeviceFunctionActivity.class.getSimpleName();
    private static final int MSG_REFRESH_VIEWS = 100;
    private Timer mTimer;
    private TimerTask mTimeTask;
    private boolean mDataRefresh = true;//数据是否刷新，水壶500ms左右，上报一次数据，设置后1000ms才开始读数据

    private boolean modeChange = false;//点击模式改变
    private @HeatModel
    int modelCustem;//设定模式
    private boolean isOpen = false;
    private boolean isBoilOpen = false;
    private AlertDialog mLiftUpWarnDialog;
    private boolean mIgnoreChange;

    private boolean flag1 = false;

    private boolean flag2 = false;

    private boolean flag3 = false;
    private int tempCustom;//设置温度
    private RadioGroup radioGroup;
    private RadioButton radioButtonNotBoil, radioButtonBiol;

    private RelativeLayout rl_boil, rl_keep_warm;
    private UMSwitchButton switchButton1, switchButton2;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_REFRESH_VIEWS:
                    refreshView();
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        layoutId = R.layout.um_activity_device_fuction;
        super.onCreate(savedInstanceState);
        TextView textView = (TextView) findViewById(R.id.title);
        textView.setText(getString(R.string.um_device_fun));
        View backView = findViewById(R.id.back);
        backView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        initViews();
    }

    private void initViews() {
//        status = getIntent().getIntExtra("status", 0);
        radioGroup = (RadioGroup) findViewById(R.id.setting_layout);

        radioButtonNotBoil = (RadioButton) findViewById(R.id.notBoil);
        radioButtonBiol = (RadioButton) findViewById(R.id.boil);

        switchButton1 = (UMSwitchButton) findViewById(R.id.iv_switch1);
        switchButton2 = (UMSwitchButton) findViewById(R.id.iv_switch2);

        rl_boil = (RelativeLayout) findViewById(R.id.rl_boil);
        rl_keep_warm = (RelativeLayout) findViewById(R.id.rl_keep_warm);


//        if (UMBluetoothManager.getInstance().getBoilModeSet() == UMGlobalParam.BOIL_MODE_COMMON) {
//            switchButton1.setChecked(false);
//        } else if (UMBluetoothManager.getInstance().getBoilModeSet() == UMGlobalParam.BOIL_MODE_SPECIAL) {
//            switchButton1.setChecked(true);
//        }

        switchButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    UMBluetoothManager.getInstance().setBoilModeSet(UMGlobalParam.BOIL_MODE_SPECIAL);
                } else {
                    UMBluetoothManager.getInstance().setBoilModeSet(UMGlobalParam.BOIL_MODE_COMMON);
                }
            }
        });

/*        switchButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
//                    UMBluetoothManager.getInstance().setMemoryStatusSet(UMGlobalParam.MEMORY_STATUS_SPECIAL);
                    UMBluetoothManager.getInstance().onSetup(0x03, 1);
                } else {
//                    UMBluetoothManager.getInstance().setMemoryStatusSet(UMGlobalParam.MEMORY_STATUS_COMMON);
                    UMBluetoothManager.getInstance().onSetup(0x03, 0);
                }
            }
        });*/

//        switchButton2.setOnCheckedChangeListener(this);



        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                modeChange = true;
                int id = radioGroup.getCheckedRadioButtonId();
                switch (id) {
                    case R.id.boil:
                        modelCustem = UMGlobalParam.MODEL_KEEP_WARM_BOIL;
                        setKettleValue(modelCustem, tempCustom);
                        radioButtonBiol.setChecked(true);
                        radioButtonNotBoil.setChecked(false);
                        rl_boil.setVisibility(View.VISIBLE);
                        break;
                    case R.id.notBoil:
                        modelCustem = UMGlobalParam.MODEL_KEEP_WARM_NOT_BOIL;
                        setKettleValue(modelCustem, tempCustom);
                        radioButtonBiol.setChecked(false);
                        radioButtonNotBoil.setChecked(true);
                        rl_boil.setVisibility(View.GONE);
                        break;
                }
            }
        });

//        radioButtonBiol.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                if (modeChange) {
//                    modeChange = false;
//                    modelCustem = UMGlobalParam.MODEL_KEEP_WARM_BOIL;
//                    setKettleValue(modelCustem, tempCustom);
//                    radioButtonBiol.setChecked(true);
//                    radioButtonNotBoil.setChecked(false);
//                    rl_boil.setVisibility(View.VISIBLE);
//                }
//            }
//        });
//
//        radioButtonNotBoil.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                if (modeChange) {
//                    modeChange = false;
//                    modelCustem = UMGlobalParam.MODEL_KEEP_WARM_NOT_BOIL;
//                    setKettleValue(modelCustem, tempCustom);
//                    radioButtonBiol.setChecked(false);
//                    radioButtonNotBoil.setChecked(true);
//                    rl_boil.setVisibility(View.GONE);
//                }
//            }
//        });
//        UMBluetoothManager.getInstance().setCustomView(radioButtonBiol, radioButtonNotBoil, switchButton1, switchButton2);

        UMBluetoothManager.getInstance().setStatusListener(new UMStatusInterface() {
            @Override
            public void onStatusDataReceive(byte[] data) {
                bindDatas(data);
            }

            @Override
            public void isOnlineChange(boolean isOnline) {

            }
        });
    }

//    private RadioGroup.OnCheckedChangeListener listener=new RadioGroup.OnCheckedChangeListener() {
//        @Override
//        public void onCheckedChanged(RadioGroup radioGroup, int i) {
//            int id= radioGroup.getCheckedRadioButtonId();
//              switch (){
//
//              }
//        }
//    };

    private void refreshView() {
        if (modelCustem == UMGlobalParam.MODEL_KEEP_WARM_NOT_BOIL) {
            radioButtonBiol.setChecked(false);
            radioButtonNotBoil.setChecked(true);
            rl_boil.setVisibility(View.GONE);
        } else {
            radioButtonBiol.setChecked(true);
            radioButtonNotBoil.setChecked(false);
            rl_boil.setVisibility(View.VISIBLE);
        }
        radioGroup.clearFocus();

        if (isBoilOpen) {
            switchButton1.setChecked(true);
        } else {
            switchButton1.setChecked(false);
        }
        if (isOpen) {
            switchButton2.setChecked(true);
        } else {
            switchButton2.setChecked(false);
        }

        switchButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (!mIgnoreChange) {

                    if (isChecked) {
                        if (UMBluetoothManager.getInstance().getCurrentVersion().length() != 0
                                && UMBluetoothManager.getInstance().getCurrentVersion().compareToIgnoreCase("6.2.0.8") <= 0) {
                            Toast.makeText(activity(), getString(R.string.text_kettle_lift_up_upgrade_tips), Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (mLiftUpWarnDialog == null) {
                            LayoutInflater layoutInflater = LayoutInflater.from(activity());
                            View view = layoutInflater.inflate(R.layout.um_dialog_lift_up, null);

                            TextView buttonCancel = (TextView) view.findViewById(R.id.button_cancel);
                            TextView buttonOk = (TextView) view.findViewById(R.id.button_confirm);
                            mLiftUpWarnDialog = new AlertDialog.Builder(activity()).setView(view).create();
                            Window dialogWindow = mLiftUpWarnDialog.getWindow();
                            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                            lp.width = PhoneUtil.dipToPx(activity(), 600);
                            dialogWindow.setAttributes(lp);

                            buttonCancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    UMBluetoothManager.getInstance().onSetup(0x03, 0);
                                    mLiftUpWarnDialog.dismiss();
                                }
                            });
                            buttonOk.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    UMBluetoothManager.getInstance().onSetup(0x03, 1);
                                    mLiftUpWarnDialog.dismiss();
                                    startTimer();
                                    mIgnoreChange = true;
                                    switchButton2.setChecked(true);
                                    mIgnoreChange = false;
                                }
                            });
                        }
                        mLiftUpWarnDialog.show();
                    } else {
                        UMBluetoothManager.getInstance().onSetup(0x03, 0);
                        startTimer();
                    }
                }
            }
        });
    }

    private void bindDatas(byte[] data) {
        Log.i("info", "============================bindDatas()");
        if (data == null) {
            Log.e(TAG, "MSG_WHAT_STATUS_VARY,data null!");
            return;
        }
        if (data.length < 7) {
            Log.e(TAG, "MSG_WHAT_STATUS_VARY,data length is " + data.length + ",not correct!");
            return;
        }
        //自定义温度
        int mTemp = data[4] & 0xff;
        tempCustom = mTemp;
        //自定义模式
        modelCustem = data[6];
        if (data.length >= 11) {
            if (data[9] == 0) {
                isBoilOpen = false;
            } else if (data[9] == 1) {
                isBoilOpen = true;
            }
            //提壶记忆功能开关
            if (data.length >= 12) {
                if (data[11] == 0) {
                    isOpen = false;
                } else if (data[11] == 1) {
                    isOpen = true;
                }
            }
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_REFRESH_VIEWS));
        if (!mDataRefresh) {
            return;
        }
    }

    // 设定水壶的温度和工作模式 参照{@link HeatModel}
    public void setKettleValue(int mode, int temp) {
        Log.d(TAG, "setKettleValue,mode=" + mode + ",temp=" + temp);
        UMBluetoothManager.getInstance().onSetup(mode, temp);
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
    public void onResume() {
        super.onResume();
//        UMBluetoothManager.getInstance().readBoilModeSet();
//        tempCustom = UMBluetoothManager.getInstance().getTempSet();
//        modelCustem = UMBluetoothManager.getInstance().getHeatModel();
//        Log.d(TAG, "tempCustom=" + tempCustom + ",modelCustem=" + modelCustem + "\n");
//        refreshView();
    }

}
