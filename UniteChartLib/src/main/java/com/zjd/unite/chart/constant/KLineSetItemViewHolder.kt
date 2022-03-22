package com.zjd.unite.chart.constant

import android.graphics.Color

/**
 * @param tag 指标名
 * @param name 该指标的具体设置名
 * @param i 该设置在指标设置中的索引
 */
class KLineSetItemViewHolder() {
    companion object {

        val MA = intArrayOf(Constant.KLineSettings.MA_MA1_DEF, Constant.KLineSettings.MA_MA2_DEF
                , Constant.KLineSettings.MA_MA3_DEF, Constant.KLineSettings.MA_MA4_DEF, Constant.KLineSettings.MA_MA5_DEF)
        val MA_NAME = arrayOf(Constant.KLineSettings.MA_MA1, Constant.KLineSettings.MA_MA2
                , Constant.KLineSettings.MA_MA3, Constant.KLineSettings.MA_MA4, Constant.KLineSettings.MA_MA5)
        val MA_RANGE = arrayOf("参考范围:[0-250,0为不显示]", "参考范围:[0-250,0为不显示]", "参考范围:[0-250,0为不显示]", "参考范围:[0-250,0为不显示]", "参考范围:[0-250,0为不显示]")
        val MA_START_END = arrayOf(0, 250, 0, 250, 0, 250, 0, 250, 0, 250)
        val MA_COLOR = arrayOf(
                Color.parseColor("#4796FF"),
                Color.parseColor("#FF4747"),
                Color.parseColor("#FF911A"),
                Color.parseColor("#0042FF"),
                Color.parseColor("#00EDBE"))


        val BOLL = intArrayOf(Constant.KLineSettings.BOLL_N_DEF, Constant.KLineSettings.BOLL_K_DEF)
        val BOLL_NAME = arrayOf(Constant.KLineSettings.BOLL_N, Constant.KLineSettings.BOLL_K)
        val BOLL_RANGE = arrayOf("参考范围:[5-250]", "参考范围:[1-10]")
        val BOLL_START_END = arrayOf(5, 250, 1, 10)
        val BOLL_COLOR = arrayOf(Color.parseColor("#9F9F9F"), Color.parseColor("#E5C220"), Color.parseColor("#c750f8"))

        val EXPMA = intArrayOf(Constant.KLineSettings.EXPMA_N1_DEF, Constant.KLineSettings.EXPMA_N2_DEF)
        val EXPMA_NAME = arrayOf(Constant.KLineSettings.EXPMA_N1, Constant.KLineSettings.EXPMA_N2)
        val EXPMA_RANGE = arrayOf("参考范围:[1-250]", "参考范围:[1-250]")
        val EXPMA_START_END = arrayOf(1, 250, 1, 250)
        val EXPMA_COLOR = arrayOf(Color.parseColor("#4795ff"), Color.parseColor("#cf6ff6"))

        val MACD = intArrayOf(Constant.KLineSettings.MACD_SHORT_DEF, Constant.KLineSettings.MACD_LONG_DEF, Constant.KLineSettings.MACD_M_DEF)
        val MACD_NAME = arrayOf(Constant.KLineSettings.MACD_SHORT, Constant.KLineSettings.MACD_LONG, Constant.KLineSettings.MACD_M)
        val MACD_RANGE = arrayOf("参考范围:[5-40]", "参考范围:[10-100]", "参考范围:[2-40]")
        val MACD_START_END = arrayOf(5, 40, 10, 100, 2, 40)

        val KDJ = intArrayOf(Constant.KLineSettings.KDJ_N_DEF, Constant.KLineSettings.KDJ_M1_DEF, Constant.KLineSettings.KDJ_M2_DEF)
        val KDJ_NAME = arrayOf(Constant.KLineSettings.KDJ_N, Constant.KLineSettings.KDJ_M1, Constant.KLineSettings.KDJ_M2)
        val KDJ_RANGE = arrayOf("参考范围:[1-100]", "参考范围:[2-40]", "参考范围:[2-40]")
        val KDJ_START_END = arrayOf(1, 100, 2, 40, 2, 40)

        val PSY = intArrayOf(Constant.KLineSettings.PSY_N_DEF, Constant.KLineSettings.PSY_M_DEF)
        val PSY_NAME = arrayOf(Constant.KLineSettings.PSY_N, Constant.KLineSettings.PSY_M)
        val PSY_START_END = arrayOf(1, 100, 1, 100)

        val TRIX = intArrayOf(Constant.KLineSettings.TRIX_N_DEF, Constant.KLineSettings.TRIX_M_DEF)
        val TRIX_NAME = arrayOf(Constant.KLineSettings.TRIX_N, Constant.KLineSettings.TRIX_M)
        val TRIX_START_END = arrayOf(1, 100, 1, 100)

        val RSI = intArrayOf(Constant.KLineSettings.RSI_N1_DEF, Constant.KLineSettings.RSI_N2_DEF, Constant.KLineSettings.RSI_N3_DEF)
        val RSI_NAME = arrayOf(Constant.KLineSettings.RSI_N1, Constant.KLineSettings.RSI_N2, Constant.KLineSettings.RSI_N3)
        val RSI_RANGE = arrayOf("参考范围:[2-100]", "参考范围:[2-100]", "参考范围:[2-100]")
        var RSI_START_END = arrayOf(2, 100, 2, 100, 2, 100)
        var RSI_COLOR = arrayOf(Color.parseColor("#3ec1d3"), Color.parseColor("#ffa41a"),
                Color.parseColor("#f64bfc"))


    }
}