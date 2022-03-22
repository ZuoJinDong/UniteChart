package com.zjd.unite.chart.chart

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import com.blankj.utilcode.util.TimeUtils
import com.zjd.unite.chart.entity.QuoteBean
import com.zjd.unite.chart.entity.TsHisBean
import com.zjd.unite.chart.entity.TsLineData
import com.zjd.unite.chart.utils.*
import java.util.*
import kotlin.math.abs

/**
 * @author ZJD
 * @date 2021/4/30
 * @desc 分时主图
 **/

class TsDuoChart @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : TsChart(context, attrs, defStyleAttr) {

    /** 叠加走势 */
    private var overlayMode = false

    /** 选中数据 */
    var onDuoDataSelectListener: OnDuoDataSelectListener? = null

    fun setOverlayMode(overlay: Boolean){
        overlayMode = overlay
        showCross = false
        canvasBitmap = null
        formatRightValue()
        formatXValue()
        postInvalidate()
    }

    fun setTsLineList(list: List<TsHisBean>?){
        tsHisList.clear()
        list?.let {
            tsHisList.addAll(it)
        }
        formatLineData()
    }

    private fun formatLineData(){
        midValue = tsHisList.lastOrNull()?.yersterdayClose?:0.0
        timeAll.clear()
        timeAllMap.clear()

        listVisible.clear()

        tsHisList.forEach { tsHisBean ->
            listVisible.addAll(tsHisBean.pointList)

            tsHisBean.tradeTime.forEach { time ->
                timeAll.addAll(time.getAllTime())
            }
        }

        visibleCountMax = timeAll.size
        visibleCountMin = timeAll.size
        visibleCount = timeAll.size
        visibleLastIndex = timeAll.size

        formatMaxAndMin()
    }

    override fun drawLine(canvas: Canvas) {
        if(overlayMode){
            //叠加
            drawDuoLine(canvas)

            crossDataList.clear()
            tsHisList.forEach { tsHis ->
                val data = tsHis.pointList.lastOrNull()
                val param = "• ${TimeUtils.millis2String(tsHis.billDay,"yyyy-MM-dd")} ${roundStr(data?.close?:0.0, mDec)}"
                crossDataList.add(param)
            }
            onDuoDataSelectListener?.onDuoDataSelect(crossDataList)
        }else{
            //连续
            drawSingleLine(canvas)
        }
    }

    val pathColors = listOf(colorGreen,colorRed,colorBlue,colorOrange,colorPurple)

    /**
     * 叠加图
     */
    private fun drawDuoLine(canvas: Canvas) {
        val pathList = mutableListOf(Path(),Path(),Path(),Path(),Path())

        tsHisList.forEachIndexed { index, tsHisBean ->
            val path = pathList[index]
            val offset = index*chartWidth
            var lastTime = 0L
            var lastY = 0f
            tsHisBean.pointList.forEachIndexed { _, data ->
                data.apply {
                    getXByTime(time)?.let { x ->
                        chartX = x*tsHisList.size - offset
                        val chartY = getValueY(close)
                        if(path.isEmpty){
                            path.moveTo(0f, getValueY(tsHisBean.yersterdayClose))
                            if(time - tsHisBean.tradeTime.first().start > 120000){
                                getXByTime(time - oneMinuteLong)?.let { x ->
                                    val realX = x*tsHisList.size - offset
                                    path.lineTo(realX, getValueY(tsHisBean.yersterdayClose))
                                }
                            }
                            path.lineTo(chartX,chartY)
                        }else{
                            if(time - lastTime > oneMinuteLong){
                                getXByTime(time - oneMinuteLong)?.let { preX ->
                                    val realX = preX*tsHisList.size - offset
                                    path.lineTo(realX, lastY)
                                }?:let {
                                    path.lineTo(chartX, lastY)
                                }
                            }

                            path.lineTo(chartX,chartY)
                        }

                        if(tsHisBean.pointList.last() == this){
                            if(System.currentTimeMillis() >= tsHisBean.tradeTime.last().end){
                                path.lineTo(chartWidth, chartY)
                            }
                        }

                        lastY = chartY
                        lastTime = time
                    }
                }
            }
            if(!path.isEmpty){
                linePaint.color = pathColors[pathColors.size - 1 - index]
                canvas.drawPath(path, linePaint)
            }
        }
    }

    override fun drawOutline(canvas: Canvas) {
        linePaint.color = lineColor
        linePaint.style = Paint.Style.STROKE
        canvas.drawRect(0f, 0f, chartWidth, chartHeight, linePaint)

        if(overlayMode){
            //叠加 绘制时间段分割线
            tsHisList.firstOrNull()?.tradeTime?.let { timeList ->
                timeList.forEachIndexed { index, tradeTimeBean ->
                    if(index > 0){
                        tradeTimeBean.start.let {
                            getXByTime(it)?.let { x ->
                                val realX = x*tsHisList.size
                                canvas.drawLine(realX,0f, realX, chartHeight, linePaint)
                            }
                        }
                    }
                }
            }
        }else{
            //连续 绘制每日分割线（第二天起始时间线）
            tsHisList.forEachIndexed { index, tsHisBean ->
                if(index > 0){
                    tsHisBean.tradeTime.firstOrNull()?.start?.let {
                        getXByTime(it)?.let { x ->
                            canvas.drawLine(x,0f, x, chartHeight, linePaint)
                        }
                    }
                }
            }
        }
    }

    private val crossDataList = mutableListOf<String>()

    /**
     * 十字线
     */
    override fun drawCross(canvas: Canvas) {
        if(overlayMode){
            drawCrossDuo(canvas)
        }else{
            super.drawCross(canvas)
        }
    }

    /**
     * 叠加图十字线
     */
    private fun drawCrossDuo(canvas: Canvas){
        linePaint.color = colorBlue
        linePaint.style = Paint.Style.FILL
        textPaint.color = Color.WHITE
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

        val crossTime = timeAllMap.minByOrNull { abs(it.value - crossX/tsHisList.size) }
        crossTime?.let {
            crossX = crossTime.value*tsHisList.size
            crossDataList.clear()

            tsHisList.forEachIndexed { index, tsHisBean ->
                var data = tsHisBean.pointList.firstOrNull { sameMinute(it.time, crossTime.key) }

                if(data == null){
                    data = tsHisBean.pointList.lastOrNull { it.chartX <= crossX }
                    if(tsHisList.last() == tsHisBean && tsHisBean.pointList.lastOrNull() == data){
                        data = null
                    }
                }

                when{
                    data != null -> roundStr(data.close, mDec)
                    tsHisList.last() == tsHisBean -> roundStr(0.0, mDec)
                    else -> roundStr(tsHisBean.yersterdayClose, mDec)
                }.let { param ->
                    crossDataList.add("• ${TimeUtils.millis2String(tsHisBean.billDay,"yyyy-MM-dd")} $param")
                }

            }
        }

        onDuoDataSelectListener?.onDuoDataSelect(crossDataList)

        canvas.drawLine(crossX,0f,crossX,chartHeight,linePaint)
        canvas.drawLine(0f,crossY,chartWidth,crossY,linePaint)
        textPaint.color = Color.WHITE


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
            canvas.drawRect(0f,rectYBottom - dp2px(4f) - rect.height(), rect.width() + dp2px(6f),rectYBottom, linePaint)
            canvas.drawText(value, dp2px(3f), rectYBottom - dp2px(2f), textPaint)
        }

        //底部数值
        crossTime?.key?.let { time ->
            TimeUtils.millis2String(time,"HH:mm").let { valueX ->
                textPaint.getTextBounds(valueX,0, valueX.length, rect)
                var bottomRectX = crossX - rect.width()/2 - dp2px(3f)
                if(bottomRectX < 0f){
                    bottomRectX = 0f
                }else if(bottomRectX > chartWidth - rect.width() - dp2px(6f)){
                    bottomRectX = chartWidth - rect.width() - dp2px(6f)
                }
                canvas.drawRect(bottomRectX,chartHeight - rect.height() - dp2px(4f),bottomRectX + rect.width() + dp2px(6f), chartHeight, linePaint)
                canvas.drawText(valueX, bottomRectX + dp2px(3f) , chartHeight - dp2px(2f), textPaint)
            }
        }
    }

    /**
     * 不同日期相同小时分钟
     */
    private fun sameMinute(time1: Long, time2: Long): Boolean{
        val calendar1 = Calendar.getInstance().apply {
            timeInMillis = time1
        }
        val calendar2 = Calendar.getInstance().apply {
            timeInMillis = time2
        }
        val h1 = calendar1.get(Calendar.HOUR_OF_DAY)
        val h2 = calendar2.get(Calendar.HOUR_OF_DAY)
        val m1 = calendar1.get(Calendar.MINUTE)
        val m2 = calendar2.get(Calendar.MINUTE)
        return h1 == h2 && m1 == m2
    }

    override fun formatRightValue(gapSize: Int) {
        if(overlayMode){
            //叠加
            yRightValue.clear()
        }else {
            //连续
            super.formatRightValue(gapSize)
        }
    }

    override fun formatXValue() {
        tsHisList.let { list ->
            xValue.clear()
            val bound = Rect()

            if(overlayMode){
                //叠加 单日时间段
                list.firstOrNull()?.tradeTime?.let { timeList ->
                    timeList.forEach { timeBean ->
                        TimeUtils.millis2String(timeBean.start,"HH:mm").let { time ->
                            textPaint.getTextBounds(time, 0, time.length, bound)
                            var x = getValueX(timeAll.indexOf(timeBean.start))*list.size

                            if(xValue.size > 0){
                                x -= bound.width() / 2f
                            }

                            xValue.add(ChartXYValue(time, x,chartHeight + bound.height() + dp2px(2f),bound))
                        }
                    }

                    timeList.lastOrNull()?.end?.let {
                        TimeUtils.millis2String(it,"HH:mm").let { time ->
                            textPaint.getTextBounds(time, 0, time.length, bound)
                            xValue.add(ChartXYValue(time, chartWidth - bound.width(),chartHeight + bound.height() + dp2px(2f),bound))
                        }
                    }
                }
            }else{
                //连续 交易日时间
                list.forEachIndexed { index, tsHisBean ->
                    val billDay = tsHisBean.billDay
                    val startTime = tsHisBean.tradeTime.first().start
                    TimeUtils.millis2String(billDay,"MM-dd").let { time ->
                        textPaint.getTextBounds(time, 0, time.length, bound)
                        val x = getValueX(timeAll.indexOf(startTime))
                        xValue.add(ChartXYValue(time, x,chartHeight + bound.height() + dp2px(2f),bound))
                    }
                }
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

    interface OnDuoDataSelectListener{
        fun onDuoDataSelect(params: List<String>)
    }

    override fun updateQuote(quoteBean: QuoteBean?) {
        quoteBean?.let {
            if(!showCross && !isShowGoldenCut() && timeAll.contains(quoteBean.recentTime)){

                tsHisList.lastOrNull()?.pointList?.let { pointList ->
                    val lastData = pointList.lastOrNull()

                    when {
                        lastData == null -> pointList.add(TsLineData(quoteBean))
                        lastData.time == quoteBean.recentTime -> lastData.close = quoteBean.currentPrice
                        else -> {
                            if(quoteBean.recentTime - lastData.time > oneMinuteLong){
                                pointList.add(TsLineData(lastData, quoteBean.recentTime - oneMinuteLong))
                            }
                            pointList.add(TsLineData(quoteBean))
                        }
                    }
                    DataFormatHelper.calculateTs(pointList, quoteBean.id, quoteBean.contractSize)

                    formatLineData()
                    formatMaxAndMin()
                    postInvalidate()
                }
            }
        }
    }
}