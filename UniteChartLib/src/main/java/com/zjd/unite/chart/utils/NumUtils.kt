package com.zjd.unite.chart.utils

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.NumberFormat
import kotlin.math.abs

/**
 * @author ZJD
 * @date 2021/4/7
 * @desc 数字相关工具
 **/

/**
 * 保留小数
 * 不使用科学计数器
 */
fun formatDouble(value: Double, dec: Int = 2): String {
    return NumberFormat.getInstance().apply {
        //设置不使用科学计数器
        isGroupingUsed = false
        //小数点最大位数
        maximumFractionDigits= dec
    }.format(value).replace("NaN","")
}

/**
 * 使用单位简化数字
 */
fun formatDoubleUnit(value: Double, dec: Int = 2): String {
    return when{
        abs(value) >= 10000 -> {
            formatDouble(value/10000, 2) + "万"
        }
        else -> formatDouble(value, dec)
    }
}

/**
 * 提供精确的小数位四舍五入处理
 * @param value 需要四舍五入的数字
 * @param scale 小数点后保留几位
 */
fun round(value: Double, scale: Int): Double {
    return if(scale < 0){
        value
    }else{
        val b = BigDecimal(value.toString())
        val one = BigDecimal("1")
        b.divide(one, scale, BigDecimal.ROUND_HALF_UP).toDouble()
    }
}

fun roundStr(value: Double, scale: Int): String {
    return if(scale < 0){
        value.toString()
    }else{
        val b = BigDecimal(value.toString())
        val one = BigDecimal("1")
        b.divide(one, scale, BigDecimal.ROUND_HALF_UP).toString()
    }
}

/**
 * 集合最大值
 */
fun getMax(vararg num: Double, withZero: Boolean = true): Double{
    return if(withZero){
        num.filter { !it.isNaN()}.maxOf { it }
    }else{
        num.filter { !it.isNaN() && it != 0.0 }.maxOf { it }
    }
}

/**
 * 集合最小值
 */
fun getMin(vararg num: Double, withZero: Boolean = true): Double{
    return if(withZero){
        num.filter { !it.isNaN()}.minOf { it }
    }else{
        num.filter { !it.isNaN() && it != 0.0 }.minOf { it }
    }
}

/**
 * 连加
 */
fun add(v1: Double, vararg v2s: Double): Double {
    var b1 = BigDecimal(v1.toString())
    for (d in v2s) {
        val b2 = BigDecimal(d.toString())
        b1 = b1.add(b2)
    }
    return b1.toDouble()
}

/**
 * 连减
 */
fun sub(d1: Double, vararg num: Double): Double{
    var b1 = BigDecimal(d1.toString())
    for (d in num) {
        val b2 = BigDecimal(d.toString())
        b1 = b1.subtract(b2)
    }
    return b1.toDouble()
}

/**
 * 连乘
 */
fun mul(v1: Double, vararg v2s: Double): Double {
    var b1 = BigDecimal(v1.toString())
    for (d in v2s) {
        val b2 = BigDecimal(d.toString())
        b1 = b1.multiply(b2)
    }
    return b1.toDouble()
}

private val DEFAULT_MATH_CONTEXT = MathContext(8, RoundingMode.HALF_UP)

/**
 * 连除
 */
fun div(v1: Double, vararg v2s: Double, mc: MathContext = DEFAULT_MATH_CONTEXT): Double {
    var b1 = BigDecimal(v1)
    for (d in v2s) {
        val b2 = BigDecimal(d)
        b1 = b1.divide(b2, mc.precision, mc.roundingMode)
    }
    return b1.toDouble()
}

fun div(d1: Double, d2: Double): Double {
    return if(d1 == 0.0 || d2 == 0.0){
        0.0
    }else{
        d1 / d2
    }
}