package com.uni.wechatbottomnavigation.utils;

import static android.hardware.Sensor.TYPE_STEP_DETECTOR;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;


public class StepCountCheckUtil {
    private boolean hasSensor;
    private Context mContext;

    public StepCountCheckUtil(Context mContext) {
        this.mContext = mContext;
    }

    public boolean isSupportStepCountSensor() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER);
    }

    public static boolean isSupportStepCountSensor(Context context) {
        @SuppressLint("WrongConstant") SensorManager sensorManger = (SensorManager) context.getSystemService("sensor");

        Sensor countSensor = sensorManger.getDefaultSensor(TYPE_STEP_DETECTOR);
        Sensor detectorSensor = sensorManger.getDefaultSensor(TYPE_STEP_DETECTOR);
        return countSensor != null || detectorSensor != null;
    }
}
