package com.viomi.kettlepro.dev;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.quintic.libota.bleGlobalVariables;
import com.quintic.libota.otaManager;
import com.viomi.kettlepro.interfaces.UMOtaInterface;
import com.viomi.kettlepro.utils.log;
import com.xiaomi.smarthome.bluetooth.BleUpgrader;
import com.xiaomi.smarthome.bluetooth.Response;
import com.xiaomi.smarthome.bluetooth.XmBluetoothManager;
import com.xiaomi.smarthome.device.api.XmPluginHostApi;

/**
 * Created by young2 on 2016/3/19.
 */
public class UMBlueToothUpgrader extends BleUpgrader {
    private final static String TAG="UMBlueToothUpgrader";
    private final  static  int mDelayTime=100;
    private otaManager updateManager=null;
    private  int mProgress=0;
    private boolean mStopUpdate=false;
    private String filePath="";//固件下载保存路径
    private Thread updateThread=null;

    public UMBlueToothUpgrader(){
        updateManager=UMBluetoothManager.getInstance().getOtaManager();
        UMBluetoothManager.getInstance().onOtaFail(new UMOtaInterface() {
            @Override
            public void otaFail() {
                updateManager.otaStop();
                mStopUpdate=true;
            }
        });
    }

    @Override
    public String getCurrentVersion() throws RemoteException {
      return UMBluetoothManager.getInstance().getUMUpgraderMsg().currentMcuVersion;

    }

    @Override
    public String getLatestVersion() throws RemoteException {
        return UMBluetoothManager.getInstance().getUMUpgraderMsg().latestMcuVersion;
    }

    @Override
    public String getUpgradeDescription() throws RemoteException {
        return UMBluetoothManager.getInstance().getUMUpgraderMsg().upgradeDescription;
    }

    @Override
    public void startUpgrade() throws RemoteException {
        Log.i(TAG,"startUpgrade!");
        startDownload();
    }

    @Override
    public void onActivityCreated(Bundle bundle) throws RemoteException {
        String currentVersion=UMBluetoothManager.getInstance().getUMUpgraderMsg().currentMcuVersion;
        String latestVersion=UMBluetoothManager.getInstance().getUMUpgraderMsg().latestMcuVersion;
        if(currentVersion.compareToIgnoreCase(latestVersion)>=0){
            showPage(XmBluetoothManager.PAGE_CURRENT_LATEST,null);
        }else {
            showPage(XmBluetoothManager.PAGE_CURRENT_DEPRECATED,null);
        }

    }

    private final Handler mHandler =new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what){
                    case XmBluetoothManager.PAGE_UPGRADING:
                        int process= (int) msg.obj;
                        Bundle bundle=new Bundle();
                        bundle.putInt(XmBluetoothManager.EXTRA_UPGRADE_PROCESS,process);
                        showPage(XmBluetoothManager.PAGE_UPGRADING,bundle);
                        break;
                    case XmBluetoothManager.PAGE_UPGRADE_SUCCESS:
                        showPage(XmBluetoothManager.PAGE_UPGRADE_SUCCESS,null);
                        UMBluetoothManager.getInstance().getUMUpgraderMsg().currentMcuVersion=UMBluetoothManager.getInstance().getUMUpgraderMsg().latestMcuVersion;
                        break;
                    case XmBluetoothManager.PAGE_UPGRADE_FAILED:
                        showPage(XmBluetoothManager.PAGE_UPGRADE_FAILED,null);
                        break;
                }
            }catch (NullPointerException e){
                Log.i(TAG,"handleMessage,NullPointeerException！");
            }


        }
    };

    private void startDownload(){
        XmPluginHostApi.instance().downloadBleFirmware(UMBluetoothManager.getInstance().getUMUpgraderMsg().url, new Response.BleUpgradeResponse() {
            @Override
            public void onProgress(int i) {
                log.d(TAG,"downloadBleFirmware,onProgress="+i);
                mProgress=i*20/100;
                Message message=mHandler.obtainMessage();
                message.what=XmBluetoothManager.PAGE_UPGRADING;
                message.obj=mProgress;
                mHandler.sendMessageDelayed(message,mDelayTime);
            }

            @Override
            public void onResponse(int i, String s) {
                Log.i(TAG,"downloadBleFirmware,onResponse,code="+i+",path="+s);
                if(i==0){
                    filePath=s;
                    mProgress=50;
                    Message message=mHandler.obtainMessage();
                    message.what=XmBluetoothManager.PAGE_UPGRADING;
                    message.obj=mProgress;
                    mHandler.sendMessageDelayed(message,mDelayTime);
                    updateProgress();
                }else {
                    mHandler.sendEmptyMessageDelayed(XmBluetoothManager.PAGE_UPGRADE_FAILED,mDelayTime);
                }

            }
        });
    }

    public void updateProgress()
    {
        UpdateInstance ins=new UpdateInstance();
        ins.bleInterfaceInit();
        Log.i(TAG, "ota update start!");
        if(updateManager.otaStart(filePath,ins)== bleGlobalVariables.otaResult.OTA_RESULT_SUCCESS)
        {
            Log.i(TAG, "ota update start sucess!");
            mStopUpdate=false;
            updateThread=new Thread(update);
            updateThread.start();
        }else
        {
            Log.e(TAG, "ota update start fail!");
            mHandler.sendEmptyMessageDelayed(XmBluetoothManager.PAGE_UPGRADE_FAILED,mDelayTime);
        }

    }

    Runnable update=new Runnable()
    {
        public void run()
        {
            int[] extra=new int[8];
            while(!mStopUpdate)
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(!Thread.currentThread().isInterrupted())
                {
                    bleGlobalVariables.otaResult ret=updateManager.otaGetProcess(extra);
                    if(ret== bleGlobalVariables.otaResult.OTA_RESULT_SUCCESS){
                        int percent=extra[0];
                        Log.d(TAG, "otaResult，percent="+percent);
                        if(mProgress>=100){
                            mHandler.sendEmptyMessageDelayed(XmBluetoothManager.PAGE_UPGRADE_SUCCESS,mDelayTime);
                        }else {
                            mProgress=20+percent*80/100;
                            Message message=mHandler.obtainMessage();
                            message.what=XmBluetoothManager.PAGE_UPGRADING;
                            message.obj=mProgress;
                            mHandler.sendMessageDelayed(message,mDelayTime);
                        }
                    }
                    else
                    {
                        Log.e(TAG, "otaResult，fail!");
                        updateManager.otaStop();
                        mStopUpdate=true;
                        mHandler.sendEmptyMessageDelayed(XmBluetoothManager.PAGE_UPGRADE_FAILED,100);
                    }
                }
            }
        }
    };
}
