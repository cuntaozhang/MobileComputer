package com.uni.wechatbottomnavigation.ui.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.uni.wechatbottomnavigation.MainActivity;
import com.uni.wechatbottomnavigation.R;
import com.uni.wechatbottomnavigation.utils.TimeUtil;


import java.util.ArrayList;
import java.util.List;

public class BeforeOrAfterCalendarView extends RelativeLayout {
    private List<Integer> dayList;
    private List<String> dateList;
    private List<RecordsCalenderItemView> itemViewList;
    private LinearLayout calenderViewLl;
    private int curPosition;

    public BeforeOrAfterCalendarView(Context context) {
        super(context);
        dayList = new ArrayList<Integer>();
        dateList = new ArrayList<String>();
        itemViewList = new ArrayList<RecordsCalenderItemView>();
        calenderViewLl = null;
        curPosition = 0;

        View view = LayoutInflater.from(context).inflate(R.layout.before_or_after_calendar_layout, this);
        calenderViewLl = view.findViewById(R.id.boa_calender_view_ll);
        setBeforeDateViews();
        initItemViews();
    }

    /**
     * 设置之前的日期显示
     */
    private void setBeforeDateViews() {
        this.dateList.addAll(TimeUtil.getBeforeDateListByNow());
        this.dayList.addAll(TimeUtil.dateListToDayList(this.dateList));
    }

    private void initItemViews() {
        for (int i = 0; i < dateList.size(); ++i) {
            Integer day = dayList.get(i);
            String curItemDate = dateList.get(i);
            RecordsCalenderItemView itemView = null;
            if (day.equals(TimeUtil.getCurrentDay())) {
                itemView = new RecordsCalenderItemView(getContext(), "今天", day.toString(), i, curItemDate);
            } else {
                itemView = new RecordsCalenderItemView(getContext(),
                        TimeUtil.getCurWeekDay(curItemDate),
                        day.toString(),
                        i,
                        curItemDate
                );
            }

            itemViewList.add(itemView);
            calenderViewLl.addView(itemView);


            final RecordsCalenderItemView finalItemView = itemView;
            itemView.setOnCalenderItemClick(new RecordsCalenderItemView.OnCalenderItemClick() {
                @Override
                public void onCalenderItemClick() {
                    int curPosition = finalItemView.getPosition();
                    switchPositionView(curPosition);
                    if (calenderClickListener != null) {
                        calenderClickListener.onClickToRefresh(
                                curPosition,
                                dateList.get(curPosition)
                        );
                    }
                }
            });
        }
        switchPositionView(6);
    }

    private void switchPositionView(int position) {
        for (int i = 0; i < itemViewList.size(); ++i) {
            if (position == i) {
                itemViewList.get(i).setChecked(true);
            } else {
                itemViewList.get(i).setChecked(false);
            }
        }
    }

    private BoaCalenderClickListener calenderClickListener = null;

    public interface BoaCalenderClickListener {
        void onClickToRefresh(int position, String curDate);
    }

    public void setOnBoaCalenderClickListener(BoaCalenderClickListener calenderClickListener) {
        this.calenderClickListener = calenderClickListener;
    }
}
