package com.viomi.kettlepro.dev;

import com.quintic.libota.BluetoothLeInterface;
import com.xiaomi.smarthome.bluetooth.XmBluetoothManager;

/**
 * Created by young2 on 2016/3/18.
 */
public class UpdateInstance extends BluetoothLeInterface
{
    @Override
    public boolean bleInterfaceInit()
    {
        //return super.bleInterfaceInit(bluetoothGatt);
        if(XmBluetoothManager.getInstance()!=null){
            return true;
        }
        return false;

    }

    @Override
    public boolean writeCharacteristic(byte[] data) {
        UMBluetoothManager.getInstance().writeOtaUpdateCharacter(data);
        return true;
    }

    @Override
    public boolean setCharacteristicNotification(boolean enabled) {
        if(enabled){
            UMBluetoothManager.getInstance().openOtaUpdateNotify();
        }else {
            UMBluetoothManager.getInstance().closeOtaUpdateNotify();
        }
        return true;
    }


}
