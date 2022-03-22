package com.zjd.unite.chart.entity

import android.graphics.RectF

/**
 * @author ZJD
 * @date 2021/4/26
 * @desc K线数据
 **/

/** 指标参数 */
object ChartParams{
    val PARAM_MAIN_MA = intArrayOf(5, 10, 20, 30, 60)
    val PARAM_MAIN_BOLL = intArrayOf(20, 2)
    val PARAM_MAIN_SAR = intArrayOf(4, 2, 20)
    val PARAM_MAIN_EXPMA = intArrayOf(12, 50)

    val PARAM_ASSIST_MACD = intArrayOf(12, 26, 9)
    val PARAM_ASSIST_KDJ = intArrayOf(9, 3, 3)
    val PARAM_ASSIST_RSI = intArrayOf(6, 12, 24)
    val PARAM_ASSIST_BIAS = intArrayOf(6, 12, 24)
    val PARAM_ASSIST_WR = intArrayOf(14, 6)
    val PARAM_ASSIST_TRIX = intArrayOf(12, 20)
    val PARAM_ASSIST_PSY = intArrayOf(12, 6)
    //val PARAM_ASSIST_OBV = intArrayOf(12, 24)
//val PARAM_ASSIST_DMI = intArrayOf(12, 24)
//val PARAM_ASSIST_CCI = intArrayOf(12, 24)
    val PARAM_ASSIST_CR = intArrayOf(5, 10, 20)
}

data class ChartParam(
    val id: Int,
    val isOpen: Int,
    val name: String,
    val param: Map<String, Int>)

class KHisData {
    var clientValue = 0
    var lastPoint2File = 0
    var offset = 0
    var period: String = "day"
    var pointsCount = 0
    var serverTime: Long = 0
    var symbolId = 0
    var time: Long = 0
    var pointList: MutableList<KLineData> = mutableListOf()
}

class KLineData(): BaseQuoteData() {
    /** 主图指标 */
    var ma: MA = MA()
    var boll: BOLL = BOLL()
    var sar: SAR = SAR()
    var expma: EXPMA = EXPMA()
    var qsxf: QSXF = QSXF()
    var jjcl: QSXF = QSXF()

    /** 副图指标 */
    var macd: MACD = MACD()
    var rsi: RSI = RSI()
    var kdj: KDJ = KDJ()
    var wr: WR = WR()
    var bias: BIAS = BIAS()
    var trix: TRIX = TRIX()
    var psy: PSY = PSY()
    var obv: OBV = OBV()
    var dmi: DMI = DMI()
    var cci: CCI = CCI()
    var cr: CR = CR()

    constructor(quote: QuoteBean):this(){
        high = quote.currentPrice
        low = quote.currentPrice
        open = quote.currentPrice
        close = quote.currentPrice
        time = quote.recentTime
        amount = quote.amount
        holding = quote.holding
        volume = quote.vol.toDouble()
    }

    /** 盯盘神器 */
    var trendList: MutableList<QuoteSymbolTrendBean> = mutableListOf()

    /** 交易轨迹 */
    var traceInfo: TraceInfo = TraceInfo()
}

abstract class MaxAndMin{
    /** 最大值 */
    var maxValue = 0.0
    /** 最小值 */
    var minValue = 0.0
}

class MA : MaxAndMin(){
    var ma1: Double = 0.0
    var ma2: Double = 0.0
    var ma3: Double = 0.0
    var ma4: Double = 0.0
    var ma5: Double = 0.0

    var ma1Volume: Double = 0.0
    var ma2Volume: Double = 0.0
}

class BOLL : MaxAndMin(){
    var up: Double = 0.0
    var mb: Double = 0.0
    var dn: Double = 0.0

    var mabollMax = 0.0
    var mabollMin = 0.0
}

class SAR : MaxAndMin(){
    var mid: Double = 0.0
    var isSarUp = true
}

class EXPMA : MaxAndMin(){
    var n1: Double = 0.0
    var n2: Double = 0.0
}

/**
 * 趋势先锋
 */
class QSXF {
    /** -1 无数据 */
    var type = -1

    /**
     * 箭头方向
     */
    fun isUp(): Boolean{
        return when(type){
            0 -> true
            else -> false
        }
    }

    /**
     * 文字
     */
    fun getStr(): String{
        return when(type){
            0 -> "平空"
            else -> "平多"
        }
    }
}

class MACD : MaxAndMin(){
    var dea: Double = 0.0
    var dif: Double = 0.0
    var macd: Double = 0.0
}

class RSI : MaxAndMin(){
    var rsi1: Double = 0.0
    var rsi2: Double = 0.0
    var rsi3: Double = 0.0
}

class KDJ : MaxAndMin(){
    var k: Double = 0.0
    var d: Double = 0.0
    var j: Double = 0.0
}

class WR : MaxAndMin(){
    var wr1: Double = 0.0
    var wr2: Double = 0.0
}

class BIAS : MaxAndMin(){
    var bias1: Double = 0.0
    var bias2: Double = 0.0
    var bias3: Double = 0.0
}

class TRIX : MaxAndMin(){
    var trix: Double = 0.0
    var matrix: Double = 0.0
}

class PSY : MaxAndMin(){
    var psy: Double = 0.0
    var psyma: Double = 0.0
}

class OBV : MaxAndMin(){
    var obv: Double = 0.0
}

class DMI : MaxAndMin(){
    var pdi: Double = 0.0
    var mdi: Double = 0.0
    var adx: Double = 0.0
    var adxr: Double = 0.0
}

class CCI : MaxAndMin(){
    var cci: Double = 0.0
}

class CR : MaxAndMin(){
    var cr: Double = 0.0
    var ma5: Double = 0.0
    var ma10: Double = 0.0
    var ma20: Double = 0.0
}

/**
 * 盯盘神器
 */
class QuoteSymbolTrendBean{
    /**
     * id : 1357.0
     * symbolId : 1135.0
     * period : m30
     * periodUnit : 2001.0
     * eventValue : -18.30317
     * eventType : 3.0
     * eventTime : 1.6197606E12
     * analysis : MACD金叉在0轴下方称为低位金叉，这是价格下跌到一定程度，出现的一次小反弹，代表行情在本周期内有看涨的意愿。
     */

    var id: Int = 0
    var symbolId = 0

    //周期
    var period: String? = null
    var periodUnit = 0.0
    var eventValue: String? = null
    //事件类型(1: 金叉, 2:死叉, 3:MACD金叉, 4:MACD高位金叉, 5:MACD死叉, 6:MACD高位死叉, 7:支撑线, 8:阻力线)
    var eventType = 0
    var eventContent: String? = null
    //事件发生时间
    var eventTime: Long = 0
    //分析
    var analysis: String? = null
    //功能类型(0.涨跌风向标,1.支撑压力探测器)
    var descriptionType = 0
}

/**
 * 交易轨迹
 */
class TradeTraceBean{
    //行情ID 现货和td是 symbolId， 期货的值是 instrumentId
    var smblId: String = ""
    //用户ID
    var accntId: String = ""
    //建仓时间
    var openTime: Long = 0
    //平仓时间
    var closeTime: Long = 0
    //订单id
    var id: Long = 0
    //0 买 1 卖
    var orderType = 0

    /**
     * 注： td和期货交易的交易轨迹，不需要用到价格；
     *
     * 如果openTime不为空，则为开仓时间点；
     *
     * 如果closeTime不为空，则为平仓时间点；
     */
    //建仓价
    var openPrc: Double = 0.0
    //平仓价
    var closePrc: Double = 0.0
}

/**
 * 持仓成本线&交易轨迹
 */
class TradeOrdersTraceBean{
    /** 交易轨迹 */
    var traceList: List<TradeTraceBean>? = null
    /** 持仓成本线 */
    var buy: String = ""
    var sell: String = ""

    fun getBuyDouble(): Double = if(buy.isEmpty()) 0.0 else buy.toDouble()

    fun getSellDouble(): Double = if(sell.isEmpty()) 0.0 else sell.toDouble()
}

/**
 * 交易轨迹
 */
class TraceInfo(var openList: List<TradeTraceBean>? = null,
                var closeList: List<TradeTraceBean>? = null)

/**
 * 模拟交易信息
 */
class SimulateInfo{
    //展示区域
    var rectF: RectF = RectF()
    //价格
    var price = 0.0
    //订单类型
    var orderType = 0
    //坐标x
    var x = 0f
    //坐标y
    var y = 0f
    //订单id
    var id: Long = 0
    //是否为开仓
    var open = false
}

class TracePathInfo(val startX: Float, val startY: Float, val endX: Float, val endY: Float, val color: Int)