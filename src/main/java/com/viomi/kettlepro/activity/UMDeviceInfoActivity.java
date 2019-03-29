package com.viomi.kettlepro.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.viomi.kettlepro.R;


/**
 * Created by young2 on 2016/12/10.
 */

public class UMDeviceInfoActivity extends  UMBaseActivity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        layoutId= R.layout.um_activity_device_info;
        super.onCreate(savedInstanceState);
        TextView textView = (TextView) findViewById(R.id.title);
        textView.setText(getString(R.string.um_device_info));
        View backView = findViewById(R.id.back);
        backView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        String mac=getIntent().getStringExtra("mac");
        TextView macText= (TextView) findViewById(R.id.mac_text);
        macText.setText(mac);
    }
}
