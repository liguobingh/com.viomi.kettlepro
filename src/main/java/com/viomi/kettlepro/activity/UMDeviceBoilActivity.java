package com.viomi.kettlepro.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.viomi.kettlepro.R;
import com.viomi.kettlepro.view.EaseSwitchButton;

/**
 * Created by young2 on 2016/12/10.
 */

public class UMDeviceBoilActivity extends  UMBaseActivity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        layoutId= R.layout.um_activity_device_boil;
        super.onCreate(savedInstanceState);
        TextView textView = (TextView) findViewById(R.id.title);
        textView.setText(getString(R.string.um_device_boil));
        View backView = findViewById(R.id.back);
        backView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        EaseSwitchButton switchButton1= (EaseSwitchButton) findViewById(R.id.iv_switch1);

        EaseSwitchButton switchButton2= (EaseSwitchButton) findViewById(R.id.iv_switch2);

    }
}
