package com.zjd.unite.chart.utils;

import android.util.Log;
import com.blankj.utilcode.util.TimeUtils;
import com.zjd.unite.chart.constant.QuoteConstant;
import com.zjd.unite.chart.entity.KLineData;
import com.zjd.unite.chart.entity.TradeTimeBean;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class QuoteUtils {

    /**
     * 根据名称获取
     */
    public static int getPeriodTs(String title){
        switch (title){
            case "日K":
                return QuoteConstant.PERIOD_DAY;
            case "月K":
                return QuoteConstant.PERIOD_MONTH;
            case "周K":
                return QuoteConstant.PERIOD_WEEK;
            case "60分":
                return QuoteConstant.PERIOD_60_MINUTE;
            case "30分":
                return QuoteConstant.PERIOD_30_MINUTE;
            case "15分":
                return QuoteConstant.PERIOD_15_MINUTE;
            case "5分":
                return QuoteConstant.PERIOD_5_MINUTE;
            case "1分":
                return QuoteConstant.PERIOD_1_MINUTE;
            case "5日":
                return QuoteConstant.PERIOD_5_DAY;
            case "3日":
                return QuoteConstant.PERIOD_3_DAY;
            case "2日":
                return QuoteConstant.PERIOD_2_DAY;
            case "4时":
                return QuoteConstant.PERIOD_4_HOUR;
            case "闪电":
                return QuoteConstant.PERIOD_FLASH;
            case "季K":
                return QuoteConstant.PERIOD_SEASON;
            case "年K":
                return QuoteConstant.PERIOD_YEAR;
            case "3分":
                return QuoteConstant.PERIOD_3_MINUTE;
            case "10分":
                return QuoteConstant.PERIOD_10_MINUTE;
            case "20分":
                return QuoteConstant.PERIOD_20_MINUTE;
            case "2时":
                return QuoteConstant.PERIOD_2_HOUR;
            case "3时":
                return QuoteConstant.PERIOD_3_HOUR;
            case "6时":
                return QuoteConstant.PERIOD_6_HOUR;
            case "8时":
                return QuoteConstant.PERIOD_8_HOUR;
            case "12时":
                return QuoteConstant.PERIOD_12_HOUR;
            default:
                return QuoteConstant.PERIOD_TS;
        }
    }

    /**
     * 判断是否在时间节点中，只要再其中的一个时间节点中即可
     *
     * @param tradeTime 时间节点列表
     */
    private static boolean isInPointNode(List<TradeTimeBean> tradeTime, long time) {
        for (int i = 0; i < tradeTime.size(); i++) {
            TradeTimeBean timeBean = tradeTime.get(i);

            if (isInTimeRange(timeBean.getStart(), timeBean.getEnd(), time)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否在当前的时间区间中
     */
    private static boolean isInTimeRange(long begin, long end, long time) {
        if (time < begin) {
            return false;
        } else return time < end;
    }

    /**
     * 判断是否在同一季度
     */
    private static boolean isInSameSeason(Calendar calendar, long preTime, long time) {
        calendar.setTimeInMillis(preTime);
        int yearPre = calendar.get(Calendar.YEAR);
        int monthPre = calendar.get(Calendar.MONTH);
        calendar.setTimeInMillis(time);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        if(yearPre != year)
            return false;

        int offsetPre = monthPre/3;
        int offset = month/3;
        return offsetPre == offset;
    }

    /**
     * 判断是否在同一年
     */
    private static boolean isInSameYear(Calendar calendar, long preTime, long time) {
        calendar.setTimeInMillis(preTime);
        int yearPre = calendar.get(Calendar.YEAR);
        calendar.setTimeInMillis(time);
        int year = calendar.get(Calendar.YEAR);
        return yearPre == year;
    }

    /**
     * 合并K线数据
     *
     * @param pointList
     * @param mTag
     * @return
     */
    public static List<KLineData> mergeKHisData(List<KLineData> pointList, int mTag) {
        List<KLineData> list = new ArrayList<>();

        long start = 0, end = 0;
        List<KLineData> beans = new ArrayList<>();
        for (int i = 0; i < pointList.size(); ) {

            KLineData bean = pointList.get(i);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(bean.getTime());
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int h = calendar.get(Calendar.HOUR_OF_DAY);
            int m = calendar.get(Calendar.MINUTE);
            int week = calendar.get(Calendar.DAY_OF_WEEK);

            if(mTag == QuoteConstant.PERIOD_3_MINUTE
                    ||mTag == QuoteConstant.PERIOD_10_MINUTE
                    || mTag == QuoteConstant.PERIOD_15_MINUTE
                    || mTag == QuoteConstant.PERIOD_20_MINUTE
                    || mTag == QuoteConstant.PERIOD_30_MINUTE){

                int offset;

                if (mTag == QuoteConstant.PERIOD_3_MINUTE) {
                    offset = 3;
                }else if (mTag == QuoteConstant.PERIOD_10_MINUTE) {
                    offset = 10;
                }else if (mTag == QuoteConstant.PERIOD_15_MINUTE) {
                    offset = 15;
                }else if (mTag == QuoteConstant.PERIOD_20_MINUTE) {
                    offset = 20;
                }else {
                    offset = 30;
                }

                if (start == 0 && end == 0) {
                    while (m % offset != 0) {
                        m--;
                    }
                    calendar.set(Calendar.MINUTE, m);
                    start = calendar.getTimeInMillis();
                    end = start + offset * QuoteConstant.min_time;
                } else {
                    if (isInTimeRange(start, end, bean.getTime())) {
                        beans.add(bean);
                        i++;
                        if (i == pointList.size()) {
                            list.add(mergeStartData(beans));
                            beans.clear();
                        }
                    } else {
                        list.add(mergeStartData(beans));
                        beans.clear();
                        start = end = 0;
                    }
                }

                if(i == pointList.size() - 1){
                    Log.d("time=====", TimeUtils.millis2String(start,"HH:mm") + "-" + TimeUtils.millis2String(end,"HH:mm"));
                }
            } else if(mTag == QuoteConstant.PERIOD_2_HOUR
                    || mTag == QuoteConstant.PERIOD_3_HOUR
                    || mTag == QuoteConstant.PERIOD_4_HOUR
                    || mTag == QuoteConstant.PERIOD_6_HOUR
                    || mTag == QuoteConstant.PERIOD_8_HOUR
                    || mTag == QuoteConstant.PERIOD_12_HOUR){

                int offset;

                if (mTag == QuoteConstant.PERIOD_2_HOUR) {
                    offset = 2;
                }else if (mTag == QuoteConstant.PERIOD_3_HOUR) {
                    offset = 3;
                }else if (mTag == QuoteConstant.PERIOD_4_HOUR) {
                    offset = 4;
                }else if (mTag == QuoteConstant.PERIOD_6_HOUR) {
                    offset = 6;
                }else if (mTag == QuoteConstant.PERIOD_8_HOUR) {
                    offset = 8;
                }else {
                    offset = 12;
                }

                if (start == 0 && end == 0) {
                    while (h % offset != 0) {
                        h--;
                    }
                    calendar.set(Calendar.HOUR_OF_DAY, h);
                    start = calendar.getTimeInMillis();
                    end = start + offset * QuoteConstant.hour_time;
                } else {
                    if (isInTimeRange(start, end, bean.getTime())) {
                        beans.add(bean);
                        i++;
                        if (i == pointList.size()) {
                            list.add(mergeStartData(beans));
                            beans.clear();
                        }
                    } else {
                        list.add(mergeStartData(beans));
                        beans.clear();
                        start = end = 0;
                    }
                }
            } else if (mTag == QuoteConstant.PERIOD_WEEK) {//星期  7个组装成1个
                if (start == 0 && end == 0) {
                    while (week != 2) {
                        if (week == 1)
                            week = 8;
                        week--;
                    }
                    calendar.set(Calendar.DAY_OF_WEEK, week);
                    start = calendar.getTimeInMillis();
                    end = start + 7 * QuoteConstant.day_time;
                } else {
                    if (isInTimeRange(start, end, bean.getTime())) {
                        beans.add(bean);
                        i++;
                        if (i == pointList.size()) {
                            list.add(mergeEndData(beans));
                            beans.clear();
                        }
                    } else {
                        list.add(mergeEndData(beans));
                        beans.clear();
                        start = end = 0;
                    }
                }
            }
            else if (mTag == QuoteConstant.PERIOD_MONTH) {
                if (start == 0 && end == 0) {
                    while (day != 1) {
                        day--;
                    }
                    calendar.set(Calendar.DAY_OF_MONTH, day);
                    start = calendar.getTimeInMillis();
                    calendar.set(Calendar.MONTH, month + 1);
                    end = calendar.getTimeInMillis();
                } else {
                    if (isInTimeRange(start, end, bean.getTime())) {
                        beans.add(bean);
                        i++;
                        if (i == pointList.size()) {
                            list.add(mergeEndData(beans));
                            beans.clear();
                        }
                    } else {
                        list.add(mergeEndData(beans));
                        beans.clear();
                        start = end = 0;
                    }
                }

            } else if (mTag == QuoteConstant.PERIOD_SEASON) {
                if(i == 0){
                    beans.clear();
                    beans.add(bean);
                }else if(isInSameSeason(calendar,beans.get(beans.size()-1).getTime(),bean.getTime())){
                    beans.add(bean);
                }else {
                    list.add(mergeEndData(beans));
                    beans.clear();
                    beans.add(bean);
                }

                if(i == pointList.size() - 1){
                    list.add(mergeEndData(beans));
                    beans.clear();
                }
                i++;
            } else if (mTag == QuoteConstant.PERIOD_YEAR) {
                if(i == 0){
                    beans.clear();
                    beans.add(bean);
                }else if(isInSameYear(calendar,beans.get(beans.size()-1).getTime(),bean.getTime())){
                    beans.add(bean);
                }else {
                    list.add(mergeEndData(beans));
                    beans.clear();
                    beans.add(bean);
                }

                if(i == pointList.size() - 1){
                    list.add(mergeEndData(beans));
                    beans.clear();
                }
                i++;
            } else {
                //默认不组装
                KLineData kLine = new KLineData();

                kLine.setClose(bean.getClose());
                kLine.setHigh(bean.getHigh());
                kLine.setHolding(bean.getHolding());
                kLine.setLow(bean.getLow());
                kLine.setOpen(bean.getOpen());
                kLine.setTime(bean.getTime());
                kLine.setVolume(bean.getVolume());
                kLine.setAmount(bean.getAmount());
                list.add(kLine);
                i++;
            }
        }

        return list;
    }

    /**
     * 合并数据
     * 使用第一个数据的时间
     *
     * @param beans
     */
    private static KLineData mergeStartData(List<KLineData> beans) {
        if (beans == null)
            return null;

        KLineData line = new KLineData();

        double high = Double.MIN_VALUE;
        double low = Double.MAX_VALUE;
        double amount = Integer.MIN_VALUE;
        int volume = 0;
        double holding = 0;
        for (int i = 0; i < beans.size(); i++) {
            KLineData bean = beans.get(i);

            //开盘 收盘
            if (i == 0) {
                line.setOpen(bean.getOpen());
                line.setTime(bean.getTime());
            }
            if (i == beans.size() - 1) {
                line.setClose(bean.getClose());
            }

            volume += bean.getVolume();
            holding += bean.getHolding();
            amount = Math.max(amount, bean.getAmount());
            //最高价 最低价
            high = Math.max(high, bean.getHigh());
            low = Math.min(low, bean.getLow());
        }
        line.setHigh(high);
        line.setLow(low);
        line.setAmount(amount);
        line.setVolume(volume);
        line.setHolding(holding);
        return line;
    }

    /**
     * 合并数据
     * 使用最后一个数据的时间
     *
     * @param beans
     */
    private static KLineData mergeEndData(List<KLineData> beans) {
        if (beans == null)
            return null;

        KLineData line = new KLineData();

        double high = Double.MIN_VALUE;
        double low = Double.MAX_VALUE;
        double amount = Integer.MIN_VALUE;
        int volume = 0;
        double holding = 0;
        for (int i = 0; i < beans.size(); i++) {
            KLineData bean = beans.get(i);

            //开盘 收盘
            if (i == 0) {
                line.setOpen(bean.getOpen());
            }
            if (i == beans.size() - 1) {
                line.setClose(bean.getClose());
                line.setTime(bean.getTime());
            }
            volume += bean.getVolume();
            holding += bean.getHolding();
            amount = Math.max(amount, bean.getAmount());
            //最高价 最低价
            high = Math.max(high, bean.getHigh());
            low = Math.min(low, bean.getLow());
        }
        line.setHigh(high);
        line.setLow(low);
        line.setAmount(amount);
        line.setVolume(volume);
        line.setHolding(holding);
        return line;
    }

    public static int getFlag(String sign) {
        int i = 0;
        if (sign.equals("多") || sign.equals("补多")) {
            i = 0;
        } else if (sign.equals("空") || sign.equals("补空")) {
            i = 1;
        }
        return i;
    }

    /**
     * 合并数据
     * 使用最后一个数据的时间
     *
     * @param beans
     */
    private static KLineData mergeEndData4H(List<KLineData> beans, long end) {
        if (beans == null) {
            return null;
        }

        KLineData line = new KLineData();

        double high = Double.MIN_VALUE;
        double low = Double.MAX_VALUE;
        double amount = Double.MIN_VALUE;
        int volume = 0;
        double holding = 0;
        for (int i = 0; i < beans.size(); i++) {
            KLineData bean = beans.get(i);

            //开盘 收盘
            if (i == 0) {
                line.setOpen(bean.getOpen());
            }
            if (i == beans.size() - 1) {
                line.setClose(bean.getClose());
                line.setTime(end);
            }
            volume += bean.getVolume();
            holding += bean.getHolding();
            amount = Math.max(amount, bean.getAmount());
            //最高价 最低价
            high = Math.max(high, bean.getHigh());
            low = Math.min(low, bean.getLow());
        }
        line.setHigh(high);
        line.setLow(low);
        line.setAmount(amount);
        line.setVolume(volume);
        line.setHolding(holding);
        return line;
    }
}
