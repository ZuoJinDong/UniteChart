package com.zjd.unite.chart.chart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.SparseArray
import android.view.MotionEvent
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.TimeUtils
import com.zjd.unite.chart.entity.CalendarBean
import com.zjd.unite.chart.entity.QuoteBean
import com.zjd.unite.chart.entity.TsHisBean
import com.zjd.unite.chart.entity.TsLineData
import com.zjd.unite.chart.utils.DataFormatHelper
import com.zjd.unite.chart.utils.dp2px
import com.zjd.unite.chart.utils.formatDouble
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * @author ZJD
 * @date 2021/4/30
 * @desc 分时主图
 **/

const val MAIN_TYPE_TS = "MAIN_TYPE_TS"

open class TsChart @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : GoldenCutChart<TsLineData>(context, attrs, defStyleAttr) {

    /** 所有时间点 */
    protected val timeAll = linkedSetOf<Long>()
    protected val timeAllMap = linkedMapOf<Long, Float>()
    /** 数据 */
    protected var tsHisList: MutableList<TsHisBean> = mutableListOf()
    /** 中线数值 */
    var midValue = 0.0
    /** 最新点指示半径 */
    protected val newPointRadius = dp2px(3f)
    /** Y轴全数据最小高度 */
    protected val fullYMinHeight = SizeUtils.dp2px(125f)
    /** 是否需要替换昨结 */
    var needSettlePrice = true

    /** 半透明阴影 */
    protected val paintShadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorShadow
        alpha = 50
        style = Paint.Style.FILL
    }

    /** 财经地雷 */
    private val financeList = mutableListOf<FinanceInfo>()

    fun setQuoteBean(dec: Int){
        mDec = dec
    }

    fun setCalendarFinance(list: MutableList<CalendarBean>){
        financeList.clear()
        val timeSet = mutableSetOf<Long>()

        list.forEach {
            timeSet.add(it.time)
        }

        timeSet.forEach { time ->
            val sameTimeList = list.filter { it.time == time }
            list.removeAll(sameTimeList)
            financeList.add(FinanceInfo(time, sameTimeList))
        }
    }

    /** 副图 */
    private val assistChartList = mutableListOf<TsAssistChart>()

    /**
     * 绑定副图
     */
    fun bindAssist(assistChart: TsAssistChart){
        assistChart.bindMainChart(this)
        assistChartList.add(assistChart)
    }

    /**
     * 是否加载过
     */
    fun isLoaded(): Boolean{
        return assistChartList.isNotEmpty() || listVisible.isNotEmpty()
    }

    fun setTsHisBean(tsHisBean: TsHisBean?){
        tsHisList.clear()
        tsHisBean?.let {
            tsHisList.add(it)
        }

        tsHisBean?.let { tsHis ->

            listVisible.clear()
            timeAll.clear()
            timeAllMap.clear()

            tsHis.tradeTime.forEach { time ->
                timeAll.addAll(time.getAllTime())
            }

//            quoteBean?.let { quote ->
//                if(tsHis.pointList.isEmpty() && timeAll.isNotEmpty() && quote.recentTime !in timeAll.first()..timeAll.last()){
//                    timeAll.minByOrNull { abs(quote.recentTime - it) }?.let {
//                        quote.recentTime = it
//                        tsHis.pointList.add(TsLineData(quote))
//                    }
//                }
//            }

            midValue = tsHis.yersterdayClose

            listVisible.addAll(tsHis.pointList)

            visibleCountMax = timeAll.size
            visibleCountMin = timeAll.size
            visibleCount = timeAll.size
            visibleLastIndex = timeAll.size

            formatMaxAndMin()
        }
    }

    override fun openGoldenCut(open: Boolean) {
        if(open){
            val maxEntity: TsLineData? = listVisible.maxByOrNull { it.close }
            val minEntity: TsLineData? = listVisible.minByOrNull { it.close }

            if(maxEntity != null && minEntity != null){
                val indexMax = listVisible.indexOf(maxEntity)
                val indexMin = listVisible.indexOf(minEntity)
                formatGoldenParams(maxEntity.close, minEntity.close, indexMin < indexMax)
            }
        }else{
            clearGoldenParams()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                removeKCallbacks()
            }
            MotionEvent.ACTION_UP -> {
                if(showCross){
                    postDelayed(cross, 3000)
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private var cross = Runnable {
        hideCross()
    }

    private fun removeKCallbacks() {
        removeCallbacks(cross)
    }

    override fun lastVisibleChanged() {
    }

    override fun drawData(canvas: Canvas) {
        /** 主线 */
        drawLine(canvas)

        /** 刷新副图 */
        assistChartList.forEach {
            it.refreshView()
        }
    }

    /**
     * 主线
     */
    open fun drawLine(canvas: Canvas) {
        drawSingleLine(canvas)
    }

    /**
     * 连续图
     */
    protected fun drawSingleLine(canvas: Canvas) {
        /** 最后一点坐标 */
        var lastX = 0f
        var lastY = 0f
        var lastAvgY = 0f

        val pathAvg = Path()
        val path = Path()
        val pathShadow = Path()
        linePaint.color = colorBlue

        var lastTime = 0L
        listVisible.forEachIndexed { index, data ->
            data.apply {
                getXByTime(time)?.let { x ->
                    chartX = x
                    val chartY = getValueY(close)
                    val avgY = getValueY(avgPrice)
                    if(path.isEmpty){
                        pathAvg.moveTo(0f, avgY)
                        path.moveTo(0f, chartY)
                        pathShadow.moveTo(0f, chartHeight)
                        pathShadow.lineTo(0f, chartY)

                        pathAvg.lineTo(chartX, avgY)
                        path.lineTo(chartX, chartY)
                        pathShadow.lineTo(chartX, chartY)
                    }else{
                        if(time - lastTime > oneMinuteLong){
                            getXByTime(time - oneMinuteLong)?.let { preX ->
                                pathAvg.lineTo(preX, lastAvgY)
                                path.lineTo(preX, lastY)
                                pathShadow.lineTo(preX, lastY)
                            }?:let {
                                pathAvg.lineTo(chartX, lastAvgY)
                                path.lineTo(chartX, lastY)
                                pathShadow.lineTo(chartX, lastY)
                            }
                        }
                        pathAvg.lineTo(chartX, avgY)
                        path.lineTo(chartX, chartY)
                        pathShadow.lineTo(chartX, chartY)
                    }
                    lastAvgY = avgY
                    lastX = chartX
                    lastY = chartY
                    lastTime = time
                }
            }
        }

        if(!path.isEmpty){
            linePaint.color = colorBlue
            canvas.drawPath(path, linePaint)
            linePaint.color = colorOrange
            canvas.drawPath(pathAvg, linePaint)
            pathShadow.lineTo(lastX, chartHeight)
            pathShadow.close()
            canvas.drawPath(pathShadow, paintShadow)
            linePaint.style = Paint.Style.FILL
            linePaint.color = colorBlue
            linePaint.alpha = 100
            canvas.drawCircle(lastX, lastY, newPointRadius, linePaint)
            linePaint.alpha = 255
            canvas.drawCircle(lastX, lastY, newPointRadius/2, linePaint)
        }
    }

    /**
     * 左侧数值
     */
    override fun drawYLeftValue(canvas: Canvas) {
        textPaint.textSize = textSizeDef

        val midIndex = (yLeftValue.size - 1)/2

        yLeftValue.forEachIndexed { index, chartXYValue ->
            if(chartHeight < fullYMinHeight && index%2 == 1){
                return@forEachIndexed
            }
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

    /**
     * 右侧数值
     */
    override fun drawYRightValue(canvas: Canvas) {
        textPaint.textSize = textSizeDef

        val midIndex = (yRightValue.size - 1)/2

        yRightValue.forEachIndexed { index, chartXYValue ->
            if(chartHeight < fullYMinHeight && index%2 == 1){
                return@forEachIndexed
            }

            chartXYValue.apply {
                textPaint.color = when {
                    index < midIndex -> colorRed
                    index > midIndex -> colorGreen
                    else -> textColorDark
                }

                if(index == yRightValue.size - 1){
                    canvas.drawText(value, 0, value.length, chartWidth - bounds.width() - dp2px(3f), y - dp2px(2f), textPaint)
                }else{
                    canvas.drawText(value, 0, value.length, chartWidth - bounds.width() - dp2px(3f), y + dp2px(2f) + bounds.height(), textPaint)
                }
            }
        }

        if(showCross){
            drawCross(canvas)
        }
    }

    override fun drawXValue(canvas: Canvas) {
        super.drawXValue(canvas)
        drawCalendarFinanceCircle(canvas)
    }

    /** 财经地雷圆点半径 */
    private val financeRadius = dp2px(3f)

    /**
     * 财经地雷圆点
     */
    private fun drawCalendarFinanceCircle(canvas: Canvas) {
        linePaint.style = Paint.Style.FILL
        linePaint.color = colorRed
        financeList.forEach { info ->
            getXByTime(info.time)?.let { x ->
                info.x = x
                info.circleRect.set(x - 4 * financeRadius, chartHeight - 4 * financeRadius, x + 4 * financeRadius, chartHeight + 4 * financeRadius)
                canvas.drawCircle(x, chartHeight, financeRadius, linePaint)
            }
        }
    }


    /**
     * 边框
     */
    override fun drawOutline(canvas: Canvas) {
        super.drawOutline(canvas)

        tsHisList.firstOrNull()?.let { hisBean ->
            hisBean.tradeTime.forEachIndexed { index, tradeTimeBean ->
                if(index > 0){
                    getXByTime(tradeTimeBean.start)?.let { x ->
                        canvas.drawLine(x,0f, x, chartHeight, linePaint)
                    }
                }
            }
        }
    }

    /**
     * 十字线
     */
    override fun drawCross(canvas: Canvas) {
        linePaint.color = colorCross
        textPaint.color = textColorDark
        textPaint.textSize = textSizeDef

        crossData = listVisible.filter { crossX >= it.chartX }.minByOrNull { crossX - it.chartX }

        if(crossData == null && listVisible.isNotEmpty()){
            crossData = listVisible[0]
        }

        crossData?.let {
            onDataSelectListener?.onDataSelect(it, showCross, crossX + marginLeft)
            val crossTime = timeAllMap.minByOrNull { abs(it.value - crossX) }
            crossTime?.let { crossTime ->
                getXByTime(it.time)?.let { x ->
                    crossX = x
                    val time =
//                        if(abs(crossTime.value - crossX) > step*2){
//                            if(crossTime.value > lastX){
//                                crossX = lastX
//                                it.time
//                            }else{
//                                crossX = crossTime.value
//                                crossTime.key
//                            }
//                        }else{
                        it.time
//                        }

                    val crossY = getValueY(it.close)
                    canvas.drawLine(crossX,0f,crossX,chartHeight,linePaint)
                    canvas.drawLine(0f,crossY,chartWidth,crossY,linePaint)
                    it.close

                    formatDouble(it.close, mDec).let { value ->
                        val rect = Rect()

                        //底部数值
                        TimeUtils.millis2String(time,"HH:mm").let { valueX ->
                            textPaint.getTextBounds(valueX,0, valueX.length, rect)
                            var bottomRectX = crossX - rect.width()/2 - dp2px(3f)
                            if(bottomRectX < 0f){
                                bottomRectX = 0f
                            }else if(bottomRectX > chartWidth - rect.width() - dp2px(6f)){
                                bottomRectX = chartWidth - rect.width() - dp2px(6f)
                            }
                            val rectBottom = RectF(bottomRectX,
                                chartHeight - rect.height() - dp2px(4f),
                                bottomRectX + rect.width() + dp2px(6f),
                                chartHeight)
                            drawDataRect(canvas, rectBottom)
                            canvas.drawText(valueX, bottomRectX + dp2px(3f) , chartHeight - dp2px(2f), textPaint)
                        }

                        //左侧
                        textPaint.getTextBounds(value,0,value.length,rect)
                        val rectLeft = RectF(0f,
                            crossY - dp2px(2f) - rect.height()/2,
                            rect.width() + dp2px(6f),
                            crossY + dp2px(2f) + rect.height()/2)
                        drawDataRect(canvas, rectLeft)
                        canvas.drawText(value, dp2px(3f), crossY + rect.height()/2, textPaint)

                        //右侧
                        "${formatDouble(100*(it.close - midValue)/midValue)}%".let { valueRight ->
                            textPaint.getTextBounds(valueRight,0,valueRight.length,rect)
                            val rectRight = RectF(chartWidth - rect.width() - dp2px(6f),
                                crossY - dp2px(2f) - rect.height()/2,
                                chartWidth,
                                crossY + dp2px(2f) + rect.height()/2)
                            drawDataRect(canvas, rectRight)
                            canvas.drawText(valueRight, chartWidth - rect.width() - dp2px(3f), crossY + rect.height()/2, textPaint)
                        }


                    }

                    /** 刷新副图 */
                    assistChartList.forEach { chart ->
                        chart.refreshView()
                    }
                }
            }
        }
    }

    /**
     * 分时图无滑动功能
     */
    override fun changeLastVisible(scaleMode: Boolean) {

    }

    override fun sizeChanged() {
        super.sizeChanged()
        timeAllMap.clear()
    }

    override fun formatMaxAndMin() {
        tsHisList.let {
            val maxEntity: TsLineData? = listVisible.maxByOrNull { it.close }
            val minEntity: TsLineData? = listVisible.minByOrNull { it.close }

            if(minEntity != null && maxEntity != null){
                var offsetGap = max(abs(maxEntity.close - midValue), abs(minEntity.close - midValue))

                if(offsetGap == 0.0){
                    offsetGap = midValue*0.1
                }

                offsetGap = if(offsetGap == 0.0){
                    midValue*0.1
                }else{
                    offsetGap * 1.1
                }
                maxValue = maxEntity.close
                minValue = minEntity.close
                topValue = midValue + offsetGap
                bottomValue = midValue - offsetGap
                formatLeftValue()
                formatRightValue()

                if(timeAllMap.isNullOrEmpty() && chartWidth != 0f){
                    timeAll.forEachIndexed { index, time ->
                        timeAllMap[time] = getValueX(index)
                    }
                }

                formatXValue()
            }
        }

        assistChartList.forEach {
            it.formatMaxAndMin()
        }
    }

    override fun formatRightValue(gapSize: Int) {
        if(midValue != 0.0){
            textPaint.textSize = textSizeDef

            yRightValue.clear()
            val yStepSize = (topValue - bottomValue) / gapSize
            val yGapSize = chartHeight / gapSize

            yLeftValue.forEachIndexed { index, chartXYValue ->
                val leftBound = Rect()
                val value = 100*(topValue-index*yStepSize - midValue)/midValue

                formatDouble(value).let {
                    "$it%".let {
                        textPaint.getTextBounds(it, 0, it.length, leftBound)
                        yRightValue.add(ChartXYValue(it, chartWidth, index * yGapSize, leftBound))
                    }
                }
            }
        }
    }

    override fun formatXValue() {
        tsHisList.firstOrNull()?.let { tsHis ->
            xValue.clear()
            val bound = Rect()

            tsHis.tradeTime.forEach { timeBean ->
                TimeUtils.millis2String(timeBean.start,"HH:mm").let { time ->
                    textPaint.getTextBounds(time, 0, time.length, bound)

                    getXByTime(timeBean.start)?.let { x ->
                        var realX = x
                        if(xValue.size > 0)
                            realX -= bound.width() / 2f
                        xValue.add(ChartXYValue(time, realX,chartHeight + bound.height() + dp2px(2f),bound))
                    }
                }
            }

            tsHis.tradeTime.lastOrNull()?.end?.let {
                TimeUtils.millis2String(it,"HH:mm").let { time ->
                    textPaint.getTextBounds(time, 0, time.length, bound)
                    xValue.add(ChartXYValue(time, chartWidth - bound.width(),chartHeight + bound.height() + dp2px(2f),bound))
                }
            }
        }
    }

    override fun onSingleTapUp(ev: MotionEvent): Boolean {
        if(!showCross){
            switchMain()
        }
        return super.onSingleTapUp(ev)
    }

    /**
     * 切换主图指标
     */
    @Suppress("UNCHECKED_CAST")
    private fun switchMain(){
        mSingleClick?.onChartSingleClick(this as BaseChart<Any>)

        if(touchChart is TsAssistChart){
            (touchChart as TsAssistChart).switchAssist()
            invalidate()
        }
    }

    override fun requestParentDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        assistChartList.forEach {
            it.requestParentDisallowInterceptTouchEvent(disallowIntercept)
        }
        super.requestParentDisallowInterceptTouchEvent(disallowIntercept)
    }

    /**
     * 更新推送数据
     */
    open fun updateQuote(quoteBean: QuoteBean?) {
        quoteBean?.let { quoteBean ->
            if(timeAll.isNotEmpty() && quoteBean.recentTime !in timeAll.first()..timeAll.last()){
                timeAll.minByOrNull { abs(quoteBean.recentTime - it) }?.let {
                    quoteBean.recentTime = it
                }
            }

            if(!isShowGoldenCut() && timeAll.contains(quoteBean.recentTime)){
                val lastData = listVisible.lastOrNull()
                when {
                    lastData == null -> listVisible.add(TsLineData(quoteBean))
                    lastData.time == quoteBean.recentTime -> lastData.apply {
                        close = quoteBean.currentPrice
                        holding += quoteBean.holding
                        volume += quoteBean.vol
                    }
                    else -> listVisible.add(TsLineData(quoteBean))
                }
                DataFormatHelper.calculateTs(listVisible, quoteBean.id, quoteBean.contractSize)
                formatMaxAndMin()
                postInvalidate()
            }
        }
    }

    override fun onLongPress(ev: MotionEvent) {
        if(isShowingGoldCut()){
            super.onLongPress(ev)
        }else if(!showCross){
            showCross = true
            crossX = ev.x - marginLeft
            crossY = ev.y
            invalidate()
        }
    }

    /**
     * 通过时间查找横坐标
     */
    protected fun getXByTime(time: Long): Float? = timeAllMap[time]

    /**
     * 财经地雷信息
     */
    class FinanceInfo(var time: Long, //时间
                      var finances: List<CalendarBean>, //信息
                      var x: Float = 0f, //横坐标
                      val circleRect: RectF = RectF(0f,0f,0f,0f), //圆点区域（用于点击）
                      val rects: List<RectF> = mutableListOf()) //信息区域（用于点击）

}