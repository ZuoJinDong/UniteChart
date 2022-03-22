package com.zjd.unite.chart.chart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import com.zjd.unite.chart.R
import com.zjd.unite.chart.entity.TsLineData
import com.zjd.unite.chart.utils.dp2px
import com.zjd.unite.chart.utils.getColor
import kotlin.math.abs
import kotlin.math.max

/**
 * @author ZJD
 * @date 2021/5/19
 * @desc 分时副图
 **/

const val TS_ASSIST_TYPE_VOL = "TS_ASSIST_TYPE_VOL"
const val TS_ASSIST_TYPE_MACDFS = "TS_ASSIST_TYPE_MACDFS"

class TsAssistChart @JvmOverloads constructor(mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : BaseChart<TsLineData>(mContext, attrs, defStyleAttr) {

    /** 指标 */
    val typeList = listOf(
            TS_ASSIST_TYPE_VOL,
            TS_ASSIST_TYPE_MACDFS)

    /** 指标 */
    var assistType = TS_ASSIST_TYPE_VOL

    private var mainChart: TsChart? = null

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
            showCross = it.showCross
            invalidate()
        }
    }

    /**
     * 切换指标
     */
    @Suppress("UNCHECKED_CAST")
    fun switchAssist(){
        //内部处理
//        var index = typeList.indexOf(assistType)
//        if(index >= typeList.size - 1)
//            index = -1
//        assistType = typeList[index+1]
//        formatMaxAndMin()


        //外部处理
        mSingleClick?.onChartSingleClick(this as BaseChart<Any>)
    }

    @JvmName("setAssistType1")
    fun setAssistType(assistType: String){
        this.assistType = assistType
        formatMaxAndMin()
    }

    override fun drawDashLineY(canvas: Canvas) {
        linePaint.pathEffect = dashPathEffect
        when(assistType){
            TS_ASSIST_TYPE_MACDFS,
            TS_ASSIST_TYPE_VOL -> {
                canvas.drawLine(0f, chartHeight/2, chartWidth, chartHeight/2, linePaint)
            }
        }
        linePaint.pathEffect = null
    }

    override fun drawYLeftValue(canvas: Canvas) {

    }

    override fun drawData(canvas: Canvas) {
        mainChart?.let {
            when(assistType){
                TS_ASSIST_TYPE_MACDFS -> drawMACD(canvas)
                TS_ASSIST_TYPE_VOL -> drawVOL(canvas)
            }
        }
    }

    /**
     * 指标VOL
     */
    private fun drawVOL(canvas: Canvas) {
        candlePaint.style = Paint.Style.FILL
        listVisible.forEachIndexed { index, tsLine ->
            tsLine.apply {
                val preClose = if(index == 0){
                    mainChart?.midValue?:tsLine.close
                }else{
                    listVisible[index - 1].close
                }

                candlePaint.color = when {
                    close > preClose -> colorRed
                    close < preClose -> colorGreen
                    else -> colorOrange
                }
                canvas.drawLine(chartX,chartHeight,chartX,getValueY(volume),candlePaint)
            }
        }
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

    override fun drawCross(canvas: Canvas) {
        if(!showCross)
            return

        val crossRadius = dp2px(1.5f)
        mainChart?.let {
            if(it.showCross && it.crossData != null){
                linePaint.color = colorCross
                linePaint.style = Paint.Style.STROKE

                it.crossData?.apply {
                    canvas.drawLine(it.crossX,0f, it.crossX, chartHeight, linePaint)
                    linePaint.style = Paint.Style.FILL
                    when(assistType){
                        TS_ASSIST_TYPE_MACDFS -> {
                            linePaint.color = getColor(mContext, R.color.macd_dif)
                            canvas.drawCircle(it.crossX, getValueY(macd.dif), crossRadius, linePaint)
                            linePaint.color = getColor(mContext, R.color.macd_dea)
                            canvas.drawCircle(it.crossX, getValueY(macd.dea), crossRadius, linePaint)
                        }
                    }
                }
            }
        }
    }

    override fun drawXValue(canvas: Canvas) {
        super.drawXValue(canvas)
        drawCross(canvas)
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
            TS_ASSIST_TYPE_MACDFS -> {
                val maxEntity: TsLineData? = listVisible.maxByOrNull { abs(it.macd.maxValue) }
                val minEntity: TsLineData? = listVisible.maxByOrNull { abs(it.macd.minValue) }

                if(minEntity != null && maxEntity != null){
                    maxTemp = max(abs(maxEntity.macd.maxValue),abs(minEntity.macd.minValue))
                    minTemp = -maxTemp
                }
            }
            TS_ASSIST_TYPE_VOL -> {
                val maxEntity: TsLineData? = listVisible.maxByOrNull { it.volume }

                if(maxEntity != null){
                    maxTemp = maxEntity.volume
                    minTemp = 0.0
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
                TS_ASSIST_TYPE_VOL -> {
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

    fun bindMainChart(tsChart: TsChart? = null){
        mainChart = tsChart
        mainChart?.let {
            this.listVisible = it.listVisible
        }?:let {
            this.listVisible.clear()
        }
    }

}