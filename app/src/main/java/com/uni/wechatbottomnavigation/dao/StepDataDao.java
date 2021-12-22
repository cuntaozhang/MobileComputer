package com.uni.wechatbottomnavigation.dao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.uni.wechatbottomnavigation.bean.StepEntity;

import java.util.ArrayList;
import java.util.List;

public class StepDataDao {
    private DBOpenHelper stepHelper;
    private SQLiteDatabase stepDb;

    public StepDataDao(Context context) {
        super();
        this.stepHelper = new DBOpenHelper(context);
    }

    public void addNewData(StepEntity stepEntity) {
        this.stepDb = this.stepHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("curData", stepEntity.getCurDate());
        values.put("totalSteps", stepEntity.getSteps());

        stepDb.insert("step", null, values);
        stepDb.close();
    }

    public StepEntity getCurDataByDate(String curDate) {
        this.stepDb = this.stepHelper.getReadableDatabase();
        StepEntity stepEntity = null;

        Cursor cursor = this.stepDb.query("step", null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            String date = cursor.getString(cursor.getColumnIndexOrThrow("curDate"));
            if (curDate.equals(date)) {
                String steps = cursor.getString(cursor.getColumnIndexOrThrow("totalSteps"));
                stepEntity = new StepEntity(date, steps);
                break;
            }
        }
        this.stepDb.close();
        cursor.close();
        return stepEntity;
    }

    public List getAllDatas() {
        this.stepDb = this.stepHelper.getReadableDatabase();
        List dataList = new ArrayList();
        Cursor cursor = this.stepDb.rawQuery("select * from step", null);
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String curDate = cursor.getString(cursor.getColumnIndex("curDate"));
            @SuppressLint("Range") String totalSteps = cursor.getString(cursor.getColumnIndex("totalSteps"));
            StepEntity entity = new StepEntity(curDate, totalSteps);
            dataList.add(entity);
        }
        stepDb.close();
        cursor.close();
        return dataList;
    }

    public void updateCurData(StepEntity stepEntity) {
        this.stepDb = this.stepHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("curDate", stepEntity.getCurDate());
        values.put("totalSteps", stepEntity.getSteps());

        this.stepDb.update("step", values, "curDate=", new String[]{stepEntity.getCurDate()});

        stepDb.close();
    }

    public void deleteCurData(String curDate) {
        this.stepDb = this.stepHelper.getReadableDatabase();
        this.stepDb.delete("step", "curDate", new String[]{curDate});
        stepDb.close();
    }

}
