package com.uni.wechatbottomnavigation.service;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;


import com.uni.wechatbottomnavigation.MainActivity;
import com.uni.wechatbottomnavigation.R;
import com.uni.wechatbottomnavigation.bean.StepEntity;
import com.uni.wechatbottomnavigation.constant.ConstantData;
import com.uni.wechatbottomnavigation.dao.StepDataDao;
import com.uni.wechatbottomnavigation.utils.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StepService extends Service implements SensorEventListener {
    //当前日期
    private String currentDate;
    //当前步数
    private int currentStep;
    //传感器
    @SuppressLint("ServiceCast")
    private SensorManager sensorManager;
    //数据库
    private StepDataDao stepDataDao;
    //计步传感器类型 0-counter 1-detector
    private int stepSensor = -1;
    //广播接收
    private BroadcastReceiver mInfoReceiver;
    //发送消息，用来和Service之间传递步数
    private final Messenger messenger = new Messenger((Handler) (new StepService.MessengerHandler()));
    //是否有当天的记录
    private boolean hasRecord;
    //未记录之前的步数
    private int hasStepCount;
    //下次记录之前的步数
    private int previousStepCount;
    private Notification.Builder builder;

    private NotificationManager notificationManager;
    private Intent nfIntent;

    @SuppressLint("ServiceCast")
    @Override
    public void onCreate() {
        super.onCreate();
//        android.os.Debug.waitForDebugger();
//        Log.d("xl", "-2-2-2-2");
        this.initBroadcastReceiver();
        (new Thread((Runnable) (new Runnable() {
            public final void run() {
                getStepDetector();
            }
        }))).start();
        this.initTodayData();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return this.messenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /**
         * 此处设将Service为前台，不然当APP结束以后很容易被GC给干掉，
         * 这也就是大多数音乐播放器会在状态栏设置一个原理大都是相通的
         */
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //----------------  针对8.0 新增代码 --------------------------------------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.builder = new Notification.Builder(this.getApplicationContext(), ConstantData.CHANNEL_ID);
            @SuppressLint("WrongConstant")
            NotificationChannel notificationChannel = new
                    NotificationChannel(
                    ConstantData.CHANNEL_ID,
                    ConstantData.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_MIN
            );
            notificationChannel.enableLights(false); //如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
            notificationChannel.setShowBadge(false); //是否显示角标
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            notificationManager.createNotificationChannel(notificationChannel);
            this.builder.setChannelId(ConstantData.CHANNEL_ID);
        } else {
            this.builder = new Notification.Builder(this.getApplicationContext());
        }
        /**
         * 设置点击通知栏打开的界面，此处需要注意了，
         * 如果你的计步界面不在主界面，则需要判断app是否已经启动，
         * 再来确定跳转页面，这里面太多坑
         */
        this.nfIntent = new Intent(this, MainActivity.class);
        this.setStepBuilder();
        // 参数
        this.startForeground(ConstantData.NOTIFY_ID, this.builder.build()); //开始前台服务
        return START_STICKY;
    }

    /**
     * 自定义handler
     */
    private class MessengerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConstantData.MSG_FROM_CLIENT:
                    try {
                        //这里负责将当前的步数发送出去，可以在界面或者其他地方获取，我这里是在MainActivity中获取来更新界面
                        Messenger messenger = msg.replyTo;
                        Message replyMsg = Message.obtain((Handler) null, ConstantData.MSG_FROM_SERVER);
                        Bundle bundle = new Bundle();
                        bundle.putInt("steps", StepService.this.currentStep);
                        replyMsg.setData(bundle);
                        messenger.send(replyMsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * 初始化广播
     */
    private void initBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        // 屏幕灭屏广播
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        //关机广播
        filter.addAction(Intent.ACTION_SHUTDOWN);
        // 屏幕解锁广播
        filter.addAction(Intent.ACTION_USER_PRESENT);
        // 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
        // example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，
        // 所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        //监听日期变化
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);

        this.mInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null) {
                    switch (action) {
                        // 屏幕灭屏广播
                        case Intent.ACTION_SCREEN_OFF:
                            StepService.this.saveStepData();
                            break;
                        //关机广播，保存好当前数据
                        case Intent.ACTION_SHUTDOWN:
                            StepService.this.saveStepData();
                            break;
                        // 屏幕解锁广播
                        case Intent.ACTION_USER_PRESENT:
                            StepService.this.saveStepData();
                            break;
                        // 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
                        // example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，
                        // 所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框
                        case Intent.ACTION_CLOSE_SYSTEM_DIALOGS:
                            StepService.this.saveStepData();
                            break;
                        //监听日期变化
                        case Intent.ACTION_DATE_CHANGED:
                        case Intent.ACTION_TIME_CHANGED:
                        case Intent.ACTION_TIME_TICK:
                            StepService.this.saveStepData();
                            StepService.this.isNewDay();
                            break;
                    }
                }
            }
        };
        //注册广播
        this.registerReceiver(this.mInfoReceiver, filter);
    }

    /**
     * 初始化当天数据
     */
    private void initTodayData() {
        //获取当前时间
        this.currentDate = TimeUtil.getCurrentDate();
        //获取数据库
        this.stepDataDao = new StepDataDao(this.getApplicationContext());
        //获取当天的数据，用于展示
        StepEntity entity = stepDataDao.getCurDataByDate(currentDate);
        //为空则说明还没有该天的数据，有则说明已经开始当天的计步了
        if (entity == null) {
            currentStep = 0;
        } else {
            currentStep = Integer.parseInt(entity.getSteps());
        }
    }

    private void isNewDay() {
        String time = "00:00";
        if (time.equals((new SimpleDateFormat("HH:mm")).format(new Date())) || !this.currentDate.equals(TimeUtil.getCurrentDate())) {
            this.initTodayData();
        }
    }

    /**
     * 获取传感器实例
     */
    private void getStepDetector() {
        if (sensorManager != null) {
            sensorManager = null;
        }
        // 获取传感器管理器的实例
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        //android4.4以后可以使用计步传感器
        if (Build.VERSION.SDK_INT >= 19) {
            addCountStepListener();
        }
    }

    /**
     * 添加传感器监听
     */
    private void addCountStepListener() {
        Sensor countSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Sensor detectSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (countSensor != null) {
            stepSensor = 0;
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else if (detectSensor != null) {
            stepSensor = 1;
            this.sensorManager.registerListener(this, detectSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    /**
     * 由传感器记录当前用户运动步数，注意：该传感器只在4.4及以后才有，并且该传感器记录的数据是从设备开机以后不断累加，
     * 只有当用户关机以后，该数据才会清空，所以需要做数据保护
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (this.stepSensor == 0) {
            int tempStep = (int) event.values[0];
            if (!this.hasRecord) {
                this.hasRecord = true;
                this.hasStepCount = tempStep;
            } else {
                int thisStepCount = tempStep - this.hasStepCount;
                this.currentStep += thisStepCount - this.previousStepCount;
                this.previousStepCount = thisStepCount;
            }
            this.saveStepData();
        } else if (this.stepSensor == 1 && (double) event.values[0] == 1.0D) {
            this.currentStep++;
            this.saveStepData();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * 保存当天的数据到数据库中，并去刷新通知栏
     */
    private void saveStepData() {
        //查询数据库中的数据
        StepEntity entity = this.stepDataDao.getCurDataByDate(currentDate);
        //为空则说明还没有该天的数据，有则说明已经开始当天的计步了
        if (entity == null) {
            //没有则新建一条数据
            entity = new StepEntity();
            entity.setCurDate(currentDate);
            entity.setSteps(String.valueOf(currentStep));

            stepDataDao.addNewData(entity);
        } else {
            //有则更新当前的数据
            entity.setSteps(String.valueOf(currentStep));

            stepDataDao.updateCurData(entity);
        }
        setStepBuilder();
    }

    private void setStepBuilder() {
        if (this.builder != null) {
            this.builder = this.builder.setContentIntent(
                    PendingIntent.getActivities(this,
                            0,
                            new Intent[]{this.nfIntent},
                            0
                    )
            );

            this.builder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_launcher_foreground));
            this.builder.setContentTitle("今日步数" + currentStep + "步");
            this.builder.setSmallIcon(R.mipmap.ic_launcher);
            this.builder.setContentTitle("加油，要记得勤加运动哦");
            // 获取构建好的Notification
            Notification stepNotification = this.builder.build();
            //调用更新
            notificationManager.notify(ConstantData.NOTIFY_ID, stepNotification);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //主界面中需要手动调用stop方法service才会结束
        stopForeground(true);
        unregisterReceiver(mInfoReceiver);
    }


    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
