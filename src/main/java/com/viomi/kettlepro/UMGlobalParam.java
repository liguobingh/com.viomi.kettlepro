package com.viomi.kettlepro;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.IntDef;
import android.util.DisplayMetrics;

import com.xiaomi.smarthome.device.api.XmPluginHostApi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class UMGlobalParam {

	public static final String MODEL_KETTLE_V1="yunmi.kettle.v1";//大陆版和香港版本
	public static final String MODEL_KETTLE_V2="yunmi.kettle.v2";//国际版
	public static final String MODEL_KETTLE_V3="yunmi.kettle.v3";//台湾版
	public static final String MODEL_KETTLE_V5="yunmi.kettle.v5";//韩国版
	public static final String MODEL_KETTLE_V6="yunmi.kettle.v6";//

	public static boolean isSaveStatusData=false;//是否保存状态数据
	public static float density=1;

	public static final int STATUS_IDLE=0;//空闲中
	public static final int STATUS_HEATING=1;//加热中
	public static final int STATUS_KEEP_WARM_BOIL=2;//煮沸保温中	
	public static final int STATUS_KEEP_WARM_NOT__BOIL=3;//未煮沸保温中
	public static final int STATUS_ABNORMAL=4;//异常
	
    @IntDef({STATUS_IDLE, STATUS_HEATING,STATUS_KEEP_WARM_BOIL,STATUS_KEEP_WARM_NOT__BOIL,STATUS_ABNORMAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {}
    
	public static final int MODEL_KEEP_WARM_BOIL=0;//煮沸后在当前温度保温
	public static final int MODEL_KEEP_WARM_NOT_BOIL=1;//加热至当前温度保温
	public static final int MODEL_BOIL=2;//自然煮沸
	@IntDef({MODEL_KEEP_WARM_BOIL, MODEL_KEEP_WARM_NOT_BOIL, MODEL_BOIL})
	@Retention(RetentionPolicy.SOURCE)
	public @interface HeatModel{}//烧水模式枚举
	
	public static final int PROCESS_NOT_COMPLETE=0;//未完成
	public static final int PROCESS_COMPLETE=1;//已完成
	@IntDef({PROCESS_NOT_COMPLETE, PROCESS_COMPLETE})
	@Retention(RetentionPolicy.SOURCE)
	public @interface Process{}//完成进度枚举
	
	public static final int KEY_BOIL=1;//煮沸按键
	public static final int KEY_KEEP_WARM=2;//保温按键
	public static final int KEY_NULL=0xff;//无按下，空闲
	@IntDef({KEY_BOIL, KEY_KEEP_WARM,KEY_NULL})
	@Retention(RetentionPolicy.SOURCE)
	public @interface KeyChoose{}//完成进度枚举
	
	public static final int FUNCTION_KEEP_WARM_BOIL=0;//煮沸后在当前温度保温
	public static final int FUNCTION_KEEP_WARM_NOT_BOIL=1;//加热至当前温度保温
	public static final int  FUNCTION_TIME_SET=2;//设定时间
	public static final int  FUNCTION_BUZZER_SET=3;//蜂鸣器设置
	@IntDef({FUNCTION_KEEP_WARM_BOIL, FUNCTION_KEEP_WARM_NOT_BOIL, FUNCTION_TIME_SET,FUNCTION_BUZZER_SET})
	@Retention(RetentionPolicy.SOURCE)
	public @interface Function{}//功能设置枚举
	
	public static final int ERROR_NOT=0;//正常
	public static final int ERROR_HEAT=1;//加热故障
	public static final int ERROR_SENSOR=2;//温度传感器故障
	public static final int ERROR_PARCH=4;//干烧
	@IntDef({ERROR_NOT, ERROR_HEAT, ERROR_SENSOR, ERROR_PARCH})
	@Retention(RetentionPolicy.SOURCE)
	public @interface Errors{}

	public static final int BOIL_MODE_COMMON=0;//煮沸后保温模式，倒水后，直接放回，煮沸保温
	public static final int BOIL_MODE_SPECIAL=1;//煮沸后保温模式，倒水后，直接放回，3℃范围内，不煮沸直接保温
	@IntDef({BOIL_MODE_COMMON, BOIL_MODE_SPECIAL})
	@Retention(RetentionPolicy.SOURCE)
	public @interface BoilMode{}

	public static final int MEMORY_STATUS_COMMON=0;//提壶记忆保温功能(关)
	public static final int MEMORY_STATUS_SPECIAL=1;//提壶记忆保温功能(开)
	@IntDef({MEMORY_STATUS_COMMON, MEMORY_STATUS_SPECIAL})
	@Retention(RetentionPolicy.SOURCE)
	public @interface MemoryStatus{}
	
	//msg.what 
 	public final static byte MSG_WHAT_STATUS_VARY= 1;//状态变化
 	public final static byte MSG_WHAT_SETUP_DELAY= 2;//设置后读状态
 	public final static byte MSG_WHAT_CONNECT_VARY= 3;//连接状态变化
 	public final static byte MSG_WHAT_SETUP_FAIL= 4;//设置模式和温度失败
 	public final static byte MSG_WHAT_TIME_WRITE= 5;//设置保温时间	
 	public final static byte MSG_WHAT_TIME_READ= 6;//读取保温时间
	public final static byte MSG_REFRESH_UPDATE_INFO_SUC = 7;
	public final static byte MSG_REFRESH_UPDATE_INFO_ERR = 8;
	public final static byte MSG_BOIL_MODE_SET_WRITE= 9;//设置倒水放回，直接煮沸参数
	public final static byte MSG_BOIL_MODE_SET_READ= 10;//读取倒水放回，直接煮沸参数

	public final static byte MSG_MEMORY_STATUS_SET_WRITE= 20;//提壶记忆保温功能
	public final static byte MSG_MEMORY_STATUS_READ= 21;//提壶记忆保温功能


	public final static int MIN_KEEP_WARM_TIME=1;//最小保温时长
 	public final static int MAX_KEEP_WARM_TIME=12;//最大保温时长

	public final static int BOIL_TEMP=98;//沸腾温度
 	
    private static UMGlobalParam INSTANCE;
    private Context mContext= XmPluginHostApi.instance().context();
    private SharedPreferences sharedPreferences=mContext.getSharedPreferences(SharedPreferencesStr,Context.MODE_PRIVATE);
    private SharedPreferences.Editor editor=sharedPreferences.edit();
    
    public static String SharedPreferencesStr = "Params";
    public static String Key_Status_First = "Key_Status_First"; //第一次进入状态页面
    public static Boolean Value_Status_First= false;
    public static String Key_Set_First = "Key_Set_First"; //第一次进入保温设置页面
    public static Boolean Value_Set_First= false;
	public static String Key_Scene_Json = "Key_Scene_Json"; //保存场景测试
	public static String Value_Scene_Json = "";

	//Typeface mTypeface=Typeface.createFromAsset(mContext.getAssets(), "fonts/MI LanTing_GB Outside YS_V2.3_20160322.ttf");

    public static UMGlobalParam getInstance(){
        if(INSTANCE==null){
            synchronized (UMGlobalParam.class){
                if(INSTANCE==null){
                    INSTANCE=new UMGlobalParam();
                }
            }
        }
        return INSTANCE;
    }
    
    public void init(){
    	DisplayMetrics metrics=mContext.getResources().getDisplayMetrics();
    	density=metrics.density;
    }
    
    
    /***
     * 是否第一次进入状态页面
     * @return 结果
     */
    public boolean isStatusFirst(){
    	if(sharedPreferences!=null){
            return sharedPreferences.getBoolean(Key_Status_First,Value_Status_First);
    	}else {
			return true;
		}
    }

    /***
     * 设置第一次进入状态页面标志
     * @param flag
     * @return 结果
     */
    public void setStatusFirst(Boolean flag){
    	if(sharedPreferences!=null&&editor!=null){
            editor.putBoolean(Key_Status_First, flag);
            editor.commit();
    	}
    }
    
    /***
     * 是否第一次进入设置页面
     * @return 结果
     */
    public boolean isSetFirst(){
    	if(sharedPreferences!=null){
            return sharedPreferences.getBoolean(Key_Set_First,Value_Set_First);
    	}else {
			return true;
		}
    }

    /***
     * 设置第一次进入设置页面标志
     * @param flag 
     * @return 结果
     */
    public void setSetFirst(Boolean flag){
    	if(sharedPreferences!=null&&editor!=null){
            editor.putBoolean(Key_Set_First, flag);
            editor.commit();	
    	}
    }

	/***
	 * 小米字体
	 * @return
     */
//	public Typeface  getTextTypeface(){
//		return mTypeface;
//	}

	/***
	 * 用户场景保存
	 * @param json
     */
	public void setSceneJson(String json){
		if(sharedPreferences!=null&&editor!=null){
			editor.putString(Key_Scene_Json, json);
			editor.commit();
		}
	}

	/***
	 * 用户场景获取
	 * @return
     */
	public String getSceneJson(){
		if(sharedPreferences!=null){
			return sharedPreferences.getString(Key_Scene_Json,Value_Scene_Json);
		}else {
			return "";
		}
	}

	/***
	 * 清楚数据
	 */
	public void clear(){
		editor.clear();
		editor.commit();
	}

}
