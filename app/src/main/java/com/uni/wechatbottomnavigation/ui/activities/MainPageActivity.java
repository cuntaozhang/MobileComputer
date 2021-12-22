package com.uni.wechatbottomnavigation.ui.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.uni.wechatbottomnavigation.R;
import com.uni.wechatbottomnavigation.base.BaseActivityMainPage;
import com.uni.wechatbottomnavigation.bean.StepEntity;
import com.uni.wechatbottomnavigation.constant.ConstantData;
import com.uni.wechatbottomnavigation.dao.StepDataDao;
import com.uni.wechatbottomnavigation.service.StepService;
import com.uni.wechatbottomnavigation.ui.view.BeforeOrAfterCalendarView;
import com.uni.wechatbottomnavigation.utils.StepCountCheckUtil;
import com.uni.wechatbottomnavigation.utils.TimeUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainPageActivity  extends BaseActivityMainPage implements Handler.Callback {
    private BeforeOrAfterCalendarView calenderView;
    private String curSelDate = "";
    private DecimalFormat df = new DecimalFormat("#.##");
    private List<StepEntity> stepEntityList = new ArrayList<StepEntity>();
    private StepDataDao stepDataDao = null;
    private boolean isBind = false;
    private Messenger mGetReplyMessenger = new Messenger(new Handler((Handler.Callback) this));
    private Messenger messenger = null;

    /**
     * 定时任务
     */
    private TimerTask timerTask = null;
    private Timer timer = null;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_mian_page;
    }

    @Override
    protected void initData() {
        curSelDate = TimeUtil.getCurrentDate();
        calenderView = new BeforeOrAfterCalendarView(this);
        stepDataDao =new StepDataDao(this);
        LinearLayout layout = this.findViewById(R.id.movement_records_calender_ll);
        layout.addView(this.calenderView);
        requestPermission();
    }

    @Override
    protected void initListener() {

        this.calenderView.setOnBoaCalenderClickListener(new BeforeOrAfterCalendarView.BoaCalenderClickListener() {
            @Override
            public void onClickToRefresh(int position, String curDate) {
                //获取当前选中的时间
                curSelDate = curDate;
                //根据日期去取数据
                setDatas();
            }
        });
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions((Activity) this, new String[]{"android.permission.ACTIVITY_RECOGNITION"}, 1);
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) this, "android.permission.ACTIVITY_RECOGNITION")) {
                    Toast.makeText((Context) this, (CharSequence) "请允许获取健身运动信息，不然不能为你计步哦~", Toast.LENGTH_SHORT).show();
                }
            } else {
                this.startStepService();
            }
        } else {
            this.startStepService();
        }
    }

    private void startStepService() {
        /**
         * 这里判断当前设备是否支持计步
         */
        if (StepCountCheckUtil.isSupportStepCountSensor(this)) {
            this.getRecordList();
            this.findViewById(R.id.is_support_tv).setVisibility(View.GONE);
            setDatas();
            setupService();
        } else {
            TextView textView = this.findViewById(R.id.movement_total_steps_tv);
            textView.setText("0");
            this.findViewById(R.id.is_support_tv).setVisibility(View.VISIBLE);
        }
    }

    /**
     * 开启计步服务
     */
    private void setupService() {
        Intent intent = new Intent(this, StepService.class);
        this.isBind = bindService(intent, this.conn, Context.BIND_AUTO_CREATE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    /**
     * 用于查询应用服务（application Service）的状态的一种interface，
     * 更详细的信息可以参考Service 和 context.bindService()中的描述，
     * 和许多来自系统的回调方式一样，ServiceConnection的方法都是进程的主线程中调用的。
     */
    private ServiceConnection conn = new ServiceConnection() {
        /**
         * 在建立起于Service的连接时会调用该方法，目前Android是通过IBind机制实现与服务的连接。
         * @param name 实际所连接到的Service组件名称
         * @param service 服务的通信信道的IBind，可以通过Service访问对应服务
         */
        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        messenger = new Messenger(service);
                        Message msg = Message.obtain(null, ConstantData.MSG_FROM_CLIENT);
                        msg.replyTo = mGetReplyMessenger;
                        messenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            };
            timer = new Timer();
            timer.schedule(timerTask, 0, 500);
        }

        /**
         * 当与Service之间的连接丢失的时候会调用该方法，
         * 这种情况经常发生在Service所在的进程崩溃或者被Kill的时候调用，
         * 此方法不会移除与Service的连接，当服务重新启动的时候仍然会调用 onServiceConnected()。
         * @param name 丢失连接的组件名称
         */
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * 设置记录数据
     */
    private void setDatas() {
        StepEntity stepEntity = stepDataDao.getCurDataByDate(curSelDate);

        if (stepEntity != null) {
            String steps = stepEntity.getSteps();
            //获取全局的步数
            TextView textView0 = this.findViewById(R.id.movement_total_steps_tv);
            textView0.setText(steps.toString());
            //计算总公里数
            TextView textView1 = this.findViewById(R.id.movement_total_km_tv);
            int it = Integer.parseInt(steps);
            textView1.setText(this.countTotalKM(it));
        } else {
            //获取全局的步数
            TextView textView0 = this.findViewById(R.id.movement_total_steps_tv);
            textView0.setText("0");
            //计算总公里数
            TextView textView1 = this.findViewById(R.id.movement_total_km_tv);
            textView1.setText("0");
        }

        //设置时间
        String time = TimeUtil.getWeekStr(curSelDate);
        TextView textView0 = this.findViewById(R.id.movement_total_km_time_tv);
        textView0.setText(time);
        TextView textView1 = this.findViewById(R.id.movement_total_steps_time_tv);
        textView1.setText(time);
    }

    /**
     * 简易计算公里数，假设一步大约有0.7米
     *
     * @param steps 用户当前步数
     * @return
     */
    private String countTotalKM(int steps) {
        double totalMeters = (double) (steps * 0.7);
        //保留两位有效数字
        return df.format(totalMeters / 1000);
    }

    /**
     * 获取全部运动历史纪录
     */
    private void getRecordList() {
        //获取数据库
        stepDataDao = new StepDataDao(this);
        stepEntityList.clear();
        stepEntityList.addAll(stepDataDao.getAllDatas());
        if (stepEntityList.size() > 7) {
            //在这里获取历史记录条数，当条数达到7条以上时，就开始删除第七天之前的数据
            for (int i = 0; i < stepEntityList.size(); ++i) {
                if (TimeUtil.isDateOutDate(stepEntityList.get(i).getCurDate())) {
                    stepDataDao.deleteCurData(stepEntityList.get(i).getCurDate());
                }
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            //这里用来获取到Service发来的数据
            case ConstantData.MSG_FROM_SERVER:
                //如果是今天则更新数据
                if (this.curSelDate.equals(TimeUtil.getCurrentDate())) {
                    int steps = msg.getData().getInt("steps");
                    TextView textView0 = this.findViewById(R.id.movement_total_steps_tv);
                    textView0.setText((String.valueOf(steps)));
                    TextView textView1 = this.findViewById(R.id.movement_total_km_tv);
                    textView1.setText((String.valueOf(this.countTotalKM(steps))));
                }
                break;
            default:
                return false;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //记得解绑Service，不然多次绑定Service会异常
        if (isBind)
            this.unbindService(conn);
    }
}
