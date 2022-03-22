package com.zjd.unite.chart.constant

/**
 * @author ZJD
 * @date 2021/6/7
 * @desc
 **/
interface Constant {
    /** 模拟交易 */
    interface Simulate{
        companion object{
            //外盘
            const val TYPE_OUTER = 1
            //TD
            const val TYPE_TD = 2
            //期货
            const val TYPE_FUTURES = 3

            //交易轨迹
            const val PATH_TRACE = 0
            //成本线
            const val PATH_COST = 1
            //买卖点
            const val PATH_DEAL = 2
        }
    }
    
    interface KLineSettings{
        companion object {
            const val NAME_JIJIN = "集金策略"
            const val ID_JIJIN = 1
            const val NAME_QVSHI = "趋势先锋"
            const val ID_QVSHI = 2
            const val NAME_DUGU = "独孤九剑"
            const val ID_DUGU = 3
            const val NAME_MA = "MA"
            const val ID_MA = 4
            const val NAME_BOLL = "BOLL"
            const val ID_BOLL = 5
            const val NAME_SAR = "SAR"
            const val ID_SAR = 6
            const val NAME_EXPMA = "EXPMA"
            const val ID_EXPMA = 7
            const val NAME_MACD = "MACD"
            const val ID_MACD = 8
            const val NAME_KDJ = "KDJ"
            const val ID_KDJ = 9
            const val NAME_RSI = "RSI"
            const val ID_RSI = 10
            const val NAME_VOL = "VOL"
            const val ID_VOL = 11
            const val NAME_BIAS = "BIAS"
            const val ID_BIAS = 12
            const val NAME_W_R = "W&R"
            const val ID_W_R = 13
            const val NAME_OBV = "OBV"
            const val ID_OBV = 14
            const val NAME_DMI = "DMI"
            const val ID_DMI = 15
            const val NAME_CCI = "CCI"
            const val ID_CCI = 16
            const val NAME_CR = "CR"
            const val ID_CR = 17
            const val NAME_PSY = "PSY"
            const val ID_PSY = 18
            const val NAME_TRIX = "TRIX"
            const val ID_TRIX = 19
            const val NAME_MABOLL = "MABOLL"
            const val ID_MABOLL = 20

            const val MA_MA1 = "MA1"
            const val MA_MA1_DEF = 5
            const val MA_MA2 = "MA2"
            const val MA_MA2_DEF = 10
            const val MA_MA3 = "MA3"
            const val MA_MA3_DEF = 20
            const val MA_MA4 = "MA4"
            const val MA_MA4_DEF = 30
            const val MA_MA5 = "MA5"
            const val MA_MA5_DEF = 60

            const val MABOLL_MA1_DEF = 5
            const val MABOLL_MA2_DEF = 10
            const val MABOLL_MA3_DEF = 20
            const val MABOLL_MA4_DEF = 0
            const val MABOLL_MA5_DEF = 0

            const val BOLL_N = "N"
            const val BOLL_N_DEF = 20
            const val BOLL_K = "K"
            const val BOLL_K_DEF = 2

            const val EXPMA_N1 = "N1"
            const val EXPMA_N1_DEF = 12
            const val EXPMA_N2 = "N2"
            const val EXPMA_N2_DEF = 50

            const val MACD_SHORT = "SHORT"
            const val MACD_SHORT_DEF = 12
            const val MACD_LONG = "LONG"
            const val MACD_LONG_DEF = 26
            const val MACD_M = "M"
            const val MACD_M_DEF = 9

            const val KDJ_N = "N"
            const val KDJ_N_DEF = 9
            const val KDJ_M1 = "M1"
            const val KDJ_M1_DEF = 3
            const val KDJ_M2 = "M2"
            const val KDJ_M2_DEF = 3

            const val RSI_N1 = "N1"
            const val RSI_N1_DEF = 6
            const val RSI_N2 = "N2"
            const val RSI_N2_DEF = 12
            const val RSI_N3 = "N3"
            const val RSI_N3_DEF = 24

            const val PSY_N = "N"
            const val PSY_N_DEF = 12
            const val PSY_M = "M"
            const val PSY_M_DEF = 6

            const val TRIX_N = "N"
            const val TRIX_N_DEF = 12
            const val TRIX_M = "M"
            const val TRIX_M_DEF = 20
        }
    }
}