package com.zjd.unite.chart.utils

import android.util.Log
import com.zjd.unite.chart.constant.ChartConstant
import com.zjd.unite.chart.entity.*
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * 数据辅助类
 * 计算macd rsi等
 */
object DataFormatHelper {

    /**
     * 计算分时指标
     */
    fun calculateTs(dataList: List<TsLineData>, quoteId: Int, contractSize: Int){
        calculateAVG(dataList, contractSize)
        calculateMACDFS(dataList)
    }

    private fun calculateAVG(dataList: List<TsLineData>, contractSize: Int) {
        if(contractSize != 0){
            var totalValue = 0.0
            var totalVol = 0.0

            dataList.forEach { bean ->
                totalValue += bean.holding
                totalVol += bean.volume

                if(totalVol == 0.0){
                    bean.avgPrice = bean.close
                }else {
                    bean.avgPrice = totalValue / totalVol / contractSize
                }
            }
        }
    }

    /**
     * 计算macd
     */
    private fun calculateMACDFS(dataList: List<TsLineData>) {
        val defParam = ChartParams.PARAM_ASSIST_MACD

        var ema12 = 0.0
        var ema26 = 0.0
        var dif: Double
        var dea = 0.0
        var macd: Double

        for (i in dataList.indices) {
            val point = dataList[i]
            val closePrice = point.close
            if (i == 0) {
                ema12 = closePrice
                ema26 = closePrice
            } else {
                // EMA（12） = 前一日EMA（12） X 11/13 + 今日收盘价 X 2/13
                ema12 = ema12 * (defParam[0] - 1) / (defParam[0] + 1) + closePrice * 2f / (defParam[0] + 1)
                // EMA（26） = 前一日EMA（26） X 25/27 + 今日收盘价 X 2/27
                ema26 = ema26 * (defParam[1] - 1) /(defParam[1] + 1) + closePrice * 2f / (defParam[1] + 1)
            }
            // DIF = EMA（12） - EMA（26） 。
            // 今日DEA = （前一日DEA X 8/10 + 今日DIF X 2/10）
            // 用（DIF-DEA）*2即为MACD柱状图。
            dif = ema12 - ema26
            dea = dea * (defParam[2] - 1) / (defParam[2] + 1) + dif * 2f / (defParam[2] + 1)
            macd = (dif - dea) * 2f

            point.macd.let {
                it.dif = dif
                it.dea = dea
                it.macd = macd

                it.maxValue = getMax(dea, dif, macd)
                it.minValue = getMin(dea, dif, macd)
            }
        }
    }

    /**
     * 计算K线指标
     */
    fun calculateK(dataList: List<KLineData>): Double {
        var start = System.currentTimeMillis()
        calculateMA(dataList)
        Log.d("calculate=====MA=", "${System.currentTimeMillis() - start}")

        start = System.currentTimeMillis()
        calculateBOLL(dataList)
        Log.d("calculate=====BOLL=", "${System.currentTimeMillis() - start}")

        start = System.currentTimeMillis()
        calculateSAR(dataList)
        Log.d("calculate=====SAR=", "${System.currentTimeMillis() - start}")

        start = System.currentTimeMillis()
        calculateEXPMA(dataList)
        Log.d("calculate=====EXPMA=", "${System.currentTimeMillis() - start}")

        start = System.currentTimeMillis()
        calculateQSXF(dataList)
        Log.d("calculate=====QSXF=", "${System.currentTimeMillis() - start}")

        start = System.currentTimeMillis()
        calculateJJCL(dataList)
        Log.d("calculate=====JJCL=", "${System.currentTimeMillis() - start}")


        start = System.currentTimeMillis()
        calculateMACD(dataList)
        Log.d("calculate=====MACD=", "${System.currentTimeMillis() - start}")

        start = System.currentTimeMillis()
        calculateKDJ(dataList)
        Log.d("calculate=====KDJ=", "${System.currentTimeMillis() - start}")

        start = System.currentTimeMillis()
        calculateRSI(dataList)
        Log.d("calculate=====RSI=", "${System.currentTimeMillis() - start}")

        start = System.currentTimeMillis()
        calculateWR(dataList)
        Log.d("calculate=====WR=", "${System.currentTimeMillis() - start}")

        start = System.currentTimeMillis()
        calculateBIAS(dataList)
        Log.d("calculate=====BIAS=", "${System.currentTimeMillis() - start}")

        start = System.currentTimeMillis()
        calculateTRIX(dataList)
        Log.d("calculate=====TRIX=", "${System.currentTimeMillis() - start}")

        start = System.currentTimeMillis()
        calculatePSY(dataList)
        Log.d("calculate=====PSY=", "${System.currentTimeMillis() - start}")

        start = System.currentTimeMillis()
        calculateOBV(dataList)
        Log.d("calculate=====OBV=", "${System.currentTimeMillis() - start}")

        start = System.currentTimeMillis()
        calculateDMI(dataList)
        Log.d("calculate=====DMI=", "${System.currentTimeMillis() - start}")

        start = System.currentTimeMillis()
        calculateCCI(dataList)
        Log.d("calculate=====CCI=", "${System.currentTimeMillis() - start}")

        start = System.currentTimeMillis()
        calculateCR(dataList)
        Log.d("calculate=====CR=", "${System.currentTimeMillis() - start}")

        start = System.currentTimeMillis()
        calculateVolumeMA(dataList).let {
            Log.d("calculate=====VolumeMA=", "${System.currentTimeMillis() - start}")
            return it
        }

    }

    /**
     * 计算CR
     */
    private fun calculateCR(dataList: List<KLineData>) {
        val defParam = ChartParams.PARAM_ASSIST_CR

        val n = 26

        val tempCr1 = mutableListOf<Double>()
        var sumCr1 = 0.0
        val tempCr2 = mutableListOf<Double>()
        var sumCr2 = 0.0

        val temp5 = mutableListOf<Double>()
        var sum5 = 0.0
        val temp10 = mutableListOf<Double>()
        var sum10 = 0.0
        val temp20 = mutableListOf<Double>()
        var sum20 = 0.0

        dataList.forEachIndexed { index, kLine ->
            val mid = if(index < 1) 0.0 else dataList[index - 1].run {
                (close + high + low)/3.0
            }

            var temp = if(index == 0) 0.0 else kLine.high - mid
            tempCr1.add(max(temp, 0.0))
            sumCr1 += tempCr1.last()
            if(tempCr1.size > n){
                sumCr1 -= tempCr1.removeFirst()
            }
            val cr1 = sumCr1


            temp = if(index == 0) 0.0 else mid - kLine.low
            tempCr2.add(max(temp, 0.0))
            sumCr2 += tempCr2.last()
            if(tempCr2.size > n){
                sumCr2 -= tempCr2.removeFirst()
            }
            val cr2 = sumCr2
            val cr = div(cr1, cr2)*100

            temp5.add(cr)
            temp10.add(cr)
            temp20.add(cr)
            sum5 += temp5.last()
            sum10 += temp10.last()
            sum20 += temp20.last()

            if(temp5.size > defParam[0]){
                sum5 -= temp5.removeFirst()
            }
            if(temp10.size > defParam[1]){
                sum10 -= temp10.removeFirst()
            }
            if(temp20.size > defParam[2]){
                sum20 -= temp20.removeFirst()
            }

            val ma5 = sum5/temp5.size
            val ma10 = sum10/temp10.size
            val ma20 = sum20/temp20.size

            kLine.cr.apply {
                this.cr = cr
                this.ma5 = ma5
                this.ma10 = ma10
                this.ma20 = ma20

                minValue = getMin(cr, ma5, ma10, ma20)
                maxValue = getMax(cr, ma5, ma10, ma20)
            }
        }
    }

    /**
     * 计算CCI
     */
    private fun calculateCCI(dataList: List<KLineData>) {

        val temp = mutableListOf<Double>()
        var sum = 0.0

        dataList.forEachIndexed { index, kLine ->
            val typ = (kLine.high + kLine.low + kLine.close)/3.0
            temp.add(typ)
            sum += temp.last()

            if(temp.size > 14){
                sum -= temp.removeFirst()
            }

            val ave = sum/temp.size
            val t1 = typ - ave
            val t2 = (temp.sumOf { abs(ave - it) }/temp.size)*0.015
            val cci = if(t2 == 0.0){
                0.0
            }else{
                t1/t2
            }

            kLine.cci.apply {
                this.cci = cci

                minValue = cci
                maxValue = cci
            }
        }
    }

    /**
     * 计算DMI
     */
    private fun calculateDMI(dataList: List<KLineData>) {

        //前一日真实幅度TR
        var preTrr = 0.0
        var preHigh = 0.0
        var preLow = 0.0
        var preClose = 0.0

        var preHdTmp = 0.0
        var preLdTmp = 0.0

        var preAdx = 0.0

        dataList.forEachIndexed { index, kLine ->
            // 当日最高价 - 当日最低价 的差
            val diffHL = kLine.high - kLine.low
            // 当日最高价 - 前一日收盘价的差，取绝对值
            val diff2 = abs(kLine.high - preClose)
            // 当日最低价 - 前一日收盘价 ，取绝对值
            val diffLow = abs(kLine.low - preClose)
            // 比较 diffHL,diff2, diffLow,取三者的最大值，
            val maxDiff = getMax(diffHL, diff2, diffLow)
            // 得出真实幅度TR
            val trr = sma(maxDiff, 14, 1, preTrr)

            // 以下是计算当日动向指数
            //上升动向(+DM)，= 当日最高价 - 前一日最高价
            val hd = if(index == 0) 0.0 else kLine.high - preHigh
            //降动向(-DM) = 前一日最低价 - 当日最低价
            val ld = if(index == 0) 0.0 else preLow - kLine.low


            val hdTmp = if(hd > 0 && hd > ld) hd else 0.0
            val dmp = sma(hdTmp, 14, 1, preHdTmp)

            val ldTmp = if(ld > 0 && ld > hd) ld else 0.0
            val dmm = sma(ldTmp, 14, 1, preLdTmp)

            val pdi = dmp*100.0/trr
            val mdi = dmm*100.0/trr

            // 以下是计算动向平均数ADX ，
            // DX = DI(diff) / DI(sum) *100;
            // DI(diff): 即PDI-MDI的差的绝对值
            // DI(sum): 即 PDI+MDI的和
            // ADX 就是DX 一定周期N的移动平均值

            // MDI-PDI 的绝对值
            val subTmp = abs(mdi - pdi)
            // MDI+PDI
            val plusTmp = mdi + pdi
            val tt = if(plusTmp == 0.0){
                0.0
            }else{
                subTmp*100.0/plusTmp
            }
            // ADX 就是DX 一定周期N的移动平均值
            val adx = sma(tt, 14, 1, preAdx)

            // ADXR = (当日的ADX - 前M日的ADX) /2 ;
            val adxr = if(index > 5){
                (dataList[index - 6].dmi.adx + adx)/2.0
            }else{
                adx/2.0
            }

            //记录
            preHdTmp = dmp
            preLdTmp = dmm
            preHigh = kLine.high
            preLow = kLine.low
            preTrr = trr
            preClose = kLine.close
            preAdx = adx

            kLine.dmi.apply {
                this.pdi = pdi
                this.mdi = mdi
                this.adx = adx
                this.adxr = adxr

                minValue = getMin(pdi, mdi, adx, adxr)
                maxValue = getMax(pdi, mdi, adx, adxr)
            }
        }
    }

    /**
     * 计算OBV
     */
    private fun calculateOBV(dataList: List<KLineData>) {

        var sum = 0.0

        dataList.forEachIndexed { index, kLine ->

            sum += when {
                index == 0 -> 0.0
                kLine.close > dataList[index - 1].close -> kLine.volume
                kLine.close < dataList[index - 1].close -> -kLine.volume
                else -> 0.0
            }

            kLine.obv.apply {
                this.obv = sum

                minValue = obv
                maxValue = obv
            }
        }
    }

    /**
     * 计算PSY
     */
    private fun calculatePSY(dataList: List<KLineData>) {
        val defParam = ChartParams.PARAM_ASSIST_PSY
        val n = defParam[0]
        val m = defParam[1]

        val temp = mutableListOf<Boolean>()
        var yesterdayClose = 0.0

        val tempPsy = mutableListOf<Double>()
        var psySum = 0.0

        dataList.forEachIndexed { index, kLine ->

            temp.add(kLine.close > yesterdayClose)
            yesterdayClose = kLine.close

            if(temp.size > n){
                temp.removeFirst()
            }
            //PSY=N日内上涨天数/N*100
            val psy = 100.0*(temp.filter { it }.size)/n
            tempPsy.add(psy)
            psySum += tempPsy.last()

            if(tempPsy.size > m){
                psySum -= tempPsy.removeFirst()
            }

            //PSYMA=PSY的M日简单移动平均
            val psyma = psySum/tempPsy.size

            kLine.psy.apply {
                this.psy = psy
                this.psyma = psyma

                minValue = getMin(psy, psyma)
                maxValue = getMax(psy, psyma)
            }
        }

    }

    /**
     * 计算RSI
     */
    private fun calculateRSI(dataList: List<KLineData>) {
        ChartParams.PARAM_ASSIST_RSI.forEachIndexed { index, offset ->
            var rsi: Double?
            var rsiABSEma = 0.0
            var rsiMaxEma = 0.0
            for (i in dataList.indices) {
                val point = dataList[i]
                val closePrice = point.close
                if (i == 0) {
                    rsi = 0.0
                    rsiABSEma = 0.0
                    rsiMaxEma = 0.0
                } else {
                    val Rmax = max(0.0, closePrice - dataList[i - 1].close)
                    val RAbs = abs(closePrice - dataList[i - 1].close)

                    rsiMaxEma = (Rmax + (offset - 1) * rsiMaxEma) / offset
                    rsiABSEma = (RAbs + (offset - 1) * rsiABSEma) / offset
                    rsi = rsiMaxEma / rsiABSEma * 100
                }
                if (i < offset - 1) {
                    rsi = 0.0
                }
                if (rsi.isNaN())
                    rsi = 0.0

                point.rsi.let {
                    when(index){
                        0 -> it.rsi1 = rsi
                        1 -> it.rsi2 = rsi
                        2 -> {
                            it.rsi3 = rsi
                            it.maxValue = getMax(it.rsi1, it.rsi2, it.rsi3)
                            it.minValue = getMin(it.rsi1, it.rsi2, it.rsi3)
                        }
                    }
                }
            }
        }
    }

    /**
     * 计算kdj
     *
     * @param dataList
     */
    private fun calculateKDJ(dataList: List<KLineData>) {
        val defParam = ChartParams.PARAM_ASSIST_KDJ
        var k = 50.0
        var d = 50.0

        dataList.forEachIndexed { i, point ->
            val closePrice = point.close
            var startIndex = i - defParam[0] + 1
            if (startIndex < 0) {
                startIndex = 0
            }
            var max14 = Double.MIN_VALUE
            var min14 = Double.MAX_VALUE
            for (index in startIndex..i) {
                max14 = max(max14, dataList[index].high)
                min14 = min(min14, dataList[index].low)
            }

            if(max14 != min14){
                var rsv: Double? = 100f * (closePrice - min14) / (max14 - min14)
                if (rsv!!.isNaN()) {
                    rsv = 0.0
                }

                k = (rsv + (defParam[1] - 1) * k) / defParam[1]
                d = (k + (defParam[2] - 1) * d) / defParam[2]
            }

            point.kdj.let {
                it.k = k
                it.d = d
                it.j = 3f * k - 2f * d

                it.minValue = getMin(it.k, it.d, it.j)
                it.maxValue = getMax(it.k, it.d, it.j)
            }
        }
    }

    /**
     * 计算wr
     */
    private fun calculateWR(dataList: List<KLineData>) {
        ChartParams.PARAM_ASSIST_WR.forEachIndexed { index, offset ->
            var wr = 10.0
            for (i in dataList.indices) {
                val point = dataList[i]
                var startIndex = i - offset + 1
                if (startIndex < 0) {
                    startIndex = 0
                }
                var max14 = Double.MIN_VALUE
                var min14 = Double.MAX_VALUE

                for (index1 in startIndex..i) {
                    max14 = max(max14, dataList[index1].high)
                    min14 = min(min14, dataList[index1].low)
                }

                if(max14 != min14){
                    wr = if (i < offset - 1)
                        10.0
                    else
                        100 * (max14 - dataList[i].close) / (max14 - min14)
                }

                point.wr.let {
                    when(index){
                        0 -> it.wr1 = wr
                        1 -> {
                            it.wr2 = wr

                            it.minValue = getMin(it.wr1, it.wr2)
                            it.maxValue = getMax(it.wr1, it.wr2)
                        }
                    }
                }
            }
        }
    }

    /**
     * 计算macd
     */
    private fun calculateMACD(dataList: List<KLineData>) {
        val defParam = ChartParams.PARAM_ASSIST_MACD

        var ema12 = 0.0
        var ema26 = 0.0
        var dif: Double
        var dea = 0.0
        var macd: Double

        for (i in dataList.indices) {
            val point = dataList[i]
            val closePrice = point.close
            if (i == 0) {
                ema12 = closePrice
                ema26 = closePrice
            } else {
                // EMA（12） = 前一日EMA（12） X 11/13 + 今日收盘价 X 2/13
                ema12 = ema12 * (defParam[0] - 1) / (defParam[0] + 1) + closePrice * 2f / (defParam[0] + 1)
                // EMA（26） = 前一日EMA（26） X 25/27 + 今日收盘价 X 2/27
                ema26 = ema26 * (defParam[1] - 1) /(defParam[1] + 1) + closePrice * 2f / (defParam[1] + 1)
            }
            // DIF = EMA（12） - EMA（26） 。
            // 今日DEA = （前一日DEA X 8/10 + 今日DIF X 2/10）
            // 用（DIF-DEA）*2即为MACD柱状图。
            dif = ema12 - ema26
            dea = dea * (defParam[2] - 1) / (defParam[2] + 1) + dif * 2f / (defParam[2] + 1)
            macd = (dif - dea) * 2f

            point.macd.let {
                it.dif = dif
                it.dea = dea
                it.macd = macd

                it.maxValue = getMax(dea, dif, macd)
                it.minValue = getMin(dea, dif, macd)
            }
        }
    }

    /**
     * 计算 BOLL
     */
    private fun calculateBOLL(dataList: List<KLineData>) {
        val defParam = ChartParams.PARAM_MAIN_BOLL
        val maList = mutableListOf<Double>()
        var maSum = 0.0
        val n = defParam[0]
        val k = defParam[1]

        dataList.forEachIndexed { i, point ->
            maList.add(point.close)
            maSum += point.close
            if (i < n - 1) {
                point.boll.let {
                    it.mb = 0.0
                    it.up = 0.0
                    it.dn = 0.0
                }
            } else {
                var md = 0.0
                val ma = maSum/maList.size
                maSum -= maList.removeFirst()
                for (j in i - n + 1..i) {
                    val c = dataList[j].close
                    val value = c - ma
                    md += value * value
                }
                md /= (n - 1)
                md = sqrt(md)
                point.boll.let {
                    it.mb = ma
                    it.up = it.mb + k * md
                    it.dn = it.mb - k * md
                }
            }

            point.boll.let {
                it.maxValue = getMax(point.high, it.up, it.mb, it.dn, withZero = false)
                it.minValue = getMin(point.low, it.up, it.mb, it.dn, withZero = false)

                it.mabollMax = getMax(point.high, it.maxValue, point.ma.maxValue, withZero = false)
                it.mabollMin = getMin(point.low, it.minValue, point.ma.minValue, withZero = false)
            }
        }
    }

    /**
     * 计算ma
     */
    private fun calculateMA(dataList: List<KLineData>) {
        val defParam = ChartParams.PARAM_MAIN_MA

        var ma5 = 0.0
        var ma10 = 0.0
        var ma20 = 0.0
        var ma30 = 0.0
        var ma60 = 0.0

        dataList.forEachIndexed { i, point ->
            val closePrice = point.close

            ma5 += closePrice
            ma10 += closePrice
            ma20 += closePrice
            ma30 += closePrice
            ma60 += closePrice

            point.ma.let {
                when {
                    i == defParam[0] - 1 -> it.ma1 = ma5 / defParam[0]
                    i >= defParam[0] -> {
                        ma5 -= dataList[i - defParam[0]].close
                        it.ma1 = ma5 / defParam[0]
                    }
                    else -> it.ma1 = Double.NaN
                }
                when {
                    i == defParam[1] - 1 -> it.ma2 = ma10 / defParam[1]
                    i >= defParam[1] -> {
                        ma10 -= dataList[i - defParam[1]].close
                        it.ma2 = ma10 / defParam[1]
                    }
                    else -> it.ma2 = Double.NaN
                }
                when {
                    i == defParam[2] - 1 -> it.ma3 = ma20 / defParam[2]
                    i >= defParam[2] -> {
                        ma20 -= dataList[i - defParam[2]].close
                        it.ma3 = ma20 / defParam[2]
                    }
                    else -> it.ma3 = Double.NaN
                }
                when {
                    i == defParam[3] - 1 -> it.ma4 = ma30 / defParam[3]
                    i >= defParam[3] -> {
                        ma30 -= dataList[i - defParam[3]].close
                        it.ma4 = ma30 / defParam[3]
                    }
                    else -> it.ma4 = Double.NaN
                }
                when {
                    i == defParam[4] - 1 -> it.ma5 = ma60 / defParam[4]
                    i >= defParam[4] -> {
                        ma60 -= dataList[i - defParam[4]].close
                        it.ma5 = ma60 / defParam[4]
                    }
                    else -> it.ma5 = Double.NaN
                }

                it.maxValue = getMax(point.high, it.ma1, it.ma2, it.ma3, it.ma4, it.ma5)
                it.minValue = getMin(point.low, it.ma1, it.ma2, it.ma3, it.ma4, it.ma5)
            }
        }
    }

    /**
     * SMA 移动平均。
     * 若Y=SMA(X，N，M)，求X的N日指数平滑移动平均。
     * N > M,   N必须大于M !!!
     * 则Y=(M*X+(N-M)*Y’) / N，其中Y’表示上一周期的Y值。
     * N表示N个周期(即N天)，M是权重的参数
     */
    fun sma(todaySMA: Double, N: Int, M: Int, yesterdaySMA: Double): Double {
        return (M * todaySMA + (N - M) * yesterdaySMA) / N
//        return div((mul(M.toDouble(), todaySMA) + mul((N - M).toDouble(), yesterdaySMA)), N.toDouble())
    }

    /**
     * EMA 指数平滑移动平均线。 若Y=EMA(X，N)，求X的N日指数平滑移动平均。
     * 则Y=［2*X+(N-1)*Y’］/(N+1)，其中Y’表示上一周期的Y值。
     */
    fun ema(todayEMA: Double, days: Int, yesterdayEMA: Double): Double {
        return (2 * todayEMA + (days - 1) * yesterdayEMA) / (days + 1)
    }

    private fun calculateTRIX(dataList: List<KLineData>) {
        val defParam = ChartParams.PARAM_ASSIST_TRIX
        val n = defParam[0]
        val m = defParam[1]

        var preTR1 = 0.0
        var preTR2 = 0.0
        var preTR3 = 0.0

        val tempTrix = mutableListOf<Double>()
        var sumTrix = 0.0

        dataList.forEachIndexed { index, kLine ->

            preTR1 = ema(kLine.close, n, preTR1)
            preTR2 = ema(preTR1, n, preTR2)
            val tr = ema(preTR2, n, preTR3)

            var trix = 0.0
            if(preTR3 != 0.0){
                //TRIX=(TR-昨日TR)/昨日TR*100
                trix = 100.0 * (tr - preTR3) / preTR3
            }
            preTR3 = tr

            tempTrix.add(trix)
            sumTrix += tempTrix.last()
            if(tempTrix.size > m){
                sumTrix -= tempTrix.removeFirst()
            }
            //MATRIX=TRIX的M日简单移动平均
            val matrix = sumTrix/tempTrix.size

            kLine.trix.apply {
                this.trix = trix
                this.matrix = matrix

                minValue = getMin(trix, matrix)
                maxValue = getMax(trix, matrix)
            }
        }
    }

    private fun calculateBIAS(dataList: List<KLineData>) {
        ChartParams.PARAM_ASSIST_BIAS.forEachIndexed { index, offset ->
            val temp = mutableListOf<Double>()
            var sum = 0.0

            dataList.forEach { kLine ->
                temp.add(kLine.close)
                sum += kLine.close

                if(temp.size > offset){
                    sum -= temp.removeFirst()
                }

                (sum/temp.size).let { ma ->
                    100.0*(kLine.close - ma)/ma
                }.let { bias ->
                    kLine.bias.apply {
                        when(index){
                            0 -> bias1 = bias
                            1 -> bias2 = bias
                            2 -> {
                                bias3 = bias

                                minValue = getMin(bias1, bias2, bias3)
                                maxValue = getMax(bias1, bias2, bias3)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun calculateJJCL(dataList: List<KLineData>) {
        dataList.forEachIndexed { index, kLine ->
            kLine.jjcl.apply {
                if(index%4 == 0){
                    type = 0
                }else if(index%4 == 2){
                    type = 1
                }
            }
        }
    }

    private fun calculateQSXF(dataList: List<KLineData>) {
        dataList.forEachIndexed { index, kLine ->
            kLine.qsxf.apply {
                if(index%4 == 1){
                    type = 0
                }else if(index%4 == 3){
                    type = 1
                }
            }
        }
    }

    private fun calculateEXPMA(dataList: List<KLineData>) {
        ChartParams.PARAM_MAIN_EXPMA.forEachIndexed { index, offset ->
            //前一天的EXPMA
            var xp = 0.0
            val param = 2f/(offset + 1)

            dataList.forEachIndexed { i, kLine ->
                //当天的收盘价
                val c = kLine.close

                xp = if(i == 0){
                    c
                }else{
                    (c - xp) * param + xp
                }

                if(index == 0){
                    kLine.expma.n1 = xp
                }else{
                    kLine.expma.n2 = xp
                }

                kLine.expma.maxValue = getMax(kLine.high, kLine.expma.n1, kLine.expma.n2, withZero = false)
                kLine.expma.minValue = getMin(kLine.low, kLine.expma.n1, kLine.expma.n2, withZero = false)
            }
        }
    }

    /**
     * 周期内最大值
     */
    private fun hhvHisData(dataList: List<KLineData>, n: Int): List<Double> {
        val temp: MutableList<Double> = ArrayList()
        val h: MutableList<Double> = ArrayList()
        for (i in dataList.indices) {
            if (i < n) {
                temp.add(dataList[i].high)
            } else {
                temp.add(dataList[i].high)
                temp.removeAt(0)
            }
            h.add(temp.maxOf { it })
        }
        return h
    }

    /**
     * 周期内最小值
     */
    private fun llvHisData(dataList: List<KLineData>, n: Int): List<Double> {
        val temp: MutableList<Double> = ArrayList()
        val l: MutableList<Double> = ArrayList()
        for (i in dataList.indices) {
            if (i < n) {
                temp.add(dataList[i].low)
            } else {
                temp.add(dataList[i].low)
                temp.removeAt(0)
            }
            l.add(temp.minOf { it })
        }
        return l
    }

    /**
     * SAR
     * 计算有误，待优化
     */
    private fun calculateSAR(dataList: List<KLineData>) {
        val n = ChartParams.PARAM_MAIN_SAR[0]

        // 一定周期内的最大值
        val maxEP = hhvHisData(dataList, n)
        // 一定周期内的最小值
        val minEP = llvHisData(dataList, n)

        if(n > 0 && dataList.isNotEmpty()){
            // 标记前一个后奏起是否上涨， 若上涨则为true,下跌则为false
            // 第一个周期默认为上涨
            var isUp = true
            // 步长
            val constStep = ChartParams.PARAM_MAIN_SAR[1] / 100.0
            // 控制步长的上限
            val constMaxp = ChartParams.PARAM_MAIN_SAR[2] / 100.0
            // 加速因子
            var af = constStep
            // List的长度
            val length: Int = dataList.size

            var i = 0
            if(n < length){
                isUp = dataList[n - 1].close > dataList.first().close
            }
            while (i < length) {
                val point = dataList[i]
                point.sar.apply {
                    isSarUp = isUp
                    if(i < n - 1){
                        mid = 0.0
                    }else if(i == n - 1){
                        //第一天
                        mid = if(isUp){
                            minEP[i]
                        }else{
                            maxEP[i]
                        }
                    }else{
                        val sarPre = dataList[i - 1].sar.mid
//                        if (point.close < sarPre) {
//                            if(isUp){
//                                isUp = false
//                                af = constStep
//                            }
//                        }else if(point.close > sarPre){
//                            if(!isUp){
//                                isUp = true
//                                af = constStep
//                            }
//                        }

                        if(isUp){
                            mid = sar(maxEP[i - 1], af, sarPre)

                            if (point.close < sarPre) {
                                // 上涨行情转到下跌行情（某一天的收盘价跌破前一天的SAR）
                                isUp = false
                                af = constStep
                                //直到某一天收盘价跌破SAR，则代表行情转为空头应卖出股票， 而行情转为空头的当天，立即将四天来的最高点，做为次一日的空头SAR
                                if (i < length - 1) {
                                    dataList[i + 1].sar.mid = maxEP[i]
                                    dataList[i + 1].sar.isSarUp = isUp
                                    i++
                                }
                            }
                            else {
                                //只要最高价>前一日最高价，则其乘数因子一律增加0.02， 如果一直累增下去，最高只能累增至0.2为止，之后一律以0.2为乘数因子。
                                if (point.high > dataList[i - 1].high) {
                                    af += constStep
                                    if (af > constMaxp) {
                                        af = constMaxp
                                    }
                                }
                            }
                        }else{
                            mid = sar(minEP[i - 1], af, sarPre)
                            if (point.close > sarPre) { // 下跌行情转到上涨行情（某一天的收盘价突破前一天的SAR）
                                isUp = true
                                af = constStep
                                if (i < length - 1) {
                                    dataList[i + 1].sar.mid = minEP[i]
                                    dataList[i + 1].sar.isSarUp = isUp
                                    i++
                                }
                            } else {
                                if (point.low < dataList[i - 1].low) {
                                    af += constStep
                                    if (af > constMaxp) {
                                        af = constMaxp
                                    }
                                }
                            }
                        }
                    }
                    i++
                }

                point.sar.apply {
                    maxValue = getMax(point.high, mid)
                    minValue = getMin(point.low, mid, withZero = false)
                }
            }
        }
    }

    private fun sar(yesterdayEP: Double, af: Double, yesterdaySAR: Double): Double {
        return yesterdaySAR + af * (yesterdayEP - yesterdaySAR)
    }

    private fun calculateVolumeMA(entries: List<KLineData>): Double {
        var volumeMa5 = 0.0
        var volumeMa10 = 0.0
        var maxVolume = 0.0
        for (i in entries.indices) {
            val entry = entries[i]

            maxVolume = max(entry.volume, maxVolume)

            volumeMa5 += entry.volume
            volumeMa10 += entry.volume

            entry.ma.let {
                when {
                    i == 4 -> it.ma1Volume = volumeMa5 / 5f
                    i > 4 -> {
                        volumeMa5 -= entries[i - 5].volume
                        it.ma1Volume = volumeMa5 / 5f
                    }
                    else -> it.ma1Volume = Double.NaN
                }
                when {
                    i == 9 -> it.ma2Volume = volumeMa10 / 10f
                    i > 9 -> {
                        volumeMa10 -= entries[i - 10].volume
                        it.ma2Volume = volumeMa10 / 10f
                    }
                    else -> it.ma2Volume = Double.NaN
                }
            }
        }
        return maxVolume
    }

    /**
     * 盯盘神器List根据周期转化为Map
     */
    fun trendListToMap(period: String, list: List<QuoteSymbolTrendBean>): MutableMap<Long, MutableList<QuoteSymbolTrendBean>>{
        val map = mutableMapOf<Long, MutableList<QuoteSymbolTrendBean>>()

        val time: Long =  when (period) {
            "m1" -> 60000
            "m5" -> 5*60000
            "m15" -> 15*60000
            "m30" -> 30*60000
            "hr" -> 60*60000
            "day" -> 24*60*60000
            else -> return map
        }

        list.forEach { trend ->
            //转化为周期时间
            val time = if(period == "day"){
                formatTimeUnit(trend.eventTime, ChartConstant.PERIOD_DAY)
            }else{
                (trend.eventTime/time)*time
            }

            if(map[time] == null){
                map[time] = mutableListOf()
            }
            map[time]?.add(trend)
        }

        return map
    }

    fun formatTimeUnit(time: Long, unit: Int): Long{
        val calendar = Calendar.getInstance().apply {
            timeInMillis = time
            when(unit){
                ChartConstant.PERIOD_DAY -> {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                ChartConstant.PERIOD_1_MINUTE -> {
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            }
        }
        return calendar.timeInMillis
    }
}
