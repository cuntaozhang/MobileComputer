package com.uni.wechatbottomnavigation.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.uni.wechatbottomnavigation.R;
import com.uni.wechatbottomnavigation.utils.ScreenUtil;


public class RecordsCalenderItemView extends RelativeLayout {
    private String weekStr;
    private String dateStr;
    private int position;
    private String curItemDate;

    private LinearLayout itemLl;
    private View lineView;
    private TextView weekTv;
    private RelativeLayout dateRl;
    private TextView dateTv;
    private OnCalenderItemClick itemClick;

    public int getPosition() {
        return position;
    }

    public interface OnCalenderItemClick {
        void onCalenderItemClick();
    }

    public void setOnCalenderItemClick(OnCalenderItemClick itemClick) {
        this.itemClick = itemClick;
    }

    public RecordsCalenderItemView(Context mContext, String weekStr, String dateStr, int position, String curItemDate) {
        super(mContext);

        this.weekStr = weekStr;
        this.dateStr = dateStr;
        this.position = position;
        this.curItemDate = curItemDate;

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.records_calender_item_view, this);
        this.itemLl = (LinearLayout) itemView.findViewById(R.id.records_calender_item_ll);
        this.weekTv = (TextView) itemView.findViewById(R.id.records_calender_item_week_tv);
        this.lineView = itemView.findViewById(R.id.calendar_item_line_view);
        this.dateRl = (RelativeLayout) itemView.findViewById(R.id.records_calender_item_date_rl);
        this.dateTv = (TextView) itemView.findViewById(R.id.records_calender_item_date_tv);

        this.weekTv.setTextSize(15.0F);
        View var9 = this.lineView;
        this.lineView.setVisibility(View.GONE);
        this.weekTv.setText(this.weekStr);
        this.dateTv.setText((CharSequence) this.dateStr);

        itemView.setLayoutParams((LayoutParams) (new LayoutParams(ScreenUtil.getScreenWidth(mContext) / 7, ViewGroup.LayoutParams.MATCH_PARENT)));
        itemView.setOnClickListener((OnClickListener) (new OnClickListener() {
            public final void onClick(View it) {
                RecordsCalenderItemView.this.itemClick.onCalenderItemClick();
            }
        }));
    }

    @SuppressLint("ResourceAsColor")
    public void setChecked(boolean checkedFlag) {
        if (checkedFlag) {
            weekTv.setTextColor(R.color.main_text_color);
            dateTv.setTextColor(R.color.white);
            dateRl.setBackgroundResource(R.mipmap.ic_blue_round_bg);
        } else {
            //当前item未被选中样式
            weekTv.setTextColor(R.color.gray_default_dark);
            dateTv.setTextColor(R.color.gray_default_dark);
            //设置背景透明
            dateRl.setBackgroundColor(Color.TRANSPARENT);
        }
    }
}

