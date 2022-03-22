package com.zjd.unite.chart.entity

/**
 * @author ZJD
 * @date 2021/8/16
 * @desc
 **/

data class QuoteSignEntry(
    val name: String,
    val params: Params,
    val period: String,
    val symbol: String,
    val time: Long,
    val value: Value?) {

    data class Params(
        val account: Account?,
        val buyStep: Int,
        val sellStep: Int) {

        data class Account(
            val balance: Int,
            val risk: Double)
    }

    data class Value(
        val hold: Hold?,
        val single: List<Single>) {

        data class Hold(
            val fluctuation: Double,
            val hold: Int,
            val income: Double,
            val period: Int,
            val unit: Int)

        data class Single(
            val direction: String,
            val price: Double,
            val time: Long,
            val type: String,
            val volume: Int){

            fun isUp(): Boolean{
                return type == "open" && direction == "more" ||
                        type == "clean" && direction == "less" ||
                        type == "cover" && direction == "more"
            }

            fun getTypeStr(): String?{
                return when{
                    type == "open" && direction == "more" -> "多"
                    type == "open" && direction == "less" -> "空"
                    type == "clean" && direction == "more" -> "平多"
                    type == "clean" && direction == "less" -> "平空"
                    type == "cover" && direction == "more" -> "补多"
                    type == "cover" && direction == "less" -> "补空"
                    else -> null
                }
            }
        }
    }
}