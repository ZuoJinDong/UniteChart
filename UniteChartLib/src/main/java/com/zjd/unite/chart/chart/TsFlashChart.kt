package com.zjd.unite.chart.chart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import com.zjd.unite.chart.entity.FlashHisBean
import com.zjd.unite.chart.entity.QuoteBean
import com.zjd.unite.chart.utils.dp2px
import com.zjd.unite.chart.utils.formatDouble
import kotlin.math.abs
import kotlin.math.max

/**
 * @author ZJD
 * @date 2021/5/19
 * @desc 闪电图
 **/

class TsFlashChart @JvmOverloads constructor(mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : BaseChart<FlashHisBean>(mContext, attrs, defStyleAttr) {

    /** 行情 */
    private var quoteBean: QuoteBean? = null

    /** 数据 */
    private var tsHisList: MutableList<FlashHisBean> = mutableListOf()
    /** 中线数值 */
    private var midValue = 0.0
    /** 最新点指示半径 */
    private val newPointRadius = dp2px(3f)

    fun setQuoteBean(quoteBean: QuoteBean){
        if(this.quoteBean == null){
            quoteBean.recentTime = 60000 * (quoteBean.recentTime/60000)
            this.quoteBean = quoteBean
        }
        mDec = quoteBean.decPointCount
    }

    fun setDec(dec: Int){
        mDec = dec
    }

    /**
     * 是否加载过
     */
    fun isLoaded(): Boolean{
        return tsHisList.isNotEmpty() || listVisible.isNotEmpty() || quoteBean != null
    }

    fun setTsHisBean(tsHisBean: List<FlashHisBean>?){
        tsHisList.clear()
        tsHisBean?.let {
            tsHisList.addAll(it)
        }

        tsHisBean?.let { tsHis ->

            listVisible.clear()
            listVisible.addAll(tsHisBean)

            visibleCountMax = 600
            visibleCountMin = 600
            visibleCount = 600
            visibleLastIndex = tsHis.takeLast(300).size

            formatMaxAndMin()
        }
    }

    override fun lastVisibleChanged() {

    }

    override fun drawData(canvas: Canvas) {
        /** 主线 */
        drawSingleLine(canvas)
    }

    /**
     * 闪电图不画左侧值
     */
    override fun drawYLeftValue(canvas: Canvas) {

    }

    /**
     * 闪电图不画十字线
     */
    override fun drawCross(canvas: Canvas) {
        //右侧
        formatDouble(listVisible.last().orig, quoteBean?.decPointCount?:2).let { valueRight ->
            textPaint.color = Color.WHITE
            textPaint.textSize = textSizeDef
            val rect = Rect()
            textPaint.getTextBounds(valueRight,0,valueRight.length,rect)
            canvas.drawRect(chartWidth - rect.width() - dp2px(6f),lastY - dp2px(2f) - rect.height()/2, chartWidth,lastY + dp2px(2f) + rect.height()/2, linePaint)
            canvas.drawText(valueRight, chartWidth - rect.width() - dp2px(3f), lastY + rect.height()/2, textPaint)
        }
    }

    /** 最后一点坐标 */
    private var lastX = 0f
    private var lastY = 0f

    /**
     * 连续图
     */
    private fun drawSingleLine(canvas: Canvas) {

        val pathAvg = Path()
        val path = Path()
        linePaint.color = colorBlue

        listVisible.takeLast(300).forEachIndexed { index, data ->
            data.apply {
                getValueX(index).let { x ->
                    val chartY = getValueY(orig)
                    val avgY = getValueY(avgPrice)
                    if(path.isEmpty){
                        pathAvg.moveTo(0f, avgY)
                        path.moveTo(0f, chartY)

                        pathAvg.lineTo(x, avgY)
                        path.lineTo(x, chartY)
                    }else{
                        getValueX(index).let { preX ->
                            pathAvg.lineTo(preX, avgY)
                            path.lineTo(preX, chartY)
                        }
                    }
                    lastX = x
                    lastY = chartY
                }
            }
        }

        if(!path.isEmpty){
            linePaint.color = colorBlue
            canvas.drawPath(path, linePaint)
            linePaint.color = colorOrange
            canvas.drawPath(pathAvg, linePaint)
            linePaint.style = Paint.Style.FILL
            linePaint.color = colorBlue
            canvas.drawLine(lastX,lastY,chartWidth,lastY,linePaint)
        }
    }

    /**
     * 右侧数值
     */
    override fun drawYRightValue(canvas: Canvas) {
        textPaint.textSize = textSizeDef

        yRightValue.forEachIndexed { index, chartXYValue ->
            chartXYValue.apply {
                textPaint.color = textColorDark

                if(index == yRightValue.size - 1){
                    canvas.drawText(value, 0, value.length, chartWidth - bounds.width() - dp2px(3f), y - dp2px(2f), textPaint)
                }else{
                    canvas.drawText(value, 0, value.length, chartWidth - bounds.width() - dp2px(3f), y + dp2px(2f) + bounds.height(), textPaint)
                }
            }
        }

        drawCross(canvas)
    }

    /**
     * 分时图无滑动功能
     */
    override fun changeLastVisible(scaleMode: Boolean) {

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return true
    }

    override fun formatMaxAndMin() {
        tsHisList.let {
            val maxEntity: FlashHisBean? = listVisible.takeLast(300).maxByOrNull { it.orig }
            val minEntity: FlashHisBean? = listVisible.takeLast(300).minByOrNull { it.orig }

            if(minEntity != null && maxEntity != null){
                midValue = (maxEntity.orig + minEntity.orig)/2

                var offsetGap = max(abs(maxEntity.orig - midValue), abs(minEntity.orig - midValue))

                if(offsetGap == 0.0){
                    offsetGap = midValue*0.1
                }

                offsetGap = if(offsetGap == 0.0){
                    midValue*0.1
                }else{
                    offsetGap * 1.1
                }
                maxValue = maxEntity.orig
                minValue = minEntity.orig
                topValue = midValue + offsetGap
                bottomValue = midValue - offsetGap
                formatLeftValue(9)
                formatRightValue(9)
                formatXValue()
            }
        }
    }

    /**
     * 更新推送数据
     */
    fun updateQuote(quoteBean: QuoteBean?) {
        quoteBean?.let { quoteBean ->
            this.quoteBean = quoteBean

            listVisible.add(FlashHisBean().apply {
                val last = listVisible.lastOrNull()

                time = quoteBean.recentTime
                orig = quoteBean.currentPrice
                last?.let {
                    sumPrice = it.sumPrice + quoteBean.currentPrice
                }?:let {
                    sumPrice = quoteBean.currentPrice
                }
                avgPrice = sumPrice/(listVisible.size + 1)
            })
            formatMaxAndMin()
            postInvalidate()
        }
    }

}