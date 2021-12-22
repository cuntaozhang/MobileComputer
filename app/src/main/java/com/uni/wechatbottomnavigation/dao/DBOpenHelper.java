package com.uni.wechatbottomnavigation.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;


public final class DBOpenHelper extends SQLiteOpenHelper {
    private final String DB_NAME = "StepCounter.db";
    private final int DB_VERSION = 1;
    //用于创建Banner表
    private String CREATE_BANNER = "create table step ("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "curDate TEXT, "
            + "totalSteps TEXT)";


    public DBOpenHelper(Context context) {
        super(context, "StepCounter.db", null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(this.CREATE_BANNER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
