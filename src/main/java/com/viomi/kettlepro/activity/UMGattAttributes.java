/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.viomi.kettlepro.activity;

import java.util.UUID;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class UMGattAttributes {
    public static String KETTLE_SERVICE = "01344736-0000-1000-8000-262837236156";//应用service
    public static String KETTLE_TIME_SET= "0000aa04-0000-1000-8000-00805f9b34fb";//保温时间设置character
    public static String KETTLE_SETUP= "0000aa01-0000-1000-8000-00805f9b34fb"; //模式和温度设置character
    public static String KETTLE_STATUS= "0000aa02-0000-1000-8000-00805f9b34fb"; //状态上报 notify character
    public static String kETTLE_PUREWATER= "0000aa03-0000-1000-8000-00805f9b34fb"; //煮水记录上报 notify character
    public static String KETTLE_BOIL_MODE_SET= "0000aa05-0000-1000-8000-00805f9b34fb";//保温倒数放回，直接保温  character
    
    public static String INFO_SERVICE =      "0000180a-0000-1000-8000-00805f9b34fb";//基本信息service
    public static String KETTLE_MCU_VERSION= "00002a28-0000-1000-8000-00805f9b34fb";//固件版本
    
    public static final UUID SERVICE_UUID = UUID.fromString(KETTLE_SERVICE);
    public static final UUID TIME_SET_UUID = UUID.fromString(KETTLE_TIME_SET);
    public static final UUID SETUP_UUID = UUID.fromString(KETTLE_SETUP);
    public static final UUID STATUS_UUID = UUID.fromString(KETTLE_STATUS);
    public static final UUID PUREWATER_UUID = UUID.fromString(kETTLE_PUREWATER);
    public static final UUID INFO_SERVICE_UUID = UUID.fromString(INFO_SERVICE);
    public static final UUID MCU_VERSION_UUID = UUID.fromString(KETTLE_MCU_VERSION);
    public static final UUID BOIL_MODE_SET_UUID = UUID.fromString(KETTLE_BOIL_MODE_SET);

    public static String HEART_RATE_MEASUREMENT = "0000FFF4-0000-1000-8000-00805f9b34fb";//"00002a37-0000-1000-8000-00805f9b34fb";
    public static String HEART_RATE_MEASUREMENT2 = "00008002-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
//
//    static {
//    	  attributes.put(KETTLE_SERVICE, "Kettle service");
//    	  attributes.put(KETTLE_TIME_SET, "Kettle keep warm time set");
//    	  attributes.put(KETTLE_SETUP, "Kettle setup characteristic");
//    	  attributes.put(KETTLE_STATUS, "Kettle status characteristic");
//    	  attributes.put(kETTLE_PUREWATER, "Kettle pure water characteristic");
//    }

//    public static String lookup(String uuid, String defaultName) {
//        String name = attributes.get(uuid);
//        return name == null ? defaultName : name;
//    }
}