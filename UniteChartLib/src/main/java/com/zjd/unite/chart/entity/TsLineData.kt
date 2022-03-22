package com.zjd.unite.chart.entity

import com.blankj.utilcode.util.CacheMemoryStaticUtils
import java.io.Serializable

/**
 * @author ZJD
 * @date 2021/4/30
 * @desc 分时数据
 **/

/** 一分钟 */
const val ONE_MINUTE = 60000L

class TsHisBean{
    var quoteId: Int = 0
    var billDay: Long = 0
    var yersterdayClose: Double = 0.0
    var cacheTime: Long = 0

    var pointList: MutableList<TsLineData> = mutableListOf()
    var tradeTime: MutableList<TradeTimeBean> = mutableListOf()

    companion object{
        fun getFromCache(id: Int): TsHisBean? {
            return CacheMemoryStaticUtils.get("TsHisBean_$id")
        }
    }

    fun saveToCache(id: Int = quoteId) {
        CacheMemoryStaticUtils.put("TsHisBean_$id", this)
    }
}

class TsLineData(): BaseQuoteData() {
    var avgPrice = 0.0

    /** 副图指标 */
    var macd: MACD = MACD()

    constructor(quoteBean: QuoteBean):this(){
        time = quoteBean.recentTime
        high = quoteBean.highPrice
        low = quoteBean.lowPrice
        open = quoteBean.openPrice
        close = quoteBean.currentPrice
        amount = quoteBean.holding
        holding = quoteBean.holding
        volume = quoteBean.vol.toDouble()
    }

    constructor(data: TsLineData, newTime: Long = data.time):this(){
        time = newTime
        high = data.high
        low = data.low
        open = data.open
        close = data.close
        volume = data.volume
        amount = data.amount

        avgPrice = data.avgPrice
        macd = data.macd
    }
}

class TradeTimeBean{
    /** 开始时间 */
    var start: Long = 0
    /** 结束时间 */
    var end: Long = 0

    private val timeAll = mutableListOf<Long>()

    fun getAllTime(): List<Long>{
        if(timeAll.isEmpty()){
            val count = (end - start)/ONE_MINUTE
            for (index in 0..count){
                timeAll.add(start + index * ONE_MINUTE)
            }
        }
        return timeAll
    }
}

/**
 * 财经日历
 */
class CalendarBean: Serializable{
    var id = 0
    var calendarDate: String = ""
    var country: String = ""
    var importance = 0
    var publishTime: String = ""
    var quotaName: String = ""
    var previousValue: String = ""
    var forecastValue: String = ""
    var publishValue: String = ""
    var effectGold = 0
    var effectOil = 0
    var countryCode: String = ""
    var time: Long = 0
}