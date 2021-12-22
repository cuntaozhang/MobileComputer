package com.uni.wechatbottomnavigation.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class ScreenUtil {
    public static final int getScreenWidth(Context mContext) {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        //获取屏幕宽高，单位是像素
        int widthPixels = displayMetrics.widthPixels;
        int heightPixels = displayMetrics.heightPixels;
        //获取屏幕密度倍数
        float density = displayMetrics.density;
        return widthPixels;
    }
}
