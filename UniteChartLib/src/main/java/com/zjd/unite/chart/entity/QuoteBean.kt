package com.zjd.unite.chart.entity

/**
 * @author ZJD
 * @date 2021/5/18
 * @desc
 **/
class QuoteBean {
    /** 商品id */
    var id: Int = 0
    /** 商品名 */
    var name: String = ""
    /** 现价 */
    var currentPrice: Double = -1.0
    /** 小数点位数 */
    var decPointCount: Int = 0
    /** 最近交易时间？报价时间？ */
    var recentTime: Long = 0
    /** 开盘价 */
    var openPrice: Double = 0.0
    /** 最高价 */
    var highPrice: Double = 0.0
    /** 最低价 */
    var lowPrice: Double = 0.0
    /** 合约手数 */
    var contractSize: Int = 0

    var holding: Double = 0.0

    var amount: Double = 0.0

    var vol: Long = 0

}