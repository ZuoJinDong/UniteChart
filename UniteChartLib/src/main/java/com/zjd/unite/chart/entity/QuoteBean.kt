package com.zjd.unite.chart.entity

/**
 * @author ZJD
 * @date 2021/5/18
 * @desc
 **/
class QuoteBean {
    /** 商品id */
    var id: Int = 0
    /** 商品所属板块id */
    var boardId: Int = 0
    /** 商品名 */
    var name: String = ""
    /** 代码名 */
    var code: String = ""
    /** 现价 */
    var currentPrice: Double = -1.0
    /** 昨收价 */
    var yesterdayClosePrice: Double = 0.0
    /** 昨结价 */
    var yesterdaySettlePrice: Double = 0.0
    /** 涨跌价 */
    var zhangDiePrice: Double = 0.0
    /** 涨跌幅，是一个百分数，使用时需要乘以100 才是百分号左边的数字 */
    var zhangDiePricePercent: Double = 0.0
    /** 小数点位数 */
    var decPointCount: Int = 0
    /** 市场状态 */
    var marketStatus: Int = 0
    /** 最近交易时间？报价时间？ */
    var recentTime: Long = 0
    /** 卖价 */
    var sellPrice: Double = 0.0
    /** 买价 */
    var buyPrice: Double = 0.0
    /** 卖出量，整数，非金额 */
    var sellVol: Long = 0
    /** 买入量，整数，非金额 */
    var buyVol: Long = 0
    /** 开盘价 */
    var openPrice: Double = 0.0
    /** 最高价 */
    var highPrice: Double = 0.0
    /** 最低价 */
    var lowPrice: Double = 0.0
    /** 合约手数 */
    var contractSize: Int = 0
    /** 买1 */
    var buy1: Double = 0.0
    /** 卖1 */
    var sell1: Double = 0.0
    /** 跌停价 */
    var limitMin: Double = 0.0
    /** 涨停价 */
    var limitMax: Double = 0.0


}