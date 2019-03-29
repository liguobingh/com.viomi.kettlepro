package com.viomi.kettlepro.dev;

import java.io.Serializable;

/**
 * Created by young2 on 2016/1/15.
 */
public class UMKettleEachRecord implements Serializable {

	public String mac;
	public String time;//接收时间
	public int index;
	public int initialTemp;//初始温度
	public int boilTemp;//煮沸温度
	public int setTemp;//自定义保温温度
	public int workMode;//工作模式
	public int errorCode;//故障码
	public int cuteOffTime;//95℃到停止加热的时间
	public int heatTime;//加热到沸腾的时间
	public int holdTime;//沸腾降温到保温温度的时间
	public int Reserved;//保留

	public String toString(){
		return "mac="+mac+",time="+time+",index="+index+",initialTemp="+initialTemp
				+",boilTemp="+boilTemp+",setTemp="+setTemp+",workMode="+workMode+",errorCode="+errorCode
				+",cuteOffTime="+cuteOffTime+",heatTime="+heatTime+",holdTime="+holdTime+",Reserved="+Reserved;
	}

}
