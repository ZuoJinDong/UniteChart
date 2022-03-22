package com.zjd.unite.chart.entity

import com.blankj.utilcode.util.TimeUtils
import com.zjd.unite.chart.constant.ChartConstant
import com.zjd.unite.chart.utils.isCurrentYear

/**
 * @author ZJD
 * @date 2021/4/30
 * @desc 行情基础数据
 **/
open class BaseQuoteData {
    /** 最高价  */
    var high: Double = 0.0
    /** 最低价  */
    var low: Double = 0.0
    /** 开盘价  */
    var open: Double = 0.0
    /** 收盘价  */
    var close: Double = 0.0
    /** 时间  */
    var time: Long = 0

    var amount: Double = 0.0
    var holding: Double = 0.0
    get() = if(field == 0.0){
        amount
    }else{
        field
    }
    /** 成交量  */
    var volume: Double = 0.0

    /** 在chart中的横坐标  */
    var chartX = 0f
    /** 是否上涨 */
    fun isIncreasing(): Boolean = close >= open

    /** 金银比组合数据 */
    var amount2: Double = 0.0
    var holding2: Double = 0.0
        get() = if(field == 0.0){
            amount2
        }else{
            field
        }
    /** 成交量  */
    var volume2: Double = 0.0

    fun getDate(period: Int = 0): String{
        // 时间
        val pattern: String =
            if (period == ChartConstant.PERIOD_DAY || period == ChartConstant.PERIOD_WEEK) {
                if (isCurrentYear(time)) {
                    "MM-dd"
                } else {
                    "yyyy-MM-dd"
                }
            } else if (period == ChartConstant.PERIOD_SEASON || period == ChartConstant.PERIOD_MONTH) {
                "yyyy-MM"
            } else if (period == ChartConstant.PERIOD_YEAR) {
                "yyyy"
            } else {
                if (isCurrentYear(time)) {
                    "MM-dd HH:mm"
                } else {
                    "yyyy-MM-dd HH:mm"
                }
            }
        return TimeUtils.millis2String(time, pattern)
    }
}