package com.zjd.unite.chart.chart

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import com.zjd.unite.chart.utils.dp2px
import com.zjd.unite.chart.utils.roundStr
import java.util.*
import kotlin.math.roundToInt

/**
 * @author ZJD
 * @date 2021/4/26
 * @desc 黄金分割
 **/
abstract class GoldenCutChart<T : Any> @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
): BaseChart<T>(context, attrs, defStyleAttr) {

    /** 黄金分割 */
    //分割比例
    private var goldenRates: DoubleArray = doubleArrayOf(0.000, 0.191, 0.382, 0.500, 0.618, 0.809, 1.000)
    // 最大值 最小值
    private var mGoldenMax: Double? = null
    private var mGoldenMin: Double? = null
    // 是否上涨趋势
    private var isGoldUp = false
    // 0: 无操作 1：移动最大值 2：移动最小值 3：整体移动 4：最大值横移 5：最小值横移
    private var goldenFlag = 0
    //每步分割线
    private val mCutSteps: MutableList<DoubleArray> = ArrayList()
    //当前分割线
    private var goldenPrices = DoubleArray(goldenRates.size)
    //最大值圆点触控区域
    private val goldenTopRect = RectF()
    //最小值圆点触控区域
    private val goldenBottomRect = RectF()
    //分割线触控区域
    private val goldenCutRect = RectF()
    //上一次触摸X
    private var goldenDownY = 0f
    //最大值圆点X
    private var goldenMaxX = 0f
    //最小值圆点X
    private var goldenMinX = 0f
    //放大视图
    private var goldenScaleBitmap: Bitmap? = null
    //执行分割截图
    private var goldenCutCatch = false
    //黄金分割线绘制状态
    private val mGoldenCutStateListener: OnGoldenCutStateListener? = null

    /** 画笔-bitmap */
    private var bgBitmapPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    /** 打开/关闭黄金分割 */
    abstract fun openGoldenCut(open: Boolean)

    fun isShowGoldenCut(): Boolean = !mCutSteps.isNullOrEmpty()

    protected fun clearGoldenParams(){
        mGoldenMin = null
        mGoldenMax = null
        mCutSteps.clear()
        invalidate()
    }

    /**
     * 子类 openGoldenCut中 设置黄金分割参数
     * @param max 最大值
     * @param min 最小值
     * @param isUp 是否上涨趋势
     */
    protected fun formatGoldenParams(max: Double, min: Double, isUp: Boolean){
        canvasBitmap = null
        showCross = false
        mGoldenMax = max
        mGoldenMin = min
        goldenMaxX = chartWidth / 2f
        goldenMinX = chartWidth / 2f
        isGoldUp = isUp
        goldenPrices = formatGoldenPrices()
        mCutSteps.add(goldenPrices)
        invalidate()
    }

    /**
     * 计算分割线价格
     */
    private fun formatGoldenPrices(): DoubleArray {
        val prices = DoubleArray(goldenRates.size)
        mGoldenMax?.let { max ->
            mGoldenMin?.let { min ->
                val offset: Double = max - min
                for (i in goldenRates.indices) {
                    prices[i] = max - goldenRates[i] * offset
                }
            }
        }
        return prices
    }

    /**
     * 在顶层绘制
     */
    override fun drawTopView(canvas: Canvas) {
        drawGoldenCut(canvas)
    }

    /**
     * 黄金分割线
     */
    private fun drawGoldenCut(canvas: Canvas) {
        if (goldenCutCatch) return
        if (mCutSteps.size > 0) {
            mGoldenCutStateListener?.onStateChange(1)
            linePaint.color = colorBlue

            //透明背景
            linePaint.alpha = 25
            linePaint.style = Paint.Style.FILL
            val realYTop: Float = getValueY(goldenPrices[0])
            val realYBottom: Float = getValueY(goldenPrices[goldenPrices.size - 1])
            if(realYTop < realYBottom){
                goldenCutRect.set(0f, realYTop, chartWidth, realYBottom)
            }else{
                goldenCutRect.set(0f, realYBottom, chartWidth, realYTop)
            }
            canvas.drawRect(goldenCutRect, linePaint)

            //分割线
            linePaint.alpha = 255
            textPaint.color = Color.WHITE
            textPaint.textSize = textSizeDef
            for (i in goldenPrices.indices) {
                val realY: Float = getValueY(goldenPrices[i])
                if (i == 0 || i == goldenPrices.size - 1) {
                    //两边实线
                    linePaint.pathEffect = null
                } else {
                    //中间虚线
                    linePaint.pathEffect = dashPathEffect4
                }
                //线
                canvas.drawLine(0f, realY, chartWidth, realY, linePaint)

                //文字
                var text = if (isGoldUp) {
                    "${goldenRates[i] * 100}%"
                } else {
                    "${goldenRates[goldenRates.size - 1 - i] * 100}%"
                }
                val textRect = Rect()
                textPaint.getTextBounds(text, 0, text.length, textRect)
                text += "(${roundStr(goldenPrices[i], mDec)})"
                canvas.drawRect(0f, realY - textRect.height() / 1.5f, textPaint.measureText(text) + dp2px(2f), realY + textRect.height() / 1.5f, linePaint)
                canvas.drawText(text, dp2px(1f), realY + textRect.height() / 2f, textPaint)

                //上下圆点
                linePaint.alpha = 30
                val touchRadius: Float = dp2px(10f)
                goldenTopRect.set(
                        goldenMaxX - touchRadius,
                        realYTop - touchRadius,
                        goldenMaxX + touchRadius,
                        realYTop + touchRadius)
                goldenBottomRect.set(
                        goldenMinX - touchRadius,
                        realYBottom - touchRadius,
                        goldenMinX + touchRadius,
                        realYBottom + touchRadius)
                canvas.drawCircle(goldenMaxX, realYTop, dp2px(4f), linePaint)
                canvas.drawCircle(goldenMinX, realYBottom, dp2px(4f), linePaint)
                linePaint.alpha = 255
                canvas.drawCircle(goldenMaxX, realYTop, dp2px(2f), linePaint)
                canvas.drawCircle(goldenMinX, realYBottom, dp2px(2f), linePaint)
            }
            drawGoldenScale(canvas)
        } else {
            mGoldenCutStateListener?.onStateChange(0)
            goldenTopRect.set(0f, 0f, 0f, 0f)
            goldenBottomRect.set(0f, 0f, 0f, 0f)
            goldenCutRect.set(0f, 0f, 0f, 0f)
        }
    }

    private val scaleBitmapDst = RectF()
    private val scaleBitmapSrc = Rect()

    /**
     * 绘制放大区域
     */
    private fun drawGoldenScale(canvas: Canvas) {
        if (goldenScaleBitmap != null && mGoldenMax != null && mGoldenMin != null) {
            val scaleCenterX: Float
            val scaleCenterY: Float
            if (goldenFlag == 4) {
                scaleCenterX = goldenMaxX
                scaleCenterY = getValueY(mGoldenMax!!)
            } else {
                scaleCenterX = goldenMinX
                scaleCenterY = getValueY(mGoldenMin!!)
            }
            val scaleCircleX: Float
            val scaleCircleY: Float = dp2px(30f)
            if (scaleCenterX < chartWidth / 2f) {
                scaleCircleX = chartWidth - dp2px(37.5f)
                scaleBitmapDst.set(
                        chartWidth - dp2px(70f),
                        dp2px(5f),
                        chartWidth - dp2px(5f),
                        dp2px(55f))
            } else {
                scaleCircleX = dp2px(37.5f)
                scaleBitmapDst.set(
                        dp2px(5f),
                        dp2px(5f),
                        dp2px(70f),
                        dp2px(55f))
            }
            scaleBitmapSrc.set(
                    (scaleCenterX - dp2px(18f) + marginLeft).roundToInt(),
                    (scaleCenterY - dp2px(13f) + marginTop).roundToInt(),
                    (scaleCenterX + dp2px(18f) + marginLeft).roundToInt(),
                    (scaleCenterY + dp2px(13f) + marginTop).roundToInt())
            bgBitmapPaint.setShadowLayer(10f, 0f, 0f, lineColor)
            canvas.drawRect(scaleBitmapDst, bgBitmapPaint)
            bgBitmapPaint.clearShadowLayer()
            canvas.drawBitmap(goldenScaleBitmap!!, scaleBitmapSrc, scaleBitmapDst, bgBitmapPaint)
            //中心点
            linePaint.alpha = 60
            canvas.drawCircle(scaleCircleX, scaleCircleY, dp2px(4f), linePaint)
            linePaint.alpha = 255
            canvas.drawCircle(scaleCircleX, scaleCircleY, dp2px(2f), linePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent, touchChart: BaseChart<T>?): Boolean {
        if(touchChart == null && isShowingGoldCut()){
            requestParentDisallowInterceptTouchEvent(true)
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    //判断触摸点是否在三个区域内 三个区域Rect在drawGoldenCut时赋值
                    when {
                        //移动最大值
                        goldenTopRect.contains(event.x, event.y) -> {
                            requestParentDisallowInterceptTouchEvent(true)
                            goldenFlag = 1
                        }
                        //移动最小值
                        goldenBottomRect.contains(event.x, event.y) -> {
                            requestParentDisallowInterceptTouchEvent(true)
                            goldenFlag = 2
                        }
                        //整体移动
                        goldenCutRect.contains(event.x, event.y) -> {
                            requestParentDisallowInterceptTouchEvent(true)
                            goldenFlag = 3
                            goldenDownY = event.y
                        }
                        else -> goldenFlag = 0
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (goldenFlag != 0) {
                        requestParentDisallowInterceptTouchEvent(true)
                        if (goldenFlag == 1 || goldenFlag == 4) {
                            //最大值
                            mGoldenMax = getRealPrice(event.y)
                            if (goldenFlag == 4) {
                                //最大值横移时重置最大值圆心横坐标
                                goldenMaxX = event.x
                            }
                        } else if (goldenFlag == 2 || goldenFlag == 5) {
                            //最小值
                            mGoldenMin = getRealPrice(event.y)
                            if (goldenFlag == 5) {
                                //最小值横移时重置最小值圆心横坐标
                                goldenMinX = event.x
                            }
                        } else if (goldenFlag == 3) {
                            //整体移动 根据纵向偏移重新计算最大最小值
                            mGoldenMax = getRealPrice(getValueY(mGoldenMax!!) + event.y - goldenDownY)
                            mGoldenMin = getRealPrice(getValueY(mGoldenMin!!) + event.y - goldenDownY)
                            goldenDownY = event.y
                        }
                        //重新计算分割点数值
                        goldenPrices = formatGoldenPrices()
                        invalidate()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (goldenFlag != 0) {
                        when (goldenFlag) {
                            1 -> mGoldenMax = getRealPrice(event.y)
                            2 -> mGoldenMin = getRealPrice(event.y)
                            3 -> {
                                mGoldenMax = getRealPrice(getValueY(mGoldenMax!!) + goldenDownY - event.y)
                                mGoldenMin = getRealPrice(getValueY(mGoldenMin!!) + goldenDownY - event.y)
                            }
                        }

                        goldenScaleBitmap?.let {
                            it.recycle()
                            goldenScaleBitmap = null
                        }

                        goldenPrices = formatGoldenPrices()
                        mCutSteps.add(goldenPrices)
                        goldenFlag = 0
                        invalidate()
                    }
                }
            }
        }else if(touchChart != null && (goldenFlag == 4 || goldenFlag == 5)){
            return true
        }
        return super.onTouchEvent(event, touchChart)
    }

    /**
     * 是否在展示黄金分割
     */
    protected fun isShowingGoldCut() = mCutSteps.isNotEmpty()

    /**
     * 长按时判断是否在最大值最小值触摸区域内
     * 是否执行横移
     */
    override fun onLongPress(ev: MotionEvent) {
        if(isShowingGoldCut()){
            if (goldenFlag != 0) {
                if (goldenTopRect.contains(ev.x - marginLeft, ev.y - marginTop)) {
                    goldenFlag = 4
                    getGoldenCutCatch()
                } else if (goldenBottomRect.contains(ev.x - marginLeft, ev.y - marginTop)) {
                    goldenFlag = 5
                    getGoldenCutCatch()
                }
            }
        }else{
            super.onLongPress(ev)
        }
    }

    /**
     * 截图
     * 用于绘制放大区域
     */
    private fun getGoldenCutCatch() {
        goldenCutCatch = true
        invalidate()
        goldenScaleBitmap = getBackgroundBitmap()
        goldenCutCatch = false
        invalidate()
    }

    interface OnGoldenCutStateListener {
        /**
         * @param status
         * 0：关闭
         * 1：开启
         */
        fun onStateChange(status: Int)
    }
}