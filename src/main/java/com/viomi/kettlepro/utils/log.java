package com.viomi.kettlepro.utils;

import android.util.Log;


public class log {
	public static boolean DEBUG = false;
	
	public static void d(String tag,String msg) {
		if(DEBUG){
			Log.d(tag,msg);
		}
	}
}
