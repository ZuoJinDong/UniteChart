package com.zjd.unite.chart.entity

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