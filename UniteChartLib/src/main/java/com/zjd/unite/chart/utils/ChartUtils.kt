package com.zjd.unite.chart.utils

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import com.zjd.unite.chart.chart.*
import com.zjd.unite.chart.entity.*
import com.zjd.unite.chart.R

/**
 * @author ZJD
 * @date 2021/4/28
 * @desc 图标相关
 **/

/**
 * 图表参数
 */
fun setParamsText(tv: TextView, data: TsLineData, chartType: String, yesterdayClose: Double = 0.0, dec: Int = 2){
    val paramBuilder = SpannableStringBuilder()
    when(chartType) {
        MAIN_TYPE_TS -> {
            data.run {
                var colorRes = when{
                    data.avgPrice > yesterdayClose -> R.color.uc_increase
                    data.avgPrice < yesterdayClose -> R.color.uc_decrease
                    else -> R.color.uc_text_light
                }

                paramBuilder.append("均价:")
                formatSpan(tv.context, paramBuilder, formatDouble(data.avgPrice, dec), colorRes)
                paramBuilder.append(" 价位:")
                colorRes = when{
                    data.close > yesterdayClose -> R.color.uc_increase
                    data.close < yesterdayClose -> R.color.uc_decrease
                    else -> R.color.uc_text_light
                }
                val offset = data.close - yesterdayClose
                val percent = 100*offset/yesterdayClose
                formatSpan(tv.context, paramBuilder, "${formatDouble(data.close, dec)} ${formatDouble(offset, dec)} ${formatDouble(percent)}%", colorRes)
            }
        }
        TS_ASSIST_TYPE_VOL -> {
            paramBuilder.append("量: ")
            paramBuilder.append(formatDouble(data.volume, dec))
        }
        TS_ASSIST_TYPE_MACDFS -> {
            data.macd.run {
                formatSpan(tv.context, paramBuilder, "DIFF: ", dif, R.color.macd_dif, 3)
                formatSpan(tv.context, paramBuilder, " DEA: ", dea, R.color.macd_dea, 3)
                formatSpan(tv.context, paramBuilder, " MACD: ", macd, R.color.macd_macd, 3)
            }
        }
    }
    tv.text = paramBuilder
}

fun setParamsText(tv: TextView, data: KLineData, chartType: String){
    val paramBuilder = SpannableStringBuilder()
    when(chartType){
        MAIN_TYPE_MA -> {
            data.ma.run {
                paramBuilder.append("MA")
                formatSpan(tv.context, paramBuilder, " ${ChartParams.PARAM_MAIN_MA[0]}: ", ma1, R.color.ma1)
                formatSpan(tv.context, paramBuilder, " ${ChartParams.PARAM_MAIN_MA[1]}: ", ma2, R.color.ma2)
                formatSpan(tv.context, paramBuilder, " ${ChartParams.PARAM_MAIN_MA[2]}: ", ma3, R.color.ma3)
                formatSpan(tv.context, paramBuilder, " ${ChartParams.PARAM_MAIN_MA[3]}: ", ma4, R.color.ma4)
                formatSpan(tv.context, paramBuilder, " ${ChartParams.PARAM_MAIN_MA[4]}: ", ma5, R.color.ma5)
            }
        }
        MAIN_TYPE_BOLL -> {
            data.boll.run {
                paramBuilder.append("(${ChartParams.PARAM_MAIN_BOLL[0]},${ChartParams.PARAM_MAIN_BOLL[1]}):")
                formatSpan(tv.context, paramBuilder, " MID: ", mb, R.color.boll_md)
                formatSpan(tv.context, paramBuilder, " UPPER:", up, R.color.boll_up)
                formatSpan(tv.context, paramBuilder, " LOWER: ", dn, R.color.boll_dn)
            }
        }
        MAIN_TYPE_MABOLL -> {
//            data.ma.run {
//                paramBuilder.append(" MA")
//                formatSpan(tv.context, paramBuilder, " ${PARAM_MAIN_MA[0]}: ", ma1, R.color.ma1)
//                formatSpan(tv.context, paramBuilder, " ${PARAM_MAIN_MA[1]}: ", ma2, R.color.ma2)
//                formatSpan(tv.context, paramBuilder, " ${PARAM_MAIN_MA[2]}: ", ma3, R.color.ma3)
//                formatSpan(tv.context, paramBuilder, " ${PARAM_MAIN_MA[3]}: ", ma4, R.color.ma4)
//                formatSpan(tv.context, paramBuilder, " ${PARAM_MAIN_MA[4]}: ", ma5, R.color.ma5)
//                paramBuilder.append("\n")
//            }
            data.boll.run {
                paramBuilder.append("(${ChartParams.PARAM_MAIN_BOLL[0]},${ChartParams.PARAM_MAIN_BOLL[1]}):")
                formatSpan(tv.context, paramBuilder, " MID: ", mb, R.color.boll_md)
                formatSpan(tv.context, paramBuilder, " UPPER:", up, R.color.boll_up)
                formatSpan(tv.context, paramBuilder, " LOWER: ", dn, R.color.boll_dn)
            }
        }
        MAIN_TYPE_SAR -> {
            paramBuilder.append("SAR: ${formatDouble(data.sar.mid)}")
        }
        MAIN_TYPE_EXPMA -> {
            data.expma.run {
                formatSpan(tv.context, paramBuilder, "N1(${ChartParams.PARAM_MAIN_EXPMA[0]}): ", n1, R.color.expma_n1)
                formatSpan(tv.context, paramBuilder, " N2(${ChartParams.PARAM_MAIN_EXPMA[1]}): ", n2, R.color.expma_n2)
            }
        }
        MAIN_TYPE_QSXF -> {
            paramBuilder.append("")
        }
        MAIN_TYPE_JJCL -> {
            paramBuilder.append("")
        }

        K_ASSIST_TYPE_VOL -> {
            paramBuilder.append("量: ")
            paramBuilder.append(formatDouble(data.volume, 0))
        }
        K_ASSIST_TYPE_MACD -> {
            paramBuilder.append("(${ChartParams.PARAM_ASSIST_MACD[0]},${ChartParams.PARAM_ASSIST_MACD[1]},${ChartParams.PARAM_ASSIST_MACD[2]}):")
            data.macd.run {
                formatSpan(tv.context, paramBuilder, " DIFF: ", dif, R.color.macd_dif, 3)
                formatSpan(tv.context, paramBuilder, " DEA: ", dea, R.color.macd_dea, 3)
                formatSpan(tv.context, paramBuilder, " MACD: ", macd, R.color.macd_macd, 3)
            }
        }
        K_ASSIST_TYPE_RSI -> ChartParams.PARAM_ASSIST_RSI.let {
            data.rsi.run {
                formatSpan(tv.context, paramBuilder, "RSI${it[0]}: ", rsi1, R.color.rsi1)
                formatSpan(tv.context, paramBuilder, " RSI${it[1]}: ", rsi2, R.color.rsi2)
                formatSpan(tv.context, paramBuilder, " RSI${it[2]}: ", rsi3, R.color.rsi3)
            }
        }


        K_ASSIST_TYPE_KDJ -> {
            paramBuilder.append("(${ChartParams.PARAM_ASSIST_KDJ[0]},${ChartParams.PARAM_ASSIST_KDJ[1]},${ChartParams.PARAM_ASSIST_KDJ[2]}):")
            data.kdj.run {
                formatSpan(tv.context, paramBuilder, " K: ", k, R.color.kdj_k)
                formatSpan(tv.context, paramBuilder, " D: ", d, R.color.kdj_d)
                formatSpan(tv.context, paramBuilder, " J: ", j, R.color.kdj_j)
            }
        }
        K_ASSIST_TYPE_WR -> ChartParams.PARAM_ASSIST_WR.let{
            data.wr.run {
                formatSpan(tv.context, paramBuilder, "WR${it[0]}: ", wr1, R.color.wr_1)
                formatSpan(tv.context, paramBuilder, " WR${it[1]}: ", wr2, R.color.wr_2)
            }
        }
        K_ASSIST_TYPE_TRIX -> {
            paramBuilder.append("(${ChartParams.PARAM_ASSIST_TRIX[0]},${ChartParams.PARAM_ASSIST_TRIX[1]}):")
            data.trix.run {
                formatSpan(tv.context, paramBuilder, " TRIX: ", trix, R.color.trix)
                formatSpan(tv.context, paramBuilder, " MATRIX: ", matrix, R.color.matrix)
            }
        }
        K_ASSIST_TYPE_PSY -> {
            paramBuilder.append("(${ChartParams.PARAM_ASSIST_PSY[0]},${ChartParams.PARAM_ASSIST_PSY[1]}):")
            data.psy.run {
                formatSpan(tv.context, paramBuilder, " PSY: ", psy, R.color.psy)
                formatSpan(tv.context, paramBuilder, " PSYMA: ", psyma, R.color.psyma)
            }
        }
        K_ASSIST_TYPE_BIAS -> {
            data.bias.run {
                formatSpan(tv.context, paramBuilder, "BIAS${ChartParams.PARAM_ASSIST_BIAS[0]}: ", bias1, R.color.bias1)
                formatSpan(tv.context, paramBuilder, " BIAS${ChartParams.PARAM_ASSIST_BIAS[1]}: ", bias2, R.color.bias2)
                formatSpan(tv.context, paramBuilder, " BIAS${ChartParams.PARAM_ASSIST_BIAS[2]}: ", bias3, R.color.bias3)
            }
        }
        K_ASSIST_TYPE_DMI -> {
            data.dmi.run {
                formatSpan(tv.context, paramBuilder, "PDI: ", pdi, R.color.k_indicator_color1)
                formatSpan(tv.context, paramBuilder, " MDI: ", mdi, R.color.k_indicator_color2)
                formatSpan(tv.context, paramBuilder, " ADX: ", adx, R.color.k_indicator_color3)
                formatSpan(tv.context, paramBuilder, " ADXR: ", adxr, R.color.k_indicator_color4)
            }
        }
        K_ASSIST_TYPE_CR -> {
            data.cr.run {
                formatSpan(tv.context, paramBuilder, "CR: ", cr, R.color.k_indicator_color1)
                formatSpan(tv.context, paramBuilder, " MA5: ", ma5, R.color.k_indicator_color2)
                formatSpan(tv.context, paramBuilder, " MA10: ", ma10, R.color.k_indicator_color3)
                formatSpan(tv.context, paramBuilder, " MA20: ", ma20, R.color.k_indicator_color4)
            }
        }
        K_ASSIST_TYPE_OBV -> {
            data.obv.run {
                formatSpan(tv.context, paramBuilder, "OBV: ", obv, R.color.k_indicator_color1)
            }
        }
        K_ASSIST_TYPE_CCI -> {
            data.cci.run {
                formatSpan(tv.context, paramBuilder, "CCI: ", cci, R.color.k_indicator_color1)
            }
        }
    }
    tv.text = paramBuilder
}

private fun formatSpan(context: Context, spanBuilder: SpannableStringBuilder, key: String, value: Double, colorRes: Int, dec: Int = 2){
    val preEnd = spanBuilder.length
    spanBuilder.append(key)
    spanBuilder.append(formatDouble(value, dec))
    spanBuilder.setSpan(ForegroundColorSpan(context.resources.getColor(colorRes)), preEnd, spanBuilder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
}

private fun formatSpan(context: Context, spanBuilder: SpannableStringBuilder, value: String, colorRes: Int){
    val preEnd = spanBuilder.length
    spanBuilder.append(value)
    spanBuilder.setSpan(ForegroundColorSpan(context.resources.getColor(colorRes)), preEnd, spanBuilder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
}