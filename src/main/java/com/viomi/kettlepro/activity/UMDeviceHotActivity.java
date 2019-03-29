package com.viomi.kettlepro.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.viomi.kettlepro.R;
import com.viomi.kettlepro.view.EaseSwitchButton;

/**
 * Created by young2 on 2016/12/10.
 */

public class UMDeviceHotActivity extends  UMBaseActivity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        layoutId= R.layout.um_activity_device_hot;
        super.onCreate(savedInstanceState);
        TextView textView = (TextView) findViewById(R.id.title);
        textView.setText(getString(R.string.um_device_hot));
        View backView = findViewById(R.id.back);
        backView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        EaseSwitchButton switchButton= (EaseSwitchButton) findViewById(R.id.iv_switch);

    }
}
