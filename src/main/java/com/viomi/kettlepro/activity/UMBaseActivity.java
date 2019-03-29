package com.viomi.kettlepro.activity;


import android.os.Bundle;
import android.view.View;

import com.viomi.kettlepro.R;
import com.xiaomi.smarthome.device.api.XmPluginBaseActivity;

public class UMBaseActivity extends XmPluginBaseActivity
{
	public int layoutId = R.layout.activity_main;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layoutId);
		View title_bar = findViewById(R.id.title_bar);
		if(title_bar!=null){
			mHostActivity.setTitleBarPadding(title_bar);
		}
	}
	
}
