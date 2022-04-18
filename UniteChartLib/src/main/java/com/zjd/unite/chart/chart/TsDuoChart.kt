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
        drawSingleLine(canvas)
    }

    val pathColors = listOf(colorGreen,colorRed,colorBlue,colorOrange,colorPurple)

    override fun drawOutline(canvas: Canvas) {
        linePaint.color = lineColor
        linePaint.style = Paint.Style.STROKE
        canvas.drawRect(0f, 0f, chartWidth, chartHeight, linePaint)

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

    override fun formatXValue() {
        tsHisList.let { list ->
            xValue.clear()
            val bound = Rect()
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

    @Suppress("UNCHECKED_CAST")
    override fun onSingleTapUp(ev: MotionEvent): Boolean {
        if(!showCross){
            mSingleClick?.onChartSingleClick(this as BaseChart<Any>)
        }
        return super.onSingleTapUp(ev)
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