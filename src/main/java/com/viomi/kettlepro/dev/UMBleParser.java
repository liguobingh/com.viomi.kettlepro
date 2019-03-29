package com.viomi.kettlepro.dev;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by young2 on 2016/1/3.
 */
public class UMBleParser {
    public static final byte[] RECORD_STOP_BYTES={(byte) 0xff,(byte)0xff,(byte)0xff,(byte)0xff};//用水记录上报完成位
 

    /***
     * 获取注水记录index
     * @param data 煮水记录返回字节
     * @return index
     */
    public static int getRecordIndex(byte[] data){
        if(data==null||data.length<RECORD_STOP_BYTES.length){
            return -1;
        }
        return (0xFF&data[1])*256+0xFF&data[0];
    }


    /***
     * 解析煮水记录
     * @param data 接收到的字节数组记录
     * @return 返回解析后的数据
     */
    public static UMKettleEachRecord parseEachRecord(String mac,byte[] data){

        if(data==null||data.length!=16){
            return null;
        }

        try{
            UMKettleEachRecord record=new UMKettleEachRecord();
            int position=0;
            record.index=(0xFF&data[position+1])*256+0xFF&data[position];

            position+=2;
            record.initialTemp=0xff&data[position];
            position++;
            record.boilTemp=0xff&data[position];
            position++;
            record.setTemp=0xff&data[position];
            position++;
            record.workMode=0xff&data[position];
            position++;
            record.errorCode=0xff&data[position];
            position++;
            record.cuteOffTime=0xff&data[position];
            position++;
            record.heatTime=(0xFF&data[position+1])*256+0xFF&data[position];
            position+=2;
            record.holdTime=(0xFF&data[position+1])*256+0xFF&data[position];

            SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date=new Date();
            record.time=format.format(date);
            record.mac=mac;

            return record;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
