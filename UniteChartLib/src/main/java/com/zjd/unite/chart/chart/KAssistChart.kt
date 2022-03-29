package com.zjd.unite.chart.chart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import com.zjd.unite.chart.R
import com.zjd.unite.chart.entity.KLineData
import com.zjd.unite.chart.utils.dp2px
import com.zjd.unite.chart.utils.getColor
import kotlin.math.abs
import kotlin.math.max

/**
 * @author ZJD
 * @date 2021/4/1
 * @desc K线副图
 **/

const val K_ASSIST_TYPE_MACD = "MACD"
const val K_ASSIST_TYPE_RSI = "RSI"
const val K_ASSIST_TYPE_KDJ = "KDJ"
const val K_ASSIST_TYPE_WR = "W&R"
const val K_ASSIST_TYPE_VOL = "VOL"
const val K_ASSIST_TYPE_BIAS = "BIAS"
const val K_ASSIST_TYPE_TRIX = "TRIX"
const val K_ASSIST_TYPE_PSY = "PSY"
const val K_ASSIST_TYPE_OBV = "OBV"
const val K_ASSIST_TYPE_DMI = "DMI"
const val K_ASSIST_TYPE_CCI = "CCI"
const val K_ASSIST_TYPE_CR = "CR"

class KAssistChart @JvmOverloads constructor(mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : BaseChart<KLineData>(mContext, attrs, defStyleAttr) {

    /** 指标 */
    val typeList = listOf(
        K_ASSIST_TYPE_MACD,
        K_ASSIST_TYPE_RSI,
        K_ASSIST_TYPE_KDJ,
        K_ASSIST_TYPE_WR,
        K_ASSIST_TYPE_VOL,
        K_ASSIST_TYPE_CCI,
        K_ASSIST_TYPE_CR,
        K_ASSIST_TYPE_DMI,
        K_ASSIST_TYPE_OBV,
        K_ASSIST_TYPE_TRIX,
        K_ASSIST_TYPE_PSY,
        K_ASSIST_TYPE_BIAS)

    /** 主图指标 */
    var assistType = K_ASSIST_TYPE_MACD

    private var mainChart: KChart? = null

    /** 画笔-线 */
    private var candlePaint = Paint().apply {
        isAntiAlias = true
        color = colorRed
        style = Paint.Style.FILL
        strokeWidth = dp2px(0.5f)
    }

    init {
        initParams()
    }

    /**
     * 初始化参数
     */
    private fun initParams() {
        marginBottom = dp2px(5f)
        textPaint.textSize = textSizeDef
        listOf("80","50","20").forEach {
            val bound = Rect()
            textPaint.getTextBounds(it, 0, it.length, bound)
            yLeftValue.add(ChartXYValue(it, dp2px(3f), bound.height() + dp2px(1f), bound))
        }
    }

    /**
     * 刷新控件
     */
    fun refreshView() {
        mainChart?.let {

            if(it.showCross){
                if(canvasBitmap == null){
                    canvasBitmap = getBackgroundBitmap()
                }
            }else{
                canvasBitmap = null
            }
            showCross = it.showCross
            invalidate()
        }
    }

    /**
     * 切换指标
     */
    @Suppress("UNCHECKED_CAST")
    fun switchAssist(assistIndex: String? = null){
        assistIndex?.let {
            assistType = it
        }?:let {
            var index = typeList.indexOf(assistType)
            if(index >= typeList.size - 1)
                index = -1
            assistType = typeList[index+1]
        }
        formatMaxAndMin()
        mainChart?.postInvalidate()
    }

    fun chartClick(){
        mSingleClick?.onChartSingleClick(this as BaseChart<Any>)
    }

    override fun drawDashLineY(canvas: Canvas) {
        linePaint.pathEffect = dashPathEffect
        when(assistType){
            K_ASSIST_TYPE_TRIX,
            K_ASSIST_TYPE_BIAS,
            K_ASSIST_TYPE_OBV,
            K_ASSIST_TYPE_DMI,
            K_ASSIST_TYPE_CCI,
            K_ASSIST_TYPE_CR,
            K_ASSIST_TYPE_VOL -> {
                canvas.drawLine(0f, chartHeight/2, chartWidth, chartHeight/2, linePaint)
            }
            K_ASSIST_TYPE_KDJ,
            K_ASSIST_TYPE_WR,
            K_ASSIST_TYPE_PSY,
            K_ASSIST_TYPE_RSI -> {
                yLeftValue.forEach {
                    val dashY = getValueY(it.value.toDouble())
                    canvas.drawLine(0f, dashY, chartWidth, dashY, linePaint)
                }
            }
        }
        linePaint.pathEffect = null
    }

    override fun drawYLeftValue(canvas: Canvas) {
        textPaint.textSize = textSizeDef
        textPaint.color = textColor
        when(assistType){
            K_ASSIST_TYPE_KDJ,
            K_ASSIST_TYPE_WR,
            K_ASSIST_TYPE_PSY,
            K_ASSIST_TYPE_RSI -> {
                yLeftValue.forEach {
                    it.apply {
                        canvas.drawText(value, x, y + getValueY(value.toDouble()), textPaint)
                    }
                }
            }
        }
    }

    override fun drawYRightValue(canvas: Canvas) {
        when(assistType){
            K_ASSIST_TYPE_PSY,
            K_ASSIST_TYPE_KDJ,
            K_ASSIST_TYPE_WR,
            K_ASSIST_TYPE_RSI -> Unit
            else -> super.drawYRightValue(canvas)
        }
    }

    override fun drawData(canvas: Canvas) {
        mainChart?.let {
            when(assistType){
                K_ASSIST_TYPE_MACD -> drawMACD(canvas)
                K_ASSIST_TYPE_RSI -> drawRSI(canvas)
                K_ASSIST_TYPE_KDJ -> drawKDJ(canvas)
                K_ASSIST_TYPE_WR -> drawWR(canvas)
                K_ASSIST_TYPE_VOL -> drawVOL(canvas)
                K_ASSIST_TYPE_BIAS -> drawBIAS(canvas)
                K_ASSIST_TYPE_TRIX -> drawTRIX(canvas)
                K_ASSIST_TYPE_PSY -> drawPSY(canvas)
                K_ASSIST_TYPE_OBV -> drawOBV(canvas)
                K_ASSIST_TYPE_DMI -> drawDMI(canvas)
                K_ASSIST_TYPE_CCI -> drawCCI(canvas)
                K_ASSIST_TYPE_CR -> drawCR(canvas)
            }
        }
    }

    /**
     * 指标CR
     */
    private fun drawCR(canvas: Canvas) {
        val pathCr = Path()
        val pathMa5 = Path()
        val pathMa10 = Path()
        val pathMa20 = Path()

        listVisible.forEach {
            it.cr.apply {
                formatPath(pathCr, cr, it.chartX)
                formatPath(pathMa5, ma5, it.chartX)
                formatPath(pathMa10, ma10, it.chartX)
                formatPath(pathMa20, ma20, it.chartX)
            }
        }

        linePaint.style = Paint.Style.STROKE
        linePaint.color = getColor(mContext, R.color.k_indicator_color1)
        canvas.drawPath(pathCr, linePaint)
        linePaint.color = getColor(mContext, R.color.k_indicator_color2)
        canvas.drawPath(pathMa5, linePaint)
        linePaint.color = getColor(mContext, R.color.k_indicator_color3)
        canvas.drawPath(pathMa10, linePaint)
        linePaint.color = getColor(mContext, R.color.k_indicator_color4)
        canvas.drawPath(pathMa20, linePaint)

    }

    /**
     * 指标CCI
     */
    private fun drawCCI(canvas: Canvas) {
        val pathCci = Path()

        listVisible.forEach {
            it.cci.apply {
                formatPath(pathCci, cci, it.chartX)
            }
        }

        linePaint.style = Paint.Style.STROKE
        linePaint.color = getColor(mContext, R.color.k_indicator_color1)
        canvas.drawPath(pathCci, linePaint)

    }

    /**
     * 指标DMI
     */
    private fun drawDMI(canvas: Canvas) {
        val pathPdi = Path()
        val pathMdi = Path()
        val pathAdx = Path()
        val pathAdxr = Path()

        listVisible.forEach {
            it.dmi.apply {
                formatPath(pathPdi, pdi, it.chartX)
                formatPath(pathMdi, mdi, it.chartX)
                formatPath(pathAdx, adx, it.chartX)
                formatPath(pathAdxr, adxr, it.chartX)
            }
        }

        linePaint.style = Paint.Style.STROKE
        linePaint.color = getColor(mContext, R.color.k_indicator_color1)
        canvas.drawPath(pathPdi, linePaint)
        linePaint.color = getColor(mContext, R.color.k_indicator_color2)
        canvas.drawPath(pathMdi, linePaint)
        linePaint.color = getColor(mContext, R.color.k_indicator_color3)
        canvas.drawPath(pathAdx, linePaint)
        linePaint.color = getColor(mContext, R.color.k_indicator_color4)
        canvas.drawPath(pathAdxr, linePaint)
    }

    /**
     * 指标OBV
     */
    private fun drawOBV(canvas: Canvas) {
        val pathObv = Path()

        listVisible.forEach {
            it.obv.apply {
                formatPath(pathObv, obv, it.chartX)
            }
        }

        linePaint.style = Paint.Style.STROKE
        linePaint.color = getColor(mContext, R.color.k_indicator_color1)
        canvas.drawPath(pathObv, linePaint)

    }

    /**
     * 指标PSY
     */
    private fun drawPSY(canvas: Canvas) {
        val pathPsy = Path()
        val pathPsyMa = Path()

        listVisible.forEach {
            it.psy.apply {
                formatPath(pathPsy, psy, it.chartX)
                formatPath(pathPsyMa, psyma, it.chartX)
            }
        }

        linePaint.style = Paint.Style.STROKE
        linePaint.color = getColor(mContext, R.color.psy)
        canvas.drawPath(pathPsy, linePaint)
        linePaint.color = getColor(mContext, R.color.psyma)
        canvas.drawPath(pathPsyMa, linePaint)
    }

    /**
     * 指标TRIX
     */
    private fun drawTRIX(canvas: Canvas) {
        val pathTrix = Path()
        val pathMaTrix = Path()

        listVisible.forEach {
            it.trix.apply {
                formatPath(pathTrix, trix, it.chartX)
                formatPath(pathMaTrix, matrix, it.chartX)
            }
        }

        linePaint.style = Paint.Style.STROKE
        linePaint.color = getColor(mContext, R.color.trix)
        canvas.drawPath(pathTrix, linePaint)
        linePaint.color = getColor(mContext, R.color.matrix)
        canvas.drawPath(pathMaTrix, linePaint)
    }

    /**
     * 指标BIAS
     */
    private fun drawBIAS(canvas: Canvas) {
        val path1 = Path()
        val path2 = Path()
        val path3 = Path()

        listVisible.forEach {
            it.bias.apply {
                formatPath(path1, bias1, it.chartX)
                formatPath(path2, bias2, it.chartX)
                formatPath(path3, bias3, it.chartX)
            }
        }

        linePaint.style = Paint.Style.STROKE
        linePaint.color = getColor(mContext, R.color.bias1)
        canvas.drawPath(path1, linePaint)
        linePaint.color = getColor(mContext, R.color.bias2)
        canvas.drawPath(path2, linePaint)
        linePaint.color = getColor(mContext, R.color.bias3)
        canvas.drawPath(path3, linePaint)
    }

    /**
     * 指标VOL
     */
    private fun drawVOL(canvas: Canvas) {
        val candleHalfWidth: Float = 0.45f * mainChart!!.getDataStepSize() - candlePaint.strokeWidth/2f

        listVisible.forEach { kLine ->
            kLine.apply {
                when {
                    open > close -> {
                        candlePaint.style = Paint.Style.FILL_AND_STROKE
                        candlePaint.color = colorGreen
                    }
                    open < close -> {
                        candlePaint.style = Paint.Style.STROKE
                        candlePaint.color = colorRed
                    }
                    else -> {
                        candlePaint.style = Paint.Style.FILL_AND_STROKE
                        candlePaint.color = colorGray
                    }
                }
                canvas.drawRect(chartX - candleHalfWidth, chartHeight, chartX + candleHalfWidth, getValueY(volume), candlePaint)
            }
        }
    }

    /**
     * 指标RSI
     */
    private fun drawRSI(canvas: Canvas) {
        val pathRsi1 = Path()
        val pathRsi2 = Path()
        val pathRsi3 = Path()

        listVisible.forEach {
            it.rsi.apply {
                formatPath(pathRsi1, rsi1, it.chartX)
                formatPath(pathRsi2, rsi2, it.chartX)
                formatPath(pathRsi3, rsi3, it.chartX)
            }
        }

        linePaint.style = Paint.Style.STROKE
        linePaint.color = getColor(mContext, R.color.rsi1)
        canvas.drawPath(pathRsi1, linePaint)
        linePaint.color = getColor(mContext, R.color.rsi2)
        canvas.drawPath(pathRsi2, linePaint)
        linePaint.color = getColor(mContext, R.color.rsi3)
        canvas.drawPath(pathRsi3, linePaint)
    }

    /**
     * 指标KDJ
     */
    private fun drawKDJ(canvas: Canvas) {
        val pathK = Path()
        val pathD = Path()
        val pathJ = Path()

        listVisible.forEach {
            it.kdj.apply {
                formatPath(pathK, k, it.chartX)
                formatPath(pathD, d, it.chartX)
                formatPath(pathJ, j, it.chartX)
            }
        }

        linePaint.style = Paint.Style.STROKE
        linePaint.color = getColor(mContext, R.color.kdj_k)
        canvas.drawPath(pathK, linePaint)
        linePaint.color = getColor(mContext, R.color.kdj_d)
        canvas.drawPath(pathD, linePaint)
        linePaint.color = getColor(mContext, R.color.kdj_j)
        canvas.drawPath(pathJ, linePaint)
    }

    /**
     * 指标WR
     */
    private fun drawWR(canvas: Canvas) {
        val pathWR1 = Path()
        val pathWR2 = Path()

        listVisible.forEach {
            it.wr.apply {
                formatPath(pathWR1, wr1, it.chartX)
                formatPath(pathWR2, wr2, it.chartX)
            }
        }

        linePaint.style = Paint.Style.STROKE
        linePaint.color = getColor(mContext, R.color.wr_1)
        canvas.drawPath(pathWR1, linePaint)
        linePaint.color = getColor(mContext, R.color.wr_2)
        canvas.drawPath(pathWR2, linePaint)
    }

    /**
     * 指标MACD
     */
    private fun drawMACD(canvas: Canvas) {
        val pathDif = Path()
        val pathDea = Path()

        val candleHalfWidth: Float = 0.45f * mainChart!!.getDataStepSize()
        candlePaint.style = Paint.Style.FILL

        listVisible.forEach {
            it.macd.apply {
                if(macd > 0){
                    candlePaint.color = colorRed
                }else{
                    candlePaint.color = colorGreen
                }
                canvas.drawRect(it.chartX - candleHalfWidth, chartHeight/2, it.chartX + candleHalfWidth, getValueY(macd), candlePaint)

                formatPath(pathDif, dif, it.chartX)
                formatPath(pathDea, dea, it.chartX)
            }
        }

        linePaint.style = Paint.Style.STROKE
        linePaint.color = getColor(mContext, R.color.macd_dif)
        canvas.drawPath(pathDif, linePaint)
        linePaint.color = getColor(mContext, R.color.macd_dea)
        canvas.drawPath(pathDea, linePaint)
    }

    private fun formatPath(path: Path, value: Double, chartX: Float){
        if (!value.isNaN()) {
            if (path.isEmpty) {
                path.moveTo(chartX, getValueY(value))
            } else {
                path.lineTo(chartX, getValueY(value))
            }
        }
    }

    private val crossRadius = dp2px(1.5f)

    override fun drawCross(canvas: Canvas) {
        mainChart?.let {
            if(it.showCross && it.crossData != null){
                linePaint.color = colorCross
                linePaint.style = Paint.Style.STROKE

                it.crossData?.apply {
                    canvas.drawLine(chartX,0f, chartX, chartHeight, linePaint)
                    linePaint.style = Paint.Style.FILL
                    when(assistType){
                        K_ASSIST_TYPE_MACD -> {
                            drawCrossCircle(canvas, chartX, macd.dif, R.color.macd_dif)
                            drawCrossCircle(canvas, chartX, macd.dea, R.color.macd_dea)
                        }
                        K_ASSIST_TYPE_RSI -> {
                            drawCrossCircle(canvas, chartX, rsi.rsi1, R.color.rsi1)
                            drawCrossCircle(canvas, chartX, rsi.rsi2, R.color.rsi2)
                            drawCrossCircle(canvas, chartX, rsi.rsi3, R.color.rsi3)
                        }
                        K_ASSIST_TYPE_KDJ -> {
                            drawCrossCircle(canvas, chartX, kdj.k, R.color.kdj_k)
                            drawCrossCircle(canvas, chartX, kdj.d, R.color.kdj_d)
                            drawCrossCircle(canvas, chartX, kdj.j, R.color.kdj_j)
                        }
                        K_ASSIST_TYPE_BIAS -> {
                            drawCrossCircle(canvas, chartX, bias.bias1, R.color.bias1)
                            drawCrossCircle(canvas, chartX, bias.bias2, R.color.bias2)
                            drawCrossCircle(canvas, chartX, bias.bias3, R.color.bias3)
                        }
                        K_ASSIST_TYPE_WR -> {
                            drawCrossCircle(canvas, chartX, wr.wr1, R.color.wr_1)
                            drawCrossCircle(canvas, chartX, wr.wr2, R.color.wr_2)
                        }
                        K_ASSIST_TYPE_TRIX -> {
                            drawCrossCircle(canvas, chartX, trix.trix, R.color.trix)
                            drawCrossCircle(canvas, chartX, trix.matrix, R.color.matrix)
                        }
                        K_ASSIST_TYPE_CCI -> {
                            drawCrossCircle(canvas, chartX, cci.cci)
                        }
                        K_ASSIST_TYPE_CR -> {
                            drawCrossCircle(canvas, chartX, cr.cr, R.color.k_indicator_color1)
                            drawCrossCircle(canvas, chartX, cr.ma5, R.color.k_indicator_color2)
                            drawCrossCircle(canvas, chartX, cr.ma10, R.color.k_indicator_color3)
                            drawCrossCircle(canvas, chartX, cr.ma20, R.color.k_indicator_color4)
                        }
                        K_ASSIST_TYPE_DMI -> {
                            drawCrossCircle(canvas, chartX, dmi.pdi, R.color.k_indicator_color1)
                            drawCrossCircle(canvas, chartX, dmi.mdi, R.color.k_indicator_color2)
                            drawCrossCircle(canvas, chartX, dmi.adx, R.color.k_indicator_color3)
                            drawCrossCircle(canvas, chartX, dmi.adxr, R.color.k_indicator_color4)
                        }
                        K_ASSIST_TYPE_OBV -> {
                            drawCrossCircle(canvas, chartX, obv.obv)
                        }
                        K_ASSIST_TYPE_PSY -> {
                            drawCrossCircle(canvas, chartX, psy.psy, R.color.psy)
                            drawCrossCircle(canvas, chartX, psy.psyma, R.color.psyma)
                        }
                    }
                }
            }
        }
    }

    /**
     * 交线圆点
     */
    private fun drawCrossCircle(canvas: Canvas, chartX: Float, value: Double, colorRes: Int = R.color.k_indicator_color1){
        linePaint.color = getColor(mContext, colorRes)
        canvas.drawCircle(chartX, getValueY(value), crossRadius, linePaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        mainChart?.let {
            return it.onTouchEvent(event, this)
        }?:let {
            return super.onTouchEvent(event)
        }
    }

    override fun lastVisibleChanged() {
        
    }

    override fun formatMaxAndMin() {
        if(listVisible.isNullOrEmpty())
            return

        var minTemp = Double.MIN_VALUE
        var maxTemp = Double.MAX_VALUE

        when(assistType){
            K_ASSIST_TYPE_MACD -> {
                val maxEntity: KLineData? = listVisible.maxByOrNull { abs(it.macd.maxValue) }
                val minEntity: KLineData? = listVisible.maxByOrNull { abs(it.macd.minValue) }

                if(minEntity != null && maxEntity != null){
                    maxTemp = max(abs(maxEntity.macd.maxValue),abs(minEntity.macd.minValue))
                    minTemp = -maxTemp
                }
            }
            K_ASSIST_TYPE_RSI -> {
                val maxEntity: KLineData? = listVisible.maxByOrNull { it.rsi.maxValue }
                val minEntity: KLineData? = listVisible.minByOrNull { it.rsi.minValue }

                if(minEntity != null && maxEntity != null){
                    maxTemp = maxEntity.rsi.maxValue
                    minTemp = minEntity.rsi.minValue
                }
            }
            K_ASSIST_TYPE_KDJ -> {
                val maxEntity: KLineData? = listVisible.maxByOrNull { it.kdj.maxValue }
                val minEntity: KLineData? = listVisible.minByOrNull { it.kdj.minValue }

                if(minEntity != null && maxEntity != null){
                    maxTemp = maxEntity.kdj.maxValue
                    minTemp = minEntity.kdj.minValue
                }
            }
            K_ASSIST_TYPE_BIAS -> {
                val maxEntity: KLineData? = listVisible.maxByOrNull { it.bias.maxValue }
                val minEntity: KLineData? = listVisible.minByOrNull { it.bias.minValue }

                if(minEntity != null && maxEntity != null){
                    maxTemp = maxEntity.bias.maxValue
                    minTemp = minEntity.bias.minValue
                }
            }
            K_ASSIST_TYPE_WR -> {
                val maxEntity: KLineData? = listVisible.maxByOrNull { it.wr.maxValue }
                val minEntity: KLineData? = listVisible.minByOrNull { it.wr.minValue }

                if(minEntity != null && maxEntity != null){
                    maxTemp = maxEntity.wr.maxValue
                    minTemp = minEntity.wr.minValue
                }
            }
            K_ASSIST_TYPE_TRIX -> {
                val maxEntity: KLineData? = listVisible.maxByOrNull { it.trix.maxValue }
                val minEntity: KLineData? = listVisible.minByOrNull { it.trix.minValue }

                if(minEntity != null && maxEntity != null){
                    maxTemp = maxEntity.trix.maxValue
                    minTemp = minEntity.trix.minValue
                }
            }
            K_ASSIST_TYPE_PSY -> {
                val maxEntity: KLineData? = listVisible.maxByOrNull { it.psy.maxValue }
                val minEntity: KLineData? = listVisible.minByOrNull { it.psy.minValue }

                if(minEntity != null && maxEntity != null){
                    maxTemp = maxEntity.psy.maxValue
                    minTemp = minEntity.psy.minValue
                }
            }
            K_ASSIST_TYPE_VOL -> {
                val maxEntity: KLineData? = listVisible.maxByOrNull { it.volume }

                if(maxEntity != null){
                    maxTemp = maxEntity.volume
                    minTemp = 0.0
                }
            }
            K_ASSIST_TYPE_OBV -> {
                val maxEntity: KLineData? = listVisible.maxByOrNull { it.obv.maxValue }
                val minEntity: KLineData? = listVisible.minByOrNull { it.obv.minValue }

                if(minEntity != null && maxEntity != null){
                    maxTemp = maxEntity.obv.maxValue
                    minTemp = minEntity.obv.minValue
                }
            }
            K_ASSIST_TYPE_DMI -> {
                val maxEntity: KLineData? = listVisible.maxByOrNull { it.dmi.maxValue }
                val minEntity: KLineData? = listVisible.minByOrNull { it.dmi.minValue }

                if(minEntity != null && maxEntity != null){
                    maxTemp = maxEntity.dmi.maxValue
                    minTemp = minEntity.dmi.minValue
                }
            }
            K_ASSIST_TYPE_CCI -> {
                val maxEntity: KLineData? = listVisible.maxByOrNull { it.cci.maxValue }
                val minEntity: KLineData? = listVisible.minByOrNull { it.cci.minValue }

                if(minEntity != null && maxEntity != null){
                    maxTemp = maxEntity.cci.maxValue
                    minTemp = minEntity.cci.minValue
                }
            }
            K_ASSIST_TYPE_CR -> {
                val maxEntity: KLineData? = listVisible.maxByOrNull { it.cr.maxValue }
                val minEntity: KLineData? = listVisible.minByOrNull { it.cr.minValue }

                if(minEntity != null && maxEntity != null){
                    maxTemp = maxEntity.cr.maxValue
                    minTemp = minEntity.cr.minValue
                }
            }
        }

        if(minTemp != Double.MIN_VALUE && maxTemp != Double.MAX_VALUE ){
            val offsetGap = if(maxTemp == minTemp){
                if(maxTemp == 0.0){
                    1.0
                }else{
                    maxTemp*0.1
                }
            }else{
                (maxTemp - minTemp)*0.1
            }

            when(assistType){
                K_ASSIST_TYPE_VOL -> {
                    topValue = maxTemp + offsetGap
                    bottomValue = minTemp
                }
                else -> {
                    topValue = maxTemp + offsetGap
                    bottomValue = minTemp - offsetGap
                }
            }
            formatRightValue(2)
        }
    }

    fun bindMainChart(kChart: KChart? = null){
        mainChart = kChart
        mainChart?.let {
            this.listVisible = it.listVisible
        }?:let {
            this.listVisible.clear()
        }
    }

}