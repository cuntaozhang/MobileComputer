package com.uni.wechatbottomnavigation.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class TimeUtil {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("\"yyyy年MM月dd日\"");
    private static final Calendar mCalendar = Calendar.getInstance();
    private static final String[] weekStrings = new String[]{"日", "一", "二", "三", "四", "五", "六"};
    private static final String[] rWeekStrings = new String[]{"周日", "周一", "周二", "周三", "周四", "周五", "周六"};


    public String changeFormateDate(String date) {
        SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd");
        String curDate = null;
        try {
            Date dt = TimeUtil.dateFormat.parse(date);
            curDate = dFormat.format(dt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return curDate;
    }

    public static boolean isDateOutDate(String date) {
        try {
            if (new Date().getTime() - dateFormat.parse(date).getTime() > 7 * 24 * 60 * 60 * 1000)
                return true;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String getCurTime() {
        SimpleDateFormat dFormat = new SimpleDateFormat("HH:mm");
        return "今天" + dFormat.format(System.currentTimeMillis());
    }

    public static String getWeekStr(String dateStr) {
        String todayStr = TimeUtil.dateFormat.format(mCalendar.getTime());
        if (todayStr == dateStr) {
            return getCurTime();
        }
        Calendar preCalendar = Calendar.getInstance();
        preCalendar.add(Calendar.DATE, -1);
        String yesterdayStr = TimeUtil.dateFormat.format(preCalendar.getTime());
        if (yesterdayStr == dateStr) {
            return "昨天";
        }

        int w = 0;
        try {
            Date date = TimeUtil.dateFormat.parse(dateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            w = calendar.get(7) - 1;
            if (w < 0) {
                w = 0;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return TimeUtil.rWeekStrings[w];
    }

    public static int getCurrentDay() {
        return mCalendar.get(Calendar.DATE);
    }

    public static String getCurrentDate() {
        return dateFormat.format(mCalendar.getTime());
    }

    public static List dateListToDayList(List<String> dateList) {
        Calendar calendar = Calendar.getInstance();
        List dayList = new ArrayList();
        Iterator dateIterator = dateList.iterator();
        while (dateIterator.hasNext()) {
            String date = dateIterator.next().toString();
            try {
                calendar.setTime(TimeUtil.dateFormat.parse(date));
                int day = calendar.get(Calendar.DATE);
                dayList.add(day);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return dayList;
    }

    public static List<String> getBeforeDateListByNow() {
        List<String> weekList = new ArrayList<>();

        for (int i = -6; i <= 0; ++i) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, i);
            String date = TimeUtil.dateFormat.format(calendar.getTime());
            weekList.add(date);
        }
        return weekList;
    }

    public static String getCurWeekDay(String curDate) {
        int w = 0;
        try {
            Date date = TimeUtil.dateFormat.parse(curDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            if (w < 0) {
                w = 0;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return weekStrings[w];
    }

}
