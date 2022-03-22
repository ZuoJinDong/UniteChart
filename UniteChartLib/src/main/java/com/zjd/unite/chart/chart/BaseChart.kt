package com.zjd.unite.chart.chart

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import com.zjd.unite.chart.R
import com.zjd.unite.chart.utils.*
import com.zjd.unite.chart.utils.formatDouble
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * @author ZJD
 * @date 2021/4/1
 * @desc
 **/
abstract class BaseChart<T : Any> @JvmOverloads constructor(val mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RelativeLayout(mContext, attrs, defStyleAttr), GestureDetector.OnGestureListener {

    /** 手势 */
    protected var gestureDetector: GestureDetector? = null
    /** 颜色-红 */
    protected var colorRed = getColor(mContext, R.color.uc_increase)
    /** 颜色-绿 */
    protected var colorGreen = getColor(mContext, R.color.uc_decrease)
    /** 颜色-灰 */
    protected var colorGray = getColor(mContext, R.color.uc_candle_gray)
    /** 颜色-橙 */
    protected var colorOrange = getColor(mContext, R.color.uc_orange)
    /** 颜色-蓝 */
    protected var colorBlue = getColor(mContext, R.color.uc_blue)
    /** 颜色-十字光标 */
    protected var colorCross = getColor(mContext, R.color.uc_cross)
    /** 颜色-选中数据背景 */
    protected var colorRectSolid = getColor(mContext, R.color.uc_chart_rect_solid)
    /** 颜色-阴影 */
    protected var colorShadow = getColor(mContext, R.color.uc_ts_shadow)
    /** 颜色-紫 */
    protected var colorPurple = getColor(mContext, R.color.ma4)
    /** 颜色-边线 */
    protected var lineColor = getColor(mContext, R.color.uc_chart_grid)
    /** 颜色-文字 */
    protected var textColor = getColor(mContext, R.color.uc_text_light)
    /** 颜色-文字 */
    protected var textColorDark = getColor(mContext, R.color.uc_text_dark)
    /** 颜色-背景-半透明 */
    protected var colorDarkAlpha = getColor(mContext, R.color.uc_half_trans)
    /** 默认字体大小 */
    protected var textSizeDef = sp2px(10f)
    /** 控件宽 */
    protected var fullWidth = 0f
    /** 控件高 */
    protected var fullHeight = 0f
    /** 图表宽 */
    protected var chartWidth = 0f
    /** 图表高 */
    protected var chartHeight = 0f
    /** 上边距 */
    protected var marginTop = dp2px(0f)
    /** 下边距 */
    protected var marginBottom = dp2px(15f)
    /** 左边距 */
    protected var marginLeft = dp2px(5f)
    /** 右边距 */
    protected var marginRight = dp2px(5f)
    /** 边框圆角 */
    protected var rectRadius = dp2px(2f)
    /** 虚线 */
    val dashPathEffect = DashPathEffect(floatArrayOf(dp2px(2f), dp2px(2f)), 0f)
    val dashPathEffect1 = DashPathEffect(floatArrayOf(dp2px(1f), dp2px(1f)), 0f)
    val dashPathEffect4 = DashPathEffect(floatArrayOf(dp2px(4f), dp2px(4f)), 0f)
    /** 画笔-线 */
    protected var linePaint = Paint().apply {
        isAntiAlias = true
        color = lineColor
        style = Paint.Style.STROKE
        strokeWidth = dp2px(0.5f)
    }
    /** 画笔-文字 */
    protected var textPaint = TextPaint().apply {
        isAntiAlias = true
        color = textColor
        style = Paint.Style.FILL_AND_STROKE
        textSize = textSizeDef
    }

    /** 左侧文字最大宽度 */
    protected var yLeftWidth = 0f
    /** 右侧文字最大宽度 */
    protected var yRightWidth = 0f

    /** 小数位数 */
    var mDec = 2
    /** 顶部值 */
    protected var topValue = 2000.0
    /** 底部值 */
    protected var bottomValue = -1000.0
    /** 最大值 */
    protected var maxValue = 2000.0
    /** 最小值 */
    protected var minValue = -1000.0
    /** 左侧指标 */
    protected var yLeftValue = mutableListOf<ChartXYValue>()
    /** 右侧指标 */
    protected var yRightValue = mutableListOf<ChartXYValue>()
    /** 底部指标 */
    protected var xValue = linkedSetOf<ChartXYValue>()
    /** 可见最大数据量 */
    protected var visibleCountMax = 300
    /** 可见最小数据量 */
    var visibleCountMin = 10
    /** 可见默认数据量 */
    protected var visibleCount = 100
    /** 可见最后一个数据 */
    protected var visibleLastIndex = 99
    //蜡烛棒 宽度
    protected var candleWidth = 0f

    /** 全部数据 */
    protected val listFull = mutableListOf<T>()
    /** 显示数据 */
    var listVisible = mutableListOf<T>()

    /** 是否显示十字光标 */
    var showCross = false
    /** 十字标数据 */
    var crossData: T? = null
    /** 图表截图 */
    protected var canvasBitmap: Bitmap? = null
    /** 选中数据 */
    var onDataSelectListener: OnDataSelectListener<T>? = null
    /** 左侧数值是否简化单位 */
    var yLeftUnit = false

    /** 滑动动画 */
    protected var flingAnim: ValueAnimator = ValueAnimator.ofFloat().apply {
        duration = 2000
        addUpdateListener {
            it.animatedValue.toString().toFloat().let { value ->
                if(!value.isNaN()){
                    moveToLeft = value > 0
                    moveDistance += value
                }
            }
            changeLastVisible()
        }
        interpolator = DecelerateInterpolator()
    }

    /** 1分钟时间 */
    protected val oneMinuteLong = 60000L
    /** 1天时间 */
    protected val oneDayLong = 24*60*oneMinuteLong
    /** 图表点击 */
    protected var mSingleClick: OnChartSingleClick? = null

    init {
        initParam()
    }

    private fun initParam() {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        gestureDetector = GestureDetector(mContext, this)
        //设置背景
//        setBackgroundColor(getColor(context, R.color.chart_bg))
    }

    /**
     * 最后一条数据变动
     */
    abstract fun lastVisibleChanged()

    /**
     * 全部数据
     */
    fun getAllData(): List<T> = listFull

    /**
     * 测试数据
     */
    protected fun initTestValue() {
        formatLeftValue()
        formatRightValue()
        formatXValue()
    }

    open fun hideCross(){
        if(showCross){
            canvasBitmap = null
            showCross = false
            invalidate()
        }
    }

    /**
     * 计算右侧值
     */
    open fun formatRightValue(gapSize: Int = 4) {
        textPaint.textSize = textSizeDef

        yRightValue.clear()
        val yStepSize = (topValue - bottomValue) / gapSize
        val yGapSize = chartHeight / gapSize
        for (index in 0..gapSize){
            val leftBound = Rect()
            val value = topValue-index*yStepSize
            formatDouble(value, mDec).let {
                textPaint.getTextBounds(it, 0, it.length, leftBound)
                yRightValue.add(ChartXYValue(it, chartWidth, index * yGapSize, leftBound))
            }
        }
    }

    /**
     * 计算左侧值
     */
    open fun formatLeftValue(gapSize: Int = 4) {
        if(chartWidth == 0f){
            return
        }

        textPaint.textSize = textSizeDef

        yLeftValue.clear()
        val yStepSize = (topValue - bottomValue) / gapSize
        val yGapSize = chartHeight / gapSize
        for (index in 0..gapSize){
            val leftBound = Rect()
            val value = topValue-index*yStepSize

            val valueStr = if(yLeftUnit){
                formatDoubleUnit(value, mDec)
            }else{
                formatDouble(value, mDec)
            }

            valueStr.let {
                textPaint.getTextBounds(it, 0, it.length, leftBound)
                yLeftValue.add(ChartXYValue(it, chartWidth, index * yGapSize, leftBound))
            }
        }
    }

    open fun formatXValue() {

    }

    open fun sizeChanged() {

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if(fullWidth != measuredWidth.toFloat() || fullHeight != measuredHeight.toFloat()){
            fullWidth = measuredWidth.toFloat()
            fullHeight = measuredHeight.toFloat()
            chartWidth = fullWidth - marginLeft - marginRight
            chartHeight = fullHeight - marginTop - marginBottom
            sizeChanged()
            formatMaxAndMin()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        try {
            if(canvasBitmap != null){
                canvas.drawBitmap(canvasBitmap!!, 0f, 0f, linePaint)
                canvas.translate(marginLeft, marginTop)
                if(showCross){
                    drawCross(canvas)
                }
                drawStaticData(canvas)
            }else{
                canvas.save()
                clipPath(canvas)
                canvas.translate(marginLeft, marginTop)
                drawOutline(canvas)
                drawDashLineY(canvas)
                drawData(canvas)
                drawYLeftValue(canvas)
                drawYRightValue(canvas)
                drawTopView(canvas)
                canvas.restore()
                canvas.translate(marginLeft, marginTop)
                drawXValue(canvas)
                if(!showCross){
                    listVisible.lastOrNull()?.let {
                        onDataSelectListener?.onDataSelect(it, showCross, crossX + marginLeft)
                    }
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    /**
     * 剪切边界
     */
    open fun clipPath(canvas: Canvas) {
        val clip = Path()
        clip.moveTo(marginLeft - linePaint.strokeWidth, marginTop - linePaint.strokeWidth)
        clip.lineTo(fullWidth - marginRight + linePaint.strokeWidth / 2, marginTop - linePaint.strokeWidth)
        clip.lineTo(fullWidth - marginRight + linePaint.strokeWidth / 2, chartHeight + marginTop + linePaint.strokeWidth / 2)
        clip.lineTo(marginLeft - linePaint.strokeWidth, chartHeight + marginTop + linePaint.strokeWidth / 2)
        clip.close()
        canvas.clipPath(clip)
    }

    /**
     * 十字光标
     */
    open fun drawCross(canvas: Canvas) {
        linePaint.color = colorCross
        canvas.drawLine(crossX, 0f, crossX, chartHeight, linePaint)
        canvas.drawLine(0f, crossY, chartWidth, crossY, linePaint)
    }

    /**
     * 底部值
     */
    open fun drawXValue(canvas: Canvas) {
        textPaint.textSize = textSizeDef
        textPaint.color = textColor
        xValue.forEachIndexed { index, chartXYValue ->
            chartXYValue.apply {
                canvas.drawText(value, 0, value.length, x, y, textPaint)
            }
        }
    }

    /**
     * 图表数据
     */
    abstract fun drawData(canvas: Canvas)

    /**
     * 静态数据
     */
    open fun drawStaticData(canvas: Canvas){

    }

    /**
     * 左侧值
     */
    open fun drawYLeftValue(canvas: Canvas) {
        textPaint.color = textColor
        textPaint.textSize = textSizeDef
        yLeftValue.forEachIndexed { index, chartXYValue ->
            chartXYValue.apply {
                if(index == yLeftValue.size - 1){
                    canvas.drawText(value, dp2px(3f), y - dp2px(2f), textPaint)
                }else{
                    canvas.drawText(value, dp2px(3f), y + dp2px(2f) + bounds.height(), textPaint)
                }
            }
        }
    }

    /**
     * 顶层绘制
     */
    open fun drawTopView(canvas: Canvas) {

    }

    /**
     * 右侧值
     */
    open fun drawYRightValue(canvas: Canvas) {
        textPaint.textSize = textSizeDef
        yRightValue.forEachIndexed { index, chartXYValue ->
            chartXYValue.apply {
                if(index == yRightValue.size - 1){
                    canvas.drawText(value, 0, value.length, chartWidth - bounds.width() - dp2px(3f), y - dp2px(2f), textPaint)
                }else{
                    canvas.drawText(value, 0, value.length, chartWidth - bounds.width() - dp2px(3f), y + dp2px(2f) + bounds.height(), textPaint)
                }
            }
        }
    }

    /**
     * 边框
     */
    open fun drawOutline(canvas: Canvas) {
        linePaint.color = lineColor
        linePaint.style = Paint.Style.STROKE
        canvas.drawRect(0f, 0f, chartWidth, chartHeight, linePaint)
    }

    /**
     * 水平虚线
     */
    open fun drawDashLineY(canvas: Canvas) {
        linePaint.pathEffect = dashPathEffect
        yLeftValue.forEachIndexed { index, chartXYValue ->
            if(index != 0 && index != yLeftValue.size - 1){
                chartXYValue.apply {
                    canvas.drawLine(0f, y, chartWidth, y, linePaint)
                }
            }
        }
        linePaint.pathEffect = null
    }
    /** 每个点横向间隔 */
    protected var step = 0f

    open fun getValueX(index: Int): Float{
        step = chartWidth / visibleCount
        return (index + 0.5f)*step
    }

    /**
     * 由值获取纵坐标
     */
    open fun getValueY(value: Double): Float{
        val offsetPercent = (topValue - value)/(topValue - bottomValue)
        return (chartHeight*offsetPercent).toFloat()
    }

    /**
     * 由值获取纵坐标
     */
    fun getValueY(value: Float): Float{
        val offsetPercent = (topValue - value)/(topValue - bottomValue)
        return (chartHeight*offsetPercent).toFloat()
    }

    /**
     * 由纵坐标获取值
     */
    fun getValueByY(y: Float): Double{
        return topValue - y * (topValue - bottomValue)/chartHeight
    }

    /**
     * 获取y对应价格
     */
    protected open fun getRealPrice(y: Float): Double {
        return (1f - y / chartHeight) * (topValue - bottomValue) + bottomValue
    }

    /**
     * 数据宽度
     */
    fun getDataStepSize(): Float = if(visibleCount == 0) chartWidth else chartWidth / visibleCount

    /** 移动距离 */
    protected var moveDistance = 0f
    /**
     * 移动方向
     * false 向右
     * true  向左
     */
    protected var moveToLeft = true
    /** 触摸源图表 */
    var touchChart: BaseChart<T>? = null

    open fun onTouchEvent(event: MotionEvent, touchChart: BaseChart<T>?): Boolean{
        this.touchChart = touchChart
        try {
            gestureDetector?.onTouchEvent(event)
        }catch (e:Exception){
            e.printStackTrace()
        }
        if(showCross){
            requestParentDisallowInterceptTouchEvent(true)
        }
        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (showCross) {
                    crossX = event.x - marginLeft
                    crossY = event.y
                    invalidate()
                } else if (event.pointerCount >= 2 && canvasBitmap == null) {
                    requestParentDisallowInterceptTouchEvent(true)
                    if (downDistance == 0f) {
                        downDistance = abs(event.getX(0) - event.getX(1))
                    } else {
                        //双指缩放
                        val scaleDistance = abs(event.getX(0) - event.getX(1))
                        val temp: Int = ((downDistance - scaleDistance) / getDataStepSize() / 10).roundToInt()
                        visibleCount += temp
                        if (visibleCount > visibleCountMax) {
                            visibleCount = visibleCountMax
                        } else if (visibleCount < visibleCountMin) {
                            visibleCount = visibleCountMin
                        }
                        changeLastVisible(true)
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                downDistance = 0f
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return onTouchEvent(event, null)
    }

    open fun requestParentDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        parent?.requestDisallowInterceptTouchEvent(disallowIntercept)
    }

    /** 展示尽头1/3空数据 0：关 1：开 */
    protected var emptyOffsetFlag = 0

    /**
     * 计算最大最小值
     */
    abstract fun formatMaxAndMin()

    /**
     * 左右滑动改变数据
     */
    open fun changeLastVisible(scaleMode: Boolean = false) {
        val offsetIndex :Int = try{
            -(moveDistance/getDataStepSize()).roundToInt()
        }catch (e: Exception){
            0
        }

        if(!scaleMode){
            if(offsetIndex != 0){
                moveDistance = 0f
            }else{
                return
            }
        }
        visibleLastIndex+=offsetIndex
        if(visibleLastIndex > listFull.size - 1 + emptyOffsetFlag*visibleCount/3){
            visibleLastIndex = listFull.size - 1 + emptyOffsetFlag*visibleCount/3
        }

        listVisible.clear()

        if(listFull.size > visibleCount){
            if(visibleLastIndex > listFull.size - 1){
                val offset = visibleCount - (visibleLastIndex - listFull.size + 1)
                listVisible.addAll(listFull.takeLast(offset))
            }else{
                val startIndex = visibleLastIndex + 1 - visibleCount
                if(startIndex < 0){
                    visibleLastIndex = visibleCount
                    listVisible.addAll(listFull.subList(0, visibleCount))
                }else{
                    listVisible.addAll(listFull.subList(startIndex, visibleLastIndex + 1))
                }
            }
        }else{
            listVisible.addAll(listFull)
        }
        formatMaxAndMin()
        postInvalidate()
    }

    /**
     * 按下
     */
    override fun onDown(ev: MotionEvent): Boolean {
        flingAnim.pause()
        return false
    }

    /**
     *
     */
    override fun onShowPress(ev: MotionEvent) {
//        ToastUtils.showShort("onShowPress")
    }

    /**
     * 点击
     */
    override fun onSingleTapUp(ev: MotionEvent): Boolean {
        hideCross()
        return false
    }

    private var downDistance = 0f
    /** 十字光标X */
    var crossX = 0f
    /** 十字光标Y */
    var crossY = 0f

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        if(e2.pointerCount < 2 && !showCross && canvasBitmap == null){
            //单指滑动
            requestParentDisallowInterceptTouchEvent(true)
            downDistance = 0f
            moveDistance -= velocityX
            moveToLeft = velocityX < 0
            changeLastVisible()
        }
        return false
    }

    override fun onLongPress(ev: MotionEvent) {
        if(!showCross){
            canvasBitmap = getBackgroundBitmap()
            showCross = true
            crossX = ev.x - marginLeft
            crossY = ev.y
            invalidate()
        }
    }

    protected fun drawDataRect(canvas: Canvas, rectF: RectF){
        linePaint.color = colorRectSolid
        linePaint.style = Paint.Style.FILL
        canvas.drawRoundRect(
            rectF,
            rectRadius,
            rectRadius,
            linePaint)

        linePaint.color = colorCross
        linePaint.style = Paint.Style.STROKE
        canvas.drawRoundRect(
            rectF,
            rectRadius,
            rectRadius,
            linePaint)
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        if(e1.pointerCount < 2 && !showCross && canvasBitmap == null){
            if(abs(velocityX) > 3000f){
                flingAnim.setFloatValues(flingSpeed(velocityX), 0f)
                flingAnim.start()
            }
        }
        return false
    }

    /**
     * 计算滑动速度
     * 显示数据越多速度越慢
     */
    private fun flingSpeed(velocityX: Float): Float{
        val period: Float = (visibleCountMax - visibleCountMin).toFloat()
        val offset = visibleCount - visibleCountMin
        return velocityX/(50f + 100f*offset/period)
    }

    fun setOnChartSingleClick(listener: OnChartSingleClick){
        mSingleClick = listener
    }

    protected open fun getBackgroundBitmap(): Bitmap? {
        return try {
            this.isDrawingCacheEnabled = true
            this.buildDrawingCache() //启用DrawingCache并创建位图
            val bitmap = Bitmap.createBitmap(this.drawingCache) //创建一个DrawingCache的拷贝，因为DrawingCache得到的位图在禁用后会被回收
            this.isDrawingCacheEnabled = false //禁用DrawingCahce否则会影响性能
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    interface OnDataSelectListener<T>{
        fun onDataSelect(data: T, showCross: Boolean, crossX: Float)
    }

    /** 数据是否为空 */
    fun isDataEmpty(): Boolean = listFull.isNullOrEmpty()

}

class ChartXYValue(var value: String, //文字
                   var x: Float, //横坐标
                   var y: Float, //纵坐标
                   var bounds: Rect) //文字区域

interface OnChartSingleClick {
    fun onChartSingleClick(chart: BaseChart<Any>)
}