package com.zjd.unite.chart.chart

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.blankj.utilcode.util.NumberUtils
import com.blankj.utilcode.util.TimeUtils
import com.zjd.unite.chart.entity.QuoteBean
import com.zjd.unite.chart.entity.TsHisBean
import com.zjd.unite.chart.entity.TsLineData
import com.zjd.unite.chart.utils.dp2px
import com.zjd.unite.chart.utils.roundStr
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * @author ZJD
 * @date 2021/4/30
 * @desc 分时多日叠加
 **/
class TsDuoMultiChart @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : GoldenCutChart<TsDuoMultiChart.UnitData>(context, attrs, defStyleAttr) {

    /** 时间点 */
    private val timeAll = mutableListOf<Long>()

    /** 中线数值 */
    var midValue = 0.0

    val pathColors = listOf(colorGreen,colorRed,colorBlue,colorOrange,colorPurple)

    /** 选中数据 */
    var onDuoDataSelectListener: OnDuoDataSelectListener? = null
    /** 十字线所在数据 */
    private val crossDataList = mutableListOf<String>()
    /** 日期 */
    private val dates = mutableListOf<String>()
    /** 数据源 */
    private val sourceList = mutableListOf<TsHisBean>()
    /** 前一条数据（用于补点） */
    private lateinit var preData: Array<TsLineData?>
    /** 是否已经计算横坐标（view为GONE时 不计算） */
    private var inited = false

    fun setTsLineList(list: List<TsHisBean>?){
        post {
            if(!list.isNullOrEmpty()){
                preData = arrayOfNulls(list.size)
                sourceList.clear()
                sourceList.addAll(list)

                if(visibility == View.GONE || chartWidth == 0f){
                    return@post
                }

                inited = true

                listVisible.clear()
                listFull.clear()
                dates.clear()
                val lastDayLong: Long
                list.last().let { tsHis ->
                    midValue = tsHis.yersterdayClose
                    lastDayLong = tsHis.billDay
                    tsHis.tradeTime.forEach { time ->
                        timeAll.addAll(time.getAllTime())
                    }
                }

                visibleCount = timeAll.size
                visibleCountMin = timeAll.size
                visibleCountMax = timeAll.size

                timeAll.forEachIndexed { index, time ->
                    listFull.add(UnitData(time, arrayOfNulls(list.size), getValueX(index)))
                }

                list.forEachIndexed { index, tsHisBean ->
                    preData[index] = TsLineData().apply {
                        close = tsHisBean.yersterdayClose
                    }

                    dates.add(TimeUtils.millis2String(tsHisBean.billDay, "yyyy-MM-dd"))
                    val offsetTime = lastDayLong - tsHisBean.billDay
                    tsHisBean.pointList.forEachIndexed { _, tsLineData ->
                        //
                        var gapDay = 0
                        tsHisBean.tradeTime.forEachIndexed { index, tradeTimeBean ->
                            if(tradeTimeBean.start <= tsLineData.time
                                && tradeTimeBean.end >= tsLineData.time
                                && index < tsHisBean.tradeTime.size - 1){
                                gapDay = ((tsHisBean.tradeTime[index + 1].start - tradeTimeBean.end)/oneDayLong).toInt()
                            }
                        }

                        val time = tsLineData.time + offsetTime + gapDay*oneDayLong
                        val timeIndex = timeAll.indexOf(time)
                        if(timeIndex >= 0){
                            listFull[timeIndex].let {
                                it.max = max(tsLineData.close, it.max)
                                it.min = min(tsLineData.close, it.min)
                                it.dataList[index] = tsLineData
                            }
                        }
                    }
                }

                val currentTime = System.currentTimeMillis()

                listFull.forEachIndexed { index, unitData ->
                    unitData.dataList.forEachIndexed { dataIndex, tsLineData ->
                        tsLineData?.let {
                            preData[dataIndex] = it
                        }?:let {
                            preData[dataIndex]?.let {
                                if(unitData.time < currentTime || (dataIndex < unitData.dataList.size - 1)){
                                    unitData.dataList[dataIndex] = it
                                }
                            }
                        }
                    }
                }

                listVisible.addAll(listFull)

                formatMaxAndMin()
                postInvalidate()
            }
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if(visibility == View.VISIBLE && !inited){
            setTsLineList(sourceList.toList())
        }
    }

    override fun lastVisibleChanged() {

    }

    override fun drawData(canvas: Canvas) {
        val pathList = mutableListOf(Path(),Path(),Path(),Path(),Path())

        listVisible.forEachIndexed { dataIndex, data ->
            data.dataList.forEachIndexed { lineIndex, tsLineData ->
                tsLineData?.let { tsLineData ->
                    pathList[lineIndex].let { path ->
                        if(path.isEmpty){
                            path.moveTo(data.chartX, getValueY(tsLineData.close))
                        }else{
                            path.lineTo(data.chartX, getValueY(tsLineData.close))
                        }
                    }
                }
            }
        }

        pathList.forEachIndexed { index, path ->
            if(!path.isEmpty){
                linePaint.color = pathColors[pathColors.size - 1 - index]
                canvas.drawPath(path, linePaint)
            }
        }

        selectData()
    }

    private fun selectData(data: UnitData? = null) {
        crossDataList.clear()
        data?.dataList?.forEachIndexed { index, tsLineData ->
            tsLineData?.close?.let {
                crossDataList.add(dates[index] + ": " + NumberUtils.format(it, mDec))
            }?:let {
                crossDataList.add(dates[index] + ": 0.00")
            }
        }?:let {
            sourceList.forEachIndexed { index, tsHisBean ->
                tsHisBean.pointList.lastOrNull()?.close?.let {
                    crossDataList.add(dates[index] + ": " + NumberUtils.format(it, mDec))
                }?:let{
                    crossDataList.add(dates[index] + ": 0.00")
                }
            }
        }
        onDuoDataSelectListener?.onDuoDataSelect(crossDataList)
    }

    override fun drawYLeftValue(canvas: Canvas) {
        textPaint.textSize = textSizeDef

        val midIndex = (yLeftValue.size - 1)/2

        yLeftValue.forEachIndexed { index, chartXYValue ->
            chartXYValue.apply {
                textPaint.color = when {
                    index < midIndex -> colorRed
                    index > midIndex -> colorGreen
                    else -> textColorDark
                }

                if(index == yLeftValue.size - 1){
                    canvas.drawText(value, dp2px(3f), y - dp2px(2f), textPaint)
                }else{
                    canvas.drawText(value, dp2px(3f), y + dp2px(2f) + bounds.height(), textPaint)
                }
            }
        }
    }

    override fun drawCross(canvas: Canvas) {
        linePaint.color = colorCross
        textPaint.color = textColorDark
        textPaint.textSize = textSizeDef

        val rect = Rect()

        if(crossX > chartWidth){
            crossX = chartWidth
        }

        if(crossY < 0){
            crossY = 0f
        }else if(crossY > chartHeight){
            crossY = chartHeight
        }

        listVisible.minByOrNull { abs(it.chartX - crossX) }?.let { crossData ->

            selectData(crossData)

            canvas.drawLine(crossX,0f,crossX,chartHeight,linePaint)
            canvas.drawLine(0f,crossY,chartWidth,crossY,linePaint)

            //左侧数值
            roundStr(getValueByY(crossY), mDec).let { value ->
                textPaint.getTextBounds(value, 0, value.length, rect)

                var rectYBottom = crossY + dp2px(2f) + rect.height()/2

                if(rectYBottom - dp2px(4f) - rect.height() < 0){
                    rectYBottom = dp2px(4f) + rect.height()
                }else if(rectYBottom > chartHeight){
                    rectYBottom = chartHeight
                }

                //左侧
                val rectLeft = RectF(0f,rectYBottom - dp2px(4f) - rect.height(), rect.width() + dp2px(6f),rectYBottom)
                drawDataRect(canvas, rectLeft)
                canvas.drawText(value, dp2px(3f), rectYBottom - dp2px(2f), textPaint)
            }

            //底部数值
            crossData.time.let { time ->
                TimeUtils.millis2String(time,"HH:mm").let { valueX ->
                    textPaint.getTextBounds(valueX,0, valueX.length, rect)
                    var bottomRectX = crossX - rect.width()/2 - dp2px(3f)
                    if(bottomRectX < 0f){
                        bottomRectX = 0f
                    }else if(bottomRectX > chartWidth - rect.width() - dp2px(6f)){
                        bottomRectX = chartWidth - rect.width() - dp2px(6f)
                    }
                    val rectBottom = RectF(bottomRectX,chartHeight - rect.height() - dp2px(4f),bottomRectX + rect.width() + dp2px(6f), chartHeight)
                    drawDataRect(canvas, rectBottom)
                    canvas.drawText(valueX, bottomRectX + dp2px(3f) , chartHeight - dp2px(2f), textPaint)
                }
            }
        }
    }

    override fun formatMaxAndMin() {
        val maxEntity: UnitData? = listVisible.maxByOrNull { it.max }
        val minEntity: UnitData? = listVisible.minByOrNull { it.min }

        if(minEntity != null && maxEntity != null){
            var offsetGap = max(abs(maxEntity.max - midValue), abs(minEntity.min - midValue))

            if(offsetGap == 0.0){
                offsetGap = midValue*0.1
            }

            offsetGap = if(offsetGap == 0.0){
                midValue*0.1
            }else{
                offsetGap * 1.1
            }
            maxValue = maxEntity.max
            minValue = minEntity.min
            topValue = midValue + offsetGap
            bottomValue = midValue - offsetGap
            formatLeftValue()
            formatXValue()
        }
    }

    override fun formatRightValue(gapSize: Int) {

    }

    override fun formatXValue() {
        if(listVisible.isNotEmpty()){
            xValue.clear()
            val bound = Rect()

            listVisible.first().let { first ->
                TimeUtils.millis2String(first.time,"HH:mm").let { time ->
                    textPaint.getTextBounds(time, 0, time.length, bound)

                    var x = first.chartX

                    if(xValue.size > 0){
                        x -= bound.width() / 2f
                    }

                    xValue.add(ChartXYValue(time, x,chartHeight + bound.height() + dp2px(2f),bound))
                }
            }

            listVisible.last().time.let {
                TimeUtils.millis2String(it,"HH:mm").let { time ->
                    textPaint.getTextBounds(time, 0, time.length, bound)
                    xValue.add(ChartXYValue(time, chartWidth - bound.width(),chartHeight + bound.height() + dp2px(2f),bound))
                }
            }
        }
    }

    override fun openGoldenCut(open: Boolean) {
        if(open){
            val maxEntity: UnitData? = listVisible.maxByOrNull { it.max }
            val minEntity: UnitData? = listVisible.minByOrNull { it.min }

            if(maxEntity != null && minEntity != null){
                val indexMax = listVisible.indexOf(maxEntity)
                val indexMin = listVisible.indexOf(minEntity)
                formatGoldenParams(maxEntity.max, minEntity.min, indexMin < indexMax)
            }
        }else{
            clearGoldenParams()
        }
    }

    fun updateQuote(quoteBean: QuoteBean?) {
        quoteBean?.let {
            if(!showCross && !isShowGoldenCut() && timeAll.contains(quoteBean.recentTime)){

                val dataIndex = timeAll.indexOf(quoteBean.recentTime)
                listVisible[dataIndex].dataList.let { dataList ->
                    dataList.lastOrNull()?.let {
                        //更新最后一条
                        it.close = quoteBean.currentPrice
                    }?:let {
                        //新增最后一条
                        dataList[dataList.size - 1] = TsLineData().apply {
                            time = quoteBean.recentTime
                            close = quoteBean.currentPrice
                        }
                    }
                }

                formatMaxAndMin()
                postInvalidate()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onSingleTapUp(ev: MotionEvent): Boolean {
        if(!showCross){
            mSingleClick?.onChartSingleClick(this as BaseChart<Any>)
        }
        return super.onSingleTapUp(ev)
    }

    /** 合并数据 */
    data class UnitData(val time: Long, //坐标轴时间
                        var dataList: Array<TsLineData?>, //每天相同时间点数据
                        var chartX: Float = 0f, //横坐标
                        var max: Double = Double.MIN_VALUE, //每日最大值
                        var min: Double = Double.MAX_VALUE, //每日最小值
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as UnitData

            if (time != other.time) return false
            if (!dataList.contentEquals(other.dataList)) return false
            if (chartX != other.chartX) return false
            if (max != other.max) return false
            if (min != other.min) return false

            return true
        }

        override fun hashCode(): Int {
            var result = time.hashCode()
            result = 31 * result + dataList.contentHashCode()
            result = 31 * result + chartX.hashCode()
            result = 31 * result + max.hashCode()
            result = 31 * result + min.hashCode()
            return result
        }
    }

    interface OnDuoDataSelectListener{
        fun onDuoDataSelect(params: List<String>)
    }
}