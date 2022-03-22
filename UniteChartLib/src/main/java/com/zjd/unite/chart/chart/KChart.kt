package com.zjd.unite.chart.chart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.util.AttributeSet
import android.view.MotionEvent
import com.blankj.utilcode.util.ThreadUtils
import com.zjd.unite.chart.R
import com.zjd.unite.chart.constant.Constant
import com.zjd.unite.chart.constant.ChartConstant
import com.zjd.unite.chart.entity.*
import com.zjd.unite.chart.utils.*
import java.util.*
import kotlin.collections.LinkedHashSet
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * @author ZJD
 * @date 2021/4/1
 * @desc K线主图
 **/

const val MAIN_TYPE_MA = "MA"
const val MAIN_TYPE_BOLL = "BOLL"
const val MAIN_TYPE_MABOLL = "MABOLL"
const val MAIN_TYPE_SAR = "SAR"
const val MAIN_TYPE_EXPMA = "EXPMA"
/** 趋势先锋 */
const val MAIN_TYPE_QSXF = "趋势先锋"
/** 集金策略 */
const val MAIN_TYPE_JJCL = "集金策略"

class KChart @JvmOverloads constructor(mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : GoldenCutChart<KLineData>(mContext, attrs, defStyleAttr) {

    /** 源数据（未合并） */
    private val source = mutableListOf<KLineData>()
    /** 展示数据（未合并） */
    private var sourceVisible = mutableListOf<KLineData>()

    /** 指标 */
    private val typeList = listOf(
            MAIN_TYPE_MA,
            MAIN_TYPE_BOLL,
            MAIN_TYPE_MABOLL,
            MAIN_TYPE_SAR,
            MAIN_TYPE_EXPMA,
            MAIN_TYPE_QSXF,
            MAIN_TYPE_JJCL)

    /** 主图指标 */
    var mainType = MAIN_TYPE_MA
    /** 副图 */
    private val assistChartList = mutableListOf<KAssistChart>()
    /** 周期 */
    var periodTag = ChartConstant.PERIOD_DAY
    /** 行情 */
    private lateinit var quoteBean: QuoteBean
    /**
     * 模拟交易类型
     * 0：无轨迹
     * 1：外盘
     * 2：TD
     * 3：期货
     */
    private var quoteType = -1

    /** 画笔-线 */
    private var candlePaint = Paint().apply {
        isAntiAlias = true
        color = colorRed
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = dp2px(0.5f)
    }

    /** 滚动至数据尽头 */
    var onScrollEndListener: OnScrollEndListener? = null

    /** 集金策略 */
    private var jjxSet = linkedSetOf<QuoteSignEntry>()
    /** 趋势先锋 */
    private var turtlesSet = linkedSetOf<QuoteSignEntry>()
    /** 盯盘神器 */
    private var trendMap: MutableMap<Long, List<QuoteSymbolTrendBean>> = mutableMapOf()

    /** 已绘制的点（包含点击区域信息）  */
    private val mTrendInfos: MutableList<SymbolTrendInfo> = mutableListOf()
    /** 当前展示盯盘信息  */
    private var clickSymbolTrendInfo: SymbolTrendInfo? = null
    /** 盯盘开通VIP点击区域  */
    private var trendVipRect: RectF = RectF()

    /** 盯盘相关颜色  */
    private val trendBgColor: Int = getColorById(mContext, R.color.color_e6fff6f2_e63f3c37)
    private val trendTextColor: Int = getColorById(mContext, R.color.color_ff6a28_e79300)
    private val trendCircleColor: Int = getColorById(mContext, R.color.color_ff6a28_ffb128)

    /** 交易轨迹相关 */
    //轨迹开关
    var traceEnable = false
    //成本线开关
    var costLineEnable = false
    //买多成本线
    var buy = 0.0
    //卖空成本线
    var sell = 0.0
    //开仓点
    private val mTraceMapOpen = LinkedHashMap<Long, List<TradeTraceBean>>()
    //平仓点
    private val mTraceMapClose = LinkedHashMap<Long, List<TradeTraceBean>>()
    private val mSimulateInfoList: MutableList<SimulateInfo> = mutableListOf()
    private var clickSimulateInfo: SimulateInfo? = null
    //当前页面是否包含点击信息
    private var currentScreenContainsClickPoint = false
    //轨迹箭头
    private val arrowRedUp: Bitmap = getBitmapFromVectorDrawable(mContext, R.drawable.vector_up_red)
    private val arrowGreenDown: Bitmap = getBitmapFromVectorDrawable(mContext, R.drawable.vector_down_green)
    private val arrowBlueUp: Bitmap = getBitmapFromVectorDrawable(mContext, R.drawable.vector_up_blue)
    private val arrowBlueDown: Bitmap = getBitmapFromVectorDrawable(mContext, R.drawable.vector_down_blue)
    private val arrowRedUpFutures: Bitmap = BitmapFactory.decodeResource(resources, R.mipmap.trade_red_up_futures)
    private val arrowGreenDownFutures: Bitmap = BitmapFactory.decodeResource(resources, R.mipmap.trade_green_down_futures)

    init {
        visibleCount = 40
        emptyOffsetFlag = 1
    }

    fun setQuoteBean(quoteBean: QuoteBean){
        this.quoteBean = quoteBean
        mDec = quoteBean.decPointCount
        quoteType = getQuoteType()
    }

    /**
     * 绑定副图
     */
    fun bindAssist(assistChart: KAssistChart){
        assistChart.bindMainChart(this)
        assistChartList.add(assistChart)
    }

    /** 设置趋势先锋 */
    fun addJjxSet(set: Set<QuoteSignEntry>){
        jjxSet.addAll(set)
        jjxSet.sortedBy { it.time }
    }

    /** 设置集金策略 */
    fun addTurtlesSet(set: Set<QuoteSignEntry>){
        turtlesSet.addAll(set)
        turtlesSet.sortedBy { it.time }
    }

    /** 盯盘神器 */
    fun addTrendMap(map: MutableMap<Long, MutableList<QuoteSymbolTrendBean>>){
        trendMap.putAll(map)
        DataFormatHelper.calculateSymbolTrend(listFull, trendMap)
        postInvalidate()
    }

    /**
     * 设置交易轨迹
     */
    fun addTradeTraceList(list: List<TradeTraceBean>?){
        list?.let {
            formatTraceMap(it, true).let { openMap ->
                mTraceMapOpen.putAll(openMap)
            }
            formatTraceMap(it, false).let { closeMap ->
                mTraceMapClose.putAll(closeMap)
            }
            postInvalidate()
        }
    }

    /**
     * 格式化轨迹时间
     * @param list 轨迹
     * @param isOpen 是否开仓
     */
    private fun formatTraceMap(list: List<TradeTraceBean>, isOpen: Boolean): LinkedHashMap<Long, MutableList<TradeTraceBean>> {
        val traceMap = LinkedHashMap<Long, MutableList<TradeTraceBean>>()
        for (i in list.indices) {
            val traceBean = list[i]
            var formatTime: Long = 0
            if (isOpen) {
                if (traceBean.openTime != 0L) {
                    formatTime = getTraceMapTime(traceBean.openTime)
                }
            } else {
                if (traceBean.closeTime != 0L) {
                    formatTime = getTraceMapTime(traceBean.closeTime)
                }
            }
            if (formatTime != 0L) {
                if (traceMap.containsKey(formatTime)) {
                    traceMap[formatTime]!!.add(traceBean)
                } else {
                    val traceBeans: MutableList<TradeTraceBean> = ArrayList()
                    traceBeans.add(traceBean)
                    traceMap[formatTime] = traceBeans
                }
            }
        }
        return traceMap
    }

    private fun getTraceMapTime(traceTime: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = traceTime
        calendar[Calendar.MILLISECOND] = 0
        calendar[Calendar.SECOND] = 0
        val minute = calendar[Calendar.MINUTE]
        when (periodTag) {
            ChartConstant.PERIOD_1_MINUTE -> return calendar.timeInMillis
            ChartConstant.PERIOD_3_MINUTE -> calendar[Calendar.MINUTE] = minute / 3 * 3
            ChartConstant.PERIOD_5_MINUTE -> calendar[Calendar.MINUTE] = minute / 5 * 5
            ChartConstant.PERIOD_10_MINUTE -> calendar[Calendar.MINUTE] = minute / 10 * 10
            ChartConstant.PERIOD_15_MINUTE -> calendar[Calendar.MINUTE] = minute / 15 * 15
            ChartConstant.PERIOD_20_MINUTE -> calendar[Calendar.MINUTE] = minute / 20 * 20
            ChartConstant.PERIOD_30_MINUTE -> calendar[Calendar.MINUTE] = minute / 30 * 30
            ChartConstant.PERIOD_60_MINUTE -> calendar[Calendar.MINUTE] = 0
            ChartConstant.PERIOD_DAY -> {
                calendar[Calendar.MINUTE] = 0
                calendar[Calendar.HOUR_OF_DAY] = 0
            }
            else -> return 0
        }
        return calendar.timeInMillis
    }

    private var dataInited = false

    fun setKLineList(list: List<KLineData>?){
        if(list.isNullOrEmpty())
            return

        source.addAll(list)
        calculateK(source.take(10000).toMutableList(),0)
    }

    /**
     * 重新计算数据
     */
    fun calculateChart(){
        calculateK(sourceVisible,0)
    }

    /**
     * 新增数据
     * endFlag
     * -1：左侧数据
     * 0：首次数据
     * 1：右侧数据
     */
    fun addKLineList(list: List<KLineData>?, endFlag: Int = -1){
        if(list.isNullOrEmpty())
            return

        if(endFlag == -1 && list.first().time == listFull.first().time){
            return
        }

        if(endFlag == 1 && list.last().time == listFull.last().time){
            return
        }

        if(endFlag == -1){
            source.addAll(0, list)
            calculateK(source.take(10000).toMutableList(), -1)
        }else if(endFlag == 1){
            source.addAll(list)
            calculateK(source.takeLast(10000).toMutableList(), 1)
        }
    }

    private val newDataTemp = mutableListOf<KLineData>()

    /**
     * 添加新数据
     */
    private fun addKLineNewData(newData: KLineData, isLast: Boolean = false){
        source.lastOrNull()?.let {
            if(inSamePeriod(it.time, newData.time)){
                it.high = max(newData.close, it.high)
                it.low = min(newData.close, it.low)
                it.close = newData.close
                it.amount = newData.amount
                it.holding += newData.holding
                it.volume += newData.volume
            }else{
                addNewData(newData)
            }
        }?: addNewData(newData)

        if(isLast && !touched && (flingAnim.isPaused || !flingAnim.isRunning) && !showCross){
            source.addAll(newDataTemp)
            newDataTemp.clear()
            calculateK(source.takeLast(sourceVisible.size).toMutableList(), 2)
        }
    }

    /**
     * 临时存储新数据
     */
    private fun addNewData(newData: KLineData) {
        newDataTemp.lastOrNull()?.let {
            if(inSamePeriod(it.time, newData.time)){
                it.high = max(newData.close, it.high)
                it.low = min(newData.close, it.low)
                it.close = newData.close
                it.amount = newData.amount
                it.holding += newData.holding
                it.volume += newData.volume
            }else{
                newDataTemp.add(newData)
            }
        }?:let {
            newDataTemp.add(newData)
        }
    }

    /** 是否在同一时间区间 */
    private fun inSamePeriod(time1: Long, time2: Long): Boolean{
        return when(periodTag){
            ChartConstant.PERIOD_1_MINUTE -> isSameMin(time1, time2)
            ChartConstant.PERIOD_3_MINUTE -> isSameMin(time1, time2,3)
            ChartConstant.PERIOD_5_MINUTE -> isSameMin(time1, time2,5)
            ChartConstant.PERIOD_10_MINUTE -> isSameMin(time1, time2,10)
            ChartConstant.PERIOD_15_MINUTE -> isSameMin(time1, time2,15)
            ChartConstant.PERIOD_20_MINUTE -> isSameMin(time1, time2,20)
            ChartConstant.PERIOD_30_MINUTE -> isSameMin(time1, time2,30)
            ChartConstant.PERIOD_60_MINUTE -> isSameMin(time1, time2,60)
            ChartConstant.PERIOD_2_HOUR -> isSameHour(time1, time2,2)
            ChartConstant.PERIOD_3_HOUR -> isSameHour(time1, time2,3)
            ChartConstant.PERIOD_4_HOUR -> isSameHour(time1, time2,4)
            ChartConstant.PERIOD_6_HOUR -> isSameHour(time1, time2,6)
            ChartConstant.PERIOD_8_HOUR -> isSameHour(time1, time2,8)
            ChartConstant.PERIOD_12_HOUR -> isSameHour(time1, time2,12)
            ChartConstant.PERIOD_DAY -> isSameDay(time1, time2)
            ChartConstant.PERIOD_WEEK -> isSameWeek(time1, time2)
            ChartConstant.PERIOD_MONTH -> isSameMonth(time1, time2)
            ChartConstant.PERIOD_SEASON -> isSameSeason(time1, time2)
            ChartConstant.PERIOD_YEAR -> isSameYear(time1, time2)
            else -> false
        }
    }

    private fun refreshList(list: List<KLineData>, endFlag: Int = 0){
        val preVisibleSize = listVisible.size

        visibleLastIndex = if(endFlag == 0){
            list.size
        }else if(endFlag == 2){
            if(preVisibleSize == visibleCount){
                list.size
            }else{
                list.size
            }
        }else{
            if(visibleLastIndex >= listFull.size){
                visibleLastIndex = listFull.size - 1
            }
            val visibleLastTime = listFull[visibleLastIndex].time
            val lastData = list.minByOrNull { abs(it.time - visibleLastTime) }
            list.indexOf(lastData)
        }

        listFull.clear()
        listFull.addAll(list)
        this.listVisible.clear()

        if(visibleLastIndex > listFull.size){
            visibleLastIndex = listFull.size
        }

        when {
            endFlag == 2 -> this.listVisible.addAll(listFull.takeLast(preVisibleSize))
            listFull.size > visibleCount -> {
                val fromIndex = if(visibleLastIndex >= visibleCount){
                    visibleLastIndex - visibleCount
                }else {
                    0
                }
                this.listVisible.addAll(listFull.subList(fromIndex, visibleLastIndex))
            }
            else -> this.listVisible.addAll(listFull)
        }

        curFirst = null
        curLast = null

        if(this.listVisible.isNullOrEmpty()){
            postInvalidate()
            return
        }

        post {
            formatMaxAndMin()
            postInvalidate()
        }

        dataInited = true
    }

    override fun drawData(canvas: Canvas) {
        if(visibleCount > 290){
            drawLine(canvas)
        }else{
            drawCandle(canvas)
            drawMainParams(canvas)
        }
    }

    override fun drawYLeftValue(canvas: Canvas) {
        super.drawYLeftValue(canvas)
        drawTradeCostLine(canvas)
    }

    /**
     * 当前首条数据
     */
    private var curFirst: KLineData? = null
    private var curLast: KLineData? = null

    override fun changeLastVisible(scaleMode: Boolean) {
        super.changeLastVisible(scaleMode)

        if(listFull.isNotEmpty()){
            //比较第一条
            val first = listFull.first()
            val last = listFull.last()

//            if(moveToLeft && listFull.indexOf(listVisible.first()) < visiable_count*2 && startTime != first.time){
//                startTime = first.time
//                onScrollEndListener?.onScrollLeftEnd(first)
//            }
//            else if(!moveToLeft && listFull.size - listFull.indexOf(listVisible.last()) < visiable_count*2 && endTime != last.time){
//                endTime = last.time
//                onScrollEndListener?.onScrollRightEnd(last)
//            }


            if(listVisible.first() == first && curFirst != first){
                //到达最左侧数据
                flingAnim.cancel()
                if(sourceVisible.first() == source.first()){
                    //从接口获取新数据
                    onScrollEndListener?.onScrollLeftEnd(source.first())
                }else{
                    //从source获取数据
                    val list = mutableListOf<KLineData>()
                    list.addAll(source.subList(0,source.indexOf(sourceVisible.first())).takeLast(5000))
                    list.addAll(sourceVisible.take(5000))
                    calculateK(list,-1)
                }
            }
            else if(!moveToLeft && listVisible.last() == last && curLast != last){
                if(visibleLastIndex >= listFull.size - 1 + visibleCount/3){
                    //留 1/3 空白
                    //到达最右侧数据
                    flingAnim.cancel()
                    if(sourceVisible.last() == source.last()){
                        //从接口获取新数据
                        onScrollEndListener?.onScrollRightEnd(source.last())
                    }else{
                        //从source获取数据
                        val list = mutableListOf<KLineData>()
                        list.addAll(sourceVisible.takeLast(5000))
                        list.addAll(source.subList(source.indexOf(sourceVisible.last()) + 1,source.size).take(5000))
                        calculateK(list, 1)
                    }
                }
            }
            curFirst = listVisible.first()
            curLast = listVisible.last()
        }
    }

    override fun postInvalidate() {
        formatChartX()
        super.postInvalidate()
        assistChartList.forEach {
            it.refreshView()
        }
    }

    override fun invalidate() {
        formatChartX()
        super.invalidate()
        assistChartList.forEach {
            it.refreshView()
        }
    }

    private fun formatChartX() {
        listVisible.forEachIndexed { index, kLine ->
            kLine.apply {
                chartX = getValueX(index)
            }
        }
    }

    private fun calculateK(list: MutableList<KLineData>, endFlag: Int) {
        ThreadUtils.executeByCached(object : ThreadUtils.Task<List<KLineData>>() {
            override fun doInBackground(): List<KLineData> {
                sourceVisible = list
                val listMerge = QuoteUtils.mergeKHisData(sourceVisible, periodTag)
                DataFormatHelper.calculateK(listMerge)
                DataFormatHelper.calculateSymbolTrend(listMerge, trendMap)
                return listMerge
            }

            override fun onSuccess(result: List<KLineData>) {
                refreshList(result, endFlag)
                if(endFlag == 0 && delayOpen){
                    openGoldenCut(true)
                }
            }

            override fun onCancel() {

            }

            override fun onFail(t: Throwable?) {

            }
        })
    }

    override fun drawStaticData(canvas: Canvas) {
        super.drawStaticData(canvas)
        drawClickSymbolTrendInfo(canvas)
    }

    override fun onLongPress(ev: MotionEvent) {
        if(clickSymbolTrendInfo == null){
            super.onLongPress(ev)
        }
    }

    override fun onSingleTapUp(ev: MotionEvent): Boolean {
        if(!showCross){
            // 获取点中区域(盯盘神器)
            val clickTrend = mTrendInfos.lastOrNull { it.rect.contains(ev.x - marginLeft, ev.y - marginTop) }
            if(clickTrend != null){
                if(canvasBitmap == null)
                    canvasBitmap = getBackgroundBitmap()
                clickSymbolTrendInfo = clickTrend
                postInvalidate()
                return true
            }else if(trendVipRect.contains(ev.x - marginLeft, ev.y - marginTop)){
                canvasBitmap = null
                clickSymbolTrendInfo = null
                trendVipRect.set(0f, 0f, 0f, 0f)
                postInvalidate()
                if (!UserManager.hasPrivilege(VipRightEnum.VipSymbolTrend.code)) {
                    QuoteStatUtil.onReplayVip()
                    JumpUtils.openWebView(Url.Html.VIP_URL, false)
                }
                return true
            }else if(canvasBitmap != null || clickSymbolTrendInfo != null){
                canvasBitmap = null
                clickSymbolTrendInfo = null
                trendVipRect.set(0f, 0f, 0f, 0f)
                postInvalidate()
                return true
            }

            val lastTradeTrace = mSimulateInfoList.lastOrNull { it.rectF.contains(ev.x - marginLeft, ev.y - marginTop) }

            if(lastTradeTrace != null){
                clickSimulateInfo = lastTradeTrace
                postInvalidate()
                return true
            }

            if(clickSimulateInfo != null){
                clickSimulateInfo = null
                postInvalidate()
                return true
            }

            switchMain()
        }
        return super.onSingleTapUp(ev)
    }

    /**
     * 切换主图指标
     */
    @Suppress("UNCHECKED_CAST")
    private fun switchMain(){
        //内部处理
//        if(touchChart is KAssistChart){
//            (touchChart as KAssistChart).switchAssist()
//        }else{
//            var index = typeList.indexOf(mainType)
//            if(index >= typeList.size - 1)
//                index = -1
//            mainType = typeList[index + 1]
//        }
//        postInvalidate()

        //外部处理
        if(touchChart is KAssistChart){
            (touchChart as KAssistChart).chartClick()
        }else{
            mSingleClick?.onChartSingleClick(this as BaseChart<Any>)
        }
    }

    @JvmName("setMainType1")
    fun setMainType(mainType: String){
        this.mainType = mainType
        formatMaxAndMin()
    }

    /**
     * 主图指标
     */
    private fun drawMainParams(canvas: Canvas) {
        when(mainType){
            MAIN_TYPE_MA -> drawMainMA(canvas)
            MAIN_TYPE_BOLL -> drawMainBOLL(canvas)
            MAIN_TYPE_MABOLL -> {
                drawMainMA(canvas)
                drawMainBOLL(canvas)
            }
            MAIN_TYPE_SAR -> drawMainSAR(canvas)
            MAIN_TYPE_EXPMA -> drawMainEXPMA(canvas)
            MAIN_TYPE_QSXF -> drawMainQSXFAndJJCL(canvas, turtlesSet)
            MAIN_TYPE_JJCL -> drawMainQSXFAndJJCL(canvas, jjxSet)
        }
    }

    /** 趋势先锋、集金策略 箭头高度 */
    private val arrowHeight = dp2px(10f)

    /**
     * 主图指标趋势先锋
     */
    private fun drawMainQSXFAndJJCL(canvas: Canvas, dataSet: LinkedHashSet<QuoteSignEntry>) {
        var textSize = getDataStepSize()

        sp2px(9f).run {
            if(textSize < this)
                textSize = this
        }

        textSizeDef.run {
            if(textSize > this)
                textSize = this
        }

        linePaint.style = Paint.Style.FILL
        textPaint.textSize = textSize

        listVisible.forEach { kLine ->
            dataSet.firstOrNull{ it.time == kLine.time }?.value?.single?.let { single ->
                single.forEachIndexed { index, data ->
                    data.getTypeStr()?.let { text ->
                        if(index == 0){
                            drawMainQSXFAndJJCL(canvas, data.isUp(), text, kLine.chartX, kLine.high, kLine.low)
                        }else{
                            drawMainQSXFAndJJCL(canvas, !single.first().isUp(), text, kLine.chartX, kLine.high, kLine.low, data.isUp())
                        }
                    }
                }
            }
        }
    }

    private fun drawMainQSXFAndJJCL(canvas: Canvas, isUp: Boolean, text: String, chartX: Float, high: Double, low: Double, red: Boolean = isUp) {

        Rect().apply {
            textPaint.getTextBounds(text, 0, text.length, this)
            linePaint.style = Paint.Style.FILL

            if(red){
                linePaint.color = colorRed
                textPaint.color = colorRed
            }else{
                linePaint.color = colorGreen
                textPaint.color = colorGreen
            }

            linePaint.alpha = 50

            if(isUp){
                val y = getValueY(low) + arrowHeight/10

                Path().apply {
                    (arrowHeight/6).let { offset ->
                        moveTo(chartX, y)
                        lineTo(chartX - offset, y + offset)
                        lineTo(chartX + offset, y + offset)
                        close()
                    }
                    canvas.drawPath(this, linePaint)
                }
                (y + arrowHeight).let {
                    canvas.drawLine(chartX, y, chartX, it, linePaint)
                    val outRectF = RectF(
                        chartX - width()/2f - height()/2.5f,
                        it,
                        chartX + width() / 2f + height()/2,
                        it + height()*4/3.2f)
                    val radius = outRectF.height()/2
                    canvas.drawRoundRect(outRectF, radius, radius, linePaint)
                    linePaint.style = Paint.Style.STROKE
                    linePaint.alpha = 255
                    canvas.drawRoundRect(outRectF, radius, radius, linePaint)
                    canvas.drawText(text, chartX - width()/2f, it + height(), textPaint)
                }

            }else{
                val y = getValueY(high) - arrowHeight/10

                Path().apply {
                    (arrowHeight/6).let { offset ->
                        moveTo(chartX, y)
                        lineTo(chartX - offset, y - offset)
                        lineTo(chartX + offset, y - offset)
                        close()
                    }
                    canvas.drawPath(this, linePaint)
                }
                (y - arrowHeight).let {
                    canvas.drawLine(chartX, y, chartX, it, linePaint)
                    val outRectF = RectF(
                        chartX - width()/2f - height()/2.5f,
                        it - height()*4/3,
                        chartX + width()/2f + height()/2,
                        it)
                    val radius = outRectF.height()/2
                    canvas.drawRoundRect(outRectF, radius, radius, linePaint)
                    linePaint.style = Paint.Style.STROKE
                    linePaint.alpha = 255
                    canvas.drawRoundRect(outRectF, radius, radius, linePaint)
                    canvas.drawText(text, chartX - width()/2f, it - height()/3, textPaint)
                }
            }
        }
    }

    /**
     * 主图指标EXPMA
     */
    private fun drawMainEXPMA(canvas: Canvas) {
        val pathN1 = Path()
        val pathN2 = Path()

        listVisible.forEach { kLine ->
            kLine.expma.run {
                if(!n1.isNaN()){
                    if(pathN1.isEmpty){
                        pathN1.moveTo(kLine.chartX, getValueY(n1))
                    }else{
                        pathN1.lineTo(kLine.chartX, getValueY(n1))
                    }
                }
                if(!n2.isNaN()){
                    if(pathN2.isEmpty){
                        pathN2.moveTo(kLine.chartX, getValueY(n2))
                    }else{
                        pathN2.lineTo(kLine.chartX, getValueY(n2))
                    }
                }
            }

        }

        linePaint.style = Paint.Style.STROKE
        linePaint.color = getColor(mContext, R.color.expma_n1)
        canvas.drawPath(pathN1, linePaint)
        linePaint.color = getColor(mContext, R.color.expma_n2)
        canvas.drawPath(pathN2, linePaint)
    }


    /**
     * 主图指标SAR
     */
    private fun drawMainSAR(canvas: Canvas) {
        val sarRadius: Float = 0.25f * getDataStepSize()
        linePaint.style = Paint.Style.STROKE
        linePaint.color = colorBlue
        listVisible.forEach { kLine ->
            kLine.sar.apply {
                linePaint.color = if(isSarUp) colorRed else colorGreen
                canvas.drawCircle(kLine.chartX, getValueY(mid), sarRadius, linePaint)
            }
        }
    }

    /**
     * 主图指标BOLL
     */
    private fun drawMainBOLL(canvas: Canvas) {
        val pathBollUp = Path()
        val pathBollMd = Path()
        val pathBollDn = Path()

        listVisible.forEach { kLine ->
            kLine.boll.let { boll ->
                if(!boll.up.isNaN()){
                    if(pathBollUp.isEmpty){
                        if(boll.up != 0.0){
                            pathBollUp.moveTo(kLine.chartX, getValueY(boll.up))
                        }
                    }else{
                        pathBollUp.lineTo(kLine.chartX, getValueY(boll.up))
                    }
                }

                if(!boll.mb.isNaN()){
                    if(pathBollMd.isEmpty){
                        if(boll.mb != 0.0){
                            pathBollMd.moveTo(kLine.chartX, getValueY(boll.mb))
                        }
                    }else{
                        pathBollMd.lineTo(kLine.chartX, getValueY(boll.mb))
                    }
                }

                if(!boll.dn.isNaN()){
                    if(pathBollDn.isEmpty){
                        if(boll.dn != 0.0){
                            pathBollDn.moveTo(kLine.chartX, getValueY(boll.dn))
                        }
                    }else{
                        pathBollDn.lineTo(kLine.chartX, getValueY(boll.dn))
                    }
                }
            }

        }

        linePaint.style = Paint.Style.STROKE
        linePaint.color = getColor(mContext, R.color.boll_up)
        canvas.drawPath(pathBollUp, linePaint)
        linePaint.color = getColor(mContext, R.color.boll_md)
        canvas.drawPath(pathBollMd, linePaint)
        linePaint.color = getColor(mContext, R.color.boll_dn)
        canvas.drawPath(pathBollDn, linePaint)
    }

    /**
     * 主图指标MA
     */
    private fun drawMainMA(canvas: Canvas) {
        val pathMA1 = Path()
        val pathMA2 = Path()
        val pathMA3 = Path()
        val pathMA4 = Path()
        val pathMA5 = Path()
        listVisible.forEach { kLine ->
            kLine.ma.let { ma ->
                if(!ma.ma1.isNaN()){
                    if(pathMA1.isEmpty){
                        pathMA1.moveTo(kLine.chartX, getValueY(ma.ma1))
                    }else{
                        pathMA1.lineTo(kLine.chartX, getValueY(ma.ma1))
                    }
                }

                if(!ma.ma2.isNaN()){
                    if(pathMA2.isEmpty){
                        pathMA2.moveTo(kLine.chartX, getValueY(ma.ma2))
                    }else{
                        pathMA2.lineTo(kLine.chartX, getValueY(ma.ma2))
                    }
                }

                if(!ma.ma3.isNaN()){
                    if(pathMA3.isEmpty){
                        pathMA3.moveTo(kLine.chartX, getValueY(ma.ma3))
                    }else{
                        pathMA3.lineTo(kLine.chartX, getValueY(ma.ma3))
                    }
                }

                if(!ma.ma4.isNaN()){
                    if(pathMA4.isEmpty){
                        pathMA4.moveTo(kLine.chartX, getValueY(ma.ma4))
                    }else{
                        pathMA4.lineTo(kLine.chartX, getValueY(ma.ma4))
                    }
                }

                if(!ma.ma5.isNaN()){
                    if(pathMA5.isEmpty){
                        pathMA5.moveTo(kLine.chartX, getValueY(ma.ma5))
                    }else{
                        pathMA5.lineTo(kLine.chartX, getValueY(ma.ma5))
                    }
                }
            }
        }
        linePaint.style = Paint.Style.STROKE
        linePaint.color = getColor(mContext, R.color.ma1)
        canvas.drawPath(pathMA1, linePaint)
        linePaint.color = getColor(mContext, R.color.ma2)
        canvas.drawPath(pathMA2, linePaint)
        linePaint.color = getColor(mContext, R.color.ma3)
        canvas.drawPath(pathMA3, linePaint)
        linePaint.color = getColor(mContext, R.color.ma4)
        canvas.drawPath(pathMA4, linePaint)
        linePaint.color = getColor(mContext, R.color.ma5)
        canvas.drawPath(pathMA5, linePaint)
    }

    /**
     * 最大值最小值
     */
    private fun drawMaxAndMin(canvas: Canvas, index: Int, isMax: Boolean) {
        linePaint.color = colorBlue
        linePaint.style = Paint.Style.FILL
        textPaint.color = textColor
        textPaint.textSize = textSizeDef
        if(index > -1){
            //有最值
            listVisible[index].apply {
                val price = if(isMax) high else low
                val x = getValueX(index)
                val y = getValueY(price)
                var stopX: Float
                formatDouble(price, mDec).let {
                    val bound = Rect()
                    textPaint.getTextBounds(it, 0, it.length, bound)
                    if(x < chartWidth/2){
                        stopX = x + dp2px(10f)
                        canvas.drawText(it, stopX + dp2px(4f), y + bound.height() / 2, textPaint)
                    }else{
                        stopX = x - dp2px(10f)
                        canvas.drawText(it, stopX - bound.width() - dp2px(4f), y + bound.height() / 2, textPaint)
                    }
                }
                canvas.drawLine(x, y, stopX, y, linePaint)
                canvas.drawCircle(stopX, y, dp2px(1.5f), linePaint)
            }
        }
    }

    /**
     * 蜡烛图
     */
    private fun drawCandle(canvas: Canvas) {
        //蜡烛图宽度
        val candleHalfWidth: Float = 0.35f*getDataStepSize()
        candleWidth = candleHalfWidth*2
        var minIndex = -1
        var maxIndex = -1
        mTrendInfos.clear()
        mSimulateInfoList.clear()

        listVisible.forEachIndexed { index, kLine ->
            kLine.run {
                if(isIncreasing()){
                    candlePaint.color = colorRed
                    candlePaint.style = Paint.Style.STROKE
                }else{
                    candlePaint.color = colorGreen
                    candlePaint.style = Paint.Style.FILL
                }
                if(index == 0){
                    minIndex = 0
                    maxIndex = 0
                }else{
                    if(high > listVisible[maxIndex].high){
                        maxIndex = index
                    }
                    if(low < listVisible[minIndex].low){
                        minIndex = index
                    }
                }
                if(chartX <= 0f){
                    chartX = getValueX(index)
                }
                val highY = getValueY(high)
                val lowY = getValueY(low)
                val openY = getValueY(open)
                val closeY = getValueY(close)
                //上直线
                canvas.drawLine(chartX, highY, chartX, min(openY, closeY), candlePaint)
                //长方形
                canvas.drawRect(chartX - candleHalfWidth, openY, chartX + candleHalfWidth, closeY, candlePaint)
                //下直线
                canvas.drawLine(chartX, lowY, chartX, max(openY, closeY), candlePaint)

                addSymbolTrend(kLine)

                //交易轨迹
                if(traceInfo.openList.isNullOrEmpty()){
                    traceInfo.openList = mTraceMapOpen[time]
                }
                if(traceInfo.closeList.isNullOrEmpty()){
                    traceInfo.closeList = mTraceMapClose[time]
                }
                drawTradePath(canvas, kLine)
            }
        }

        drawTradePathLine(canvas)

        drawMaxAndMin(canvas, minIndex, false)
        drawMaxAndMin(canvas, maxIndex, true)
    }

    /**
     * 达到最大显示量时
     * 路径 path
     */
    private fun drawLine(canvas: Canvas) {
        val path = Path()
        mTrendInfos.clear()

        listVisible.forEachIndexed { index, kLine ->
            kLine.apply {
//                chartX = getValueX(index)
                if(path.isEmpty){
                    path.moveTo(chartX, getValueY(high))
                }else{
                    path.lineTo(chartX, getValueY(high))
                }

                addSymbolTrend(kLine)
            }
        }
        linePaint.style = Paint.Style.STROKE
        linePaint.color = colorBlue
        canvas.drawPath(path, linePaint)
    }

    override fun drawXValue(canvas: Canvas) {
        drawSymbolTrend(canvas)
        drawTradePathText(canvas)
        super.drawXValue(canvas)
    }

    override fun requestParentDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        assistChartList.forEach {
            it.requestParentDisallowInterceptTouchEvent(disallowIntercept)
        }
        super.requestParentDisallowInterceptTouchEvent(disallowIntercept)
    }

    /**
     * 交易轨迹价格浮层
     */
    private fun drawTradePathText(canvas: Canvas) {
        if (candleWidth < dp2px(2f) || !currentScreenContainsClickPoint)
            return

        clickSimulateInfo?.let { info ->
            if(mSimulateInfoList.any { it.id == info.id && it.open == info.open }){
                val text: String
                if (!info.open) {
                    text = "平" + formatDouble(info.price)
                    linePaint.color = resources.getColor(R.color.equal_color)
                } else if (info.orderType == 0) {
                    text = "多" + formatDouble(info.price)
                    linePaint.color = resources.getColor(R.color.uc_increase)
                } else {
                    text = "空" + formatDouble(info.price)
                    linePaint.color = resources.getColor(R.color.uc_decrease)
                }
                linePaint.style = Paint.Style.FILL
                val rect = Rect()
                textPaint.color = Color.WHITE
                textPaint.getTextBounds(text, 0, text.length, rect)
                val x = info.x - rect.width() / 2f
                val y: Float = info.rectF.top - rect.height() / 2f
                val rectF = RectF(x - rect.height() / 5f, y - rect.height() - rect.height() / 8f, x + rect.width() + rect.height() / 5f, y + rect.height() / 3f)
                canvas.drawRect(rectF, linePaint)
                canvas.drawText(text, x, y, textPaint)
            }
        }
    }

    /**
     * 持仓成本线
     */
    private fun drawTradeCostLine(canvas: Canvas) {
        if(QuoteDao.getTradeCostStatus()){
            linePaint.pathEffect = dashPathEffect
            val margin = dp2px(1f)
            var buyY = 0f
            var sellY = 0f
            if (buy != 0.0) {
                linePaint.color = colorRed
                buyY = getValueY(buy)
                linePaint.style = Paint.Style.STROKE
                canvas.drawLine(0f, buyY, chartWidth, buyY, linePaint)
            }
            if (sell != 0.0) {
                linePaint.color = colorGreen
                sellY = getValueY(sell)
                linePaint.style = Paint.Style.STROKE
                canvas.drawLine(0f, sellY, chartWidth, sellY, linePaint)
            }
            linePaint.pathEffect = null

            //价格
            if (buy != 0.0 || sell != 0.0) {
                linePaint.style = Paint.Style.FILL
                textPaint.color = Color.WHITE
                if (buy != 0.0 && sell != 0.0) {
                    val bound = Rect()
                    val buyStr = "多$buy"
                    textPaint.getTextBounds(buyStr, 0, buyStr.length, bound)
                    val gapHeight = Math.abs(buyY - sellY)
                    val rectHeight = bound.height() + margin * 2
                    if (gapHeight < rectHeight) {
                        val offset = (rectHeight - gapHeight) / 2
                        if (sellY > buyY) {
                            sellY += offset
                            buyY -= offset
                        } else {
                            sellY -= offset
                            buyY += offset
                        }
                    }
                }
                if (buy != 0.0) {
                    linePaint.color = colorRed
                    val bound = Rect()
                    val buyStr = "多$buy"
                    textPaint.getTextBounds(buyStr, 0, buyStr.length, bound)
                    canvas.drawRect(RectF(0f, buyY - bound.height() / 2f - margin, bound.width() + 2 * margin, buyY + bound.height() / 2f + margin), linePaint)
                    canvas.drawText(buyStr, margin, buyY + bound.height() / 2.5f, textPaint)
                }
                if (sell != 0.0) {
                    linePaint.color = colorGreen
                    val bound = Rect()
                    val sellStr = "空$sell"
                    textPaint.getTextBounds(sellStr, 0, sellStr.length, bound)
                    canvas.drawRect(RectF(0f, sellY - bound.height() / 2f - margin, bound.width() + 2 * margin, sellY + bound.height() / 2f + margin), linePaint)
                    canvas.drawText(sellStr, margin, sellY + bound.height() / 2.5f, textPaint)
                }
            }
        }
    }

    /**
     * 买卖轨迹连线
     */
    private fun drawTradePathLine(canvas: Canvas) {
        val tradeTracePaths: List<TracePathInfo> = getTradeTracePaths(mSimulateInfoList)
        linePaint.pathEffect = dashPathEffect
        for (i in tradeTracePaths.indices) {
            val info: TracePathInfo = tradeTracePaths[i]
            linePaint.color = info.color
            canvas.drawLine(info.startX, info.startY, info.endX, info.endY, linePaint)
        }
        linePaint.pathEffect = null
    }

    /**
     * 获取两个箭头坐标
     */
    private fun getTradeTracePaths(list: List<SimulateInfo>): List<TracePathInfo>{
        val pathInfoList: MutableList<TracePathInfo> = mutableListOf()
        list.filter { !it.open }.forEachIndexed { _, closeInfo ->
            list.firstOrNull { it.open && it.id == closeInfo.id }?.let { openInfo ->
                val color: Int = if(openInfo.orderType == 0){
                    //买多
                    getSpecialTxtColor(context, closeInfo.price - openInfo.price)
                }else{
                    //卖空
                    getSpecialTxtColor(context, openInfo.price - closeInfo.price)
                }
                pathInfoList.add(TracePathInfo(openInfo.x, openInfo.y, closeInfo.x, closeInfo.y, color))
            }
        }
        return pathInfoList
    }

    /**
     * 交易轨迹
     */
    private fun drawTradePath(canvas: Canvas, bean: KLineData) {
        if (quoteType == 0 || mainType == MAIN_TYPE_QSXF || mainType == MAIN_TYPE_JJCL || !couldDrawTradeTrace()) {
            return
        }else if (quoteType == Constant.Simulate.TYPE_OUTER && !QuoteDao.getTradePathStatus()) {
            return
        } else if ((quoteType == Constant.Simulate.TYPE_TD || quoteType == Constant.Simulate.TYPE_FUTURES) && !QuoteDao.getTradeDealStatus()) {
            return
        }

        val x = bean.chartX

        val arrowWidth = if (candleWidth > dp2px(20f)) {
            dp2px(20f)
        } else {
            candleWidth
        }

        //测试
//        if(openList == null)
//            openList = mutableListOf()
//        val bean1 = TradeTraceBean()
//        bean1.id = bean.time
//        bean1.openPrc = bean.low
//        bean1.closePrc = bean.high
//        bean1.orderType = 0
//        openList.add(bean1)

        //开仓
        bean.traceInfo.openList?.toMutableList()?.let { openList ->
            openList.forEach { tradeTrace ->
                val bitmap: Bitmap
                val rectF: RectF
                val rect: Rect
                val y: Float
                if (quoteType == Constant.Simulate.TYPE_OUTER) {

                    y = getValueY(tradeTrace.openPrc)

                    val simulateInfo = SimulateInfo()
                    if (tradeTrace.orderType == 0) {
                        bitmap = arrowRedUp
                        rectF = RectF(x - arrowWidth / 2, y, x + arrowWidth / 2, y + arrowWidth * bitmap.height / bitmap.width)
                        simulateInfo.y = rectF.top
                    } else {
                        bitmap = arrowGreenDown
                        rectF = RectF(x - arrowWidth / 2, y - arrowWidth * bitmap.height / bitmap.width, x + arrowWidth / 2, y)
                        simulateInfo.y = rectF.bottom
                    }

                    rect = Rect(0, 0, bitmap.width, bitmap.height)
                    simulateInfo.id = tradeTrace.id
                    simulateInfo.open = true
                    simulateInfo.x = x
                    simulateInfo.price = tradeTrace.openPrc
                    simulateInfo.orderType = tradeTrace.orderType
                    simulateInfo.rectF = rectF
                    //                mOpenInfo.put(tradeTrace.getId(),simulateInfo);
                    mSimulateInfoList.add(simulateInfo)

                    clickSimulateInfo?.let { info ->
                        if (info.open && info.id == tradeTrace.id) {
                            currentScreenContainsClickPoint = true
                            info.x = simulateInfo.x
                            info.y = simulateInfo.y
                            info.rectF = simulateInfo.rectF
                        }
                    }

                    if(candleWidth > dp2px(2f))
                        canvas.drawBitmap(bitmap, rect, rectF, linePaint)
                } else {
                    if (tradeTrace.orderType == 0) {
                        y = getValueY(bean.low) + dp2px(5f)
                        bitmap = arrowRedUpFutures
                        rectF = RectF(x - arrowWidth / 2, y, x + arrowWidth / 2, y + arrowWidth * bitmap.height / bitmap.width)
                    } else {
                        y = getValueY(bean.high) - dp2px(5f)
                        bitmap = arrowGreenDownFutures
                        rectF = RectF(x - arrowWidth / 2, y - arrowWidth * bitmap.height / bitmap.width, x + arrowWidth / 2, y)
                    }
                    rect = Rect(0, 0, bitmap.width, bitmap.height)
                    if(candleWidth > dp2px(2f))
                        canvas.drawBitmap(bitmap, rect, rectF, linePaint)
                }

            }
        }



        //        if(closeList == null)
//            closeList = mutableListOf()
//        closeList.add(bean1)

        //平仓
        bean.traceInfo.closeList?.toMutableList()?.let { closeList ->
            closeList.forEachIndexed { index, tradeTrace ->
                val bitmap: Bitmap
                val rectF: RectF
                val rect: Rect
                val y: Float
                if (quoteType == Constant.Simulate.TYPE_OUTER) {
                    y = getValueY(tradeTrace.closePrc)
                    val simulateInfo = SimulateInfo()
                    if (tradeTrace.orderType == 0) {
                        bitmap = arrowBlueDown
                        rectF = RectF(x - arrowWidth / 2, y - arrowWidth * bitmap.height / bitmap.width, x + arrowWidth / 2, y)
                        simulateInfo.y = rectF.bottom
                    } else {
                        bitmap = arrowBlueUp
                        rectF = RectF(x - arrowWidth / 2, y, x + arrowWidth / 2, y + arrowWidth * bitmap.height / bitmap.width)
                        simulateInfo.y = rectF.top
                    }
                    rect = Rect(0, 0, bitmap.width, bitmap.height)
                    simulateInfo.id = tradeTrace.id
                    simulateInfo.open = false
                    simulateInfo.x = x
                    simulateInfo.price = tradeTrace.closePrc
                    simulateInfo.orderType = tradeTrace.orderType
                    simulateInfo.rectF = rectF
                    //                mCloseInfo.put(tradeTrace.getId(),simulateInfo);
                    mSimulateInfoList.add(simulateInfo)

                    clickSimulateInfo?.let { info ->
                        if (!info.open && info.id == tradeTrace.id) {
                            currentScreenContainsClickPoint = true
                            info.x = simulateInfo.x
                            info.y = simulateInfo.y
                            info.rectF = simulateInfo.rectF
                        }
                    }
                } else {
                    if (tradeTrace.orderType == 0) {
                        y = getValueY(bean.high) - dp2px(5f)
                        bitmap = arrowGreenDownFutures
                        rectF = RectF(x - arrowWidth / 2, y - arrowWidth * bitmap.height / bitmap.width, x + arrowWidth / 2, y)
                    } else {
                        y = getValueY(bean.low) + dp2px(5f)
                        bitmap = arrowRedUpFutures
                        rectF = RectF(x - arrowWidth / 2, y, x + arrowWidth / 2, y + arrowWidth * bitmap.height / bitmap.width)
                    }
                    rect = Rect(0, 0, bitmap.width, bitmap.height)
                }
                if(candleWidth > dp2px(2f))
                    canvas.drawBitmap(bitmap, rect, rectF, linePaint)
            }
        }
    }

    /**
     * 是否绘制交易轨迹
     */
    private fun couldDrawTradeTrace(): Boolean {
        return periodTag == ChartConstant.PERIOD_1_MINUTE
                || periodTag == ChartConstant.PERIOD_3_MINUTE
                || periodTag == ChartConstant.PERIOD_5_MINUTE
                || periodTag == ChartConstant.PERIOD_10_MINUTE
                || periodTag == ChartConstant.PERIOD_15_MINUTE
                || periodTag == ChartConstant.PERIOD_20_MINUTE
                || periodTag == ChartConstant.PERIOD_30_MINUTE
                || periodTag == ChartConstant.PERIOD_60_MINUTE
                //                || periodTag == QuoteConstant.PERIOD_4_HOUR
                || periodTag == ChartConstant.PERIOD_DAY
    }

    /**
     * 获取模拟交易类型
     * @return
     * 0：无轨迹
     * 1：外盘
     * 2：TD
     * 3：期货
     */
    fun getQuoteType(): Int {
        return run {
            val boardId: Int = quoteBean.boardId
            when {
                QuoteUtils.isFutures(boardId) -> Constant.Simulate.TYPE_FUTURES
                boardId == BoardConstant.BOARD_TD -> Constant.Simulate.TYPE_TD
                else -> Constant.Simulate.TYPE_OUTER
            }
        }
    }

    /** 盯盘神器点半径 */
    private val trendRadius = dp2px(3f)

    /**
     * 添加盯盘神器信息
     */
    private fun addSymbolTrend(data: KLineData) {
        data.trendList.firstOrNull()?.let {
            mTrendInfos.add(SymbolTrendInfo(data.chartX, RectF(data.chartX - 4 * trendRadius, chartHeight - 4 * trendRadius, data.chartX + 4 * trendRadius, chartHeight + 4 * trendRadius), it))
        }
    }

    /**
     * 盯盘神器 点
     */
    private fun drawSymbolTrend(canvas: Canvas) {
        linePaint.style = Paint.Style.FILL
        linePaint.color = trendCircleColor
        mTrendInfos.forEachIndexed { index, symbolTrendInfo ->
            canvas.drawCircle(symbolTrendInfo.x, chartHeight, trendRadius, linePaint)
        }
    }

    /**
     * 需要展示的盯盘神器信息
     */
    private fun drawClickSymbolTrendInfo(canvas: Canvas) {
        clickSymbolTrendInfo?.let { trendInfo ->
            val f10 = dp2px(10f)
            val f12 = dp2px(12f)
            val trend: QuoteSymbolTrendBean = trendInfo.trend
            var bgWidth = (chartWidth * 0.45).toInt()
            val textWidth = (chartWidth * 0.45f - f10).roundToInt()
            val bgHeight: Int
            linePaint.pathEffect = dashPathEffect
            linePaint.color = trendCircleColor
            canvas.drawLine(trendInfo.x, chartHeight, trendInfo.x, chartHeight / 2f, linePaint)
            linePaint.pathEffect = null
            linePaint.style = Paint.Style.FILL
            trendVipRect.set(0f, 0f, 0f, 0f)
            val orangeLine = RectF()
            val radius: FloatArray
            val roundR: Float = f10 / 2
            val clipPath = Path()
            if(UserManager.hasPrivilege(VipRightEnum.VipSymbolTrend.getCode())){
                val bitmap = if (trend.descriptionType == 1) {
                    BitmapFactory.decodeResource(resources, R.mipmap.vip_zcyltcq)
                } else {
                    BitmapFactory.decodeResource(resources, R.mipmap.vip_zdfxb)
                }
                val src = Rect(0, 0, bitmap.width, bitmap.height)
                val dst = RectF(0f, 0f, f10 * bitmap.width / bitmap.height, f10)
                val textContent = StaticLayout(trend.eventContent, textPaint, textWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true)
                val textAnalysis = StaticLayout(trend.analysis, textPaint, textWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true)
                bgHeight = (dst.height() + textContent.height + textAnalysis.height + f10 * 1.5).roundToInt()
                if (trendInfo.x < chartWidth / 2f) {
                    //点在左侧，信息绘制右侧
                    radius = floatArrayOf(0f, 0f, roundR, roundR, roundR, roundR, 0f, 0f)
                    trendVipRect.set(trendInfo.x + f12, (chartHeight - bgHeight) / 2f, trendInfo.x + f12 + bgWidth, (chartHeight + bgHeight) / 2f)
                    orangeLine[trendVipRect.left, trendVipRect.top, trendVipRect.left - f10 / 4f] = trendVipRect.bottom
                    canvas.drawLine(trendInfo.x, chartHeight / 2f, trendInfo.x + f12, chartHeight / 2f, linePaint)
                } else {
                    //点在右侧，信息绘制左侧
                    radius = floatArrayOf(roundR, roundR, 0f, 0f, 0f, 0f, roundR, roundR)
                    trendVipRect.set(trendInfo.x - f12 - bgWidth, (chartHeight - bgHeight) / 2f, trendInfo.x - f12, (chartHeight + bgHeight) / 2f)
                    orangeLine[trendVipRect.right, trendVipRect.top, trendVipRect.right + f10 / 4f] = trendVipRect.bottom
                    canvas.drawLine(trendInfo.x, chartHeight / 2f, trendInfo.x - f12, chartHeight / 2f, linePaint)
                }
                canvas.drawRect(orangeLine, linePaint)
                linePaint.color = trendBgColor
                canvas.save()
                clipPath.addRoundRect(trendVipRect, radius, Path.Direction.CW)
                canvas.clipPath(clipPath)
                linePaint.maskFilter = BlurMaskFilter(f10 / 4, BlurMaskFilter.Blur.INNER)
                canvas.drawRect(trendVipRect, linePaint)
                linePaint.maskFilter = null
                canvas.restore()
                canvas.save()
                canvas.translate(trendVipRect.left + f10 / 2f, trendVipRect.top + f10 / 2f)
                textPaint.textAlign = Paint.Align.LEFT
                canvas.drawBitmap(bitmap, src, dst, null)
                canvas.translate(0f, dst.height() + f10 / 4)
                textPaint.color = textColor
                textContent.draw(canvas)
                canvas.translate(0f, textContent.height + f10 / 4)
                textPaint.color = trendTextColor
                textAnalysis.draw(canvas)
                canvas.restore()
            } else {
//                Bitmap bitmap = ResourceUtilsKt.getBitmapFromVectorDrawable(getContext(),R.mipmap.vip_trend_open);
//                Rect src = new Rect(0,0,bitmap.getWidth(), bitmap.getHeight());
//                setLayerType(LAYER_TYPE_SOFTWARE, null);
                val bound = Rect()
                val vipTitle = "【曲合大会员】专享盯盘神器"
                val vipOpen = "立即开通>"
                textPaint.isFakeBoldText = true
                textPaint.textSize = sp2px(10f)
                textPaint.getTextBounds(vipTitle, 0, vipTitle.length, bound)
                bgWidth = (bound.width() + f12 * 2).roundToInt()
                bgHeight = dp2px(60f).roundToInt()
                if (trendInfo.x < chartWidth / 2f) {
                    //点在左侧，信息绘制右侧
                    radius = floatArrayOf(0f, 0f, roundR, roundR, roundR, roundR, 0f, 0f)
                    trendVipRect.set(trendInfo.x + f12, (chartHeight - bgHeight) / 2f, trendInfo.x + f12 + bgWidth, (chartHeight + bgHeight) / 2f)
                    orangeLine[trendVipRect.left, trendVipRect.top, trendVipRect.left - f10 / 4f] = trendVipRect.bottom
                    canvas.drawLine(trendInfo.x, chartHeight / 2f, trendInfo.x + f12, chartHeight / 2f, linePaint)
                } else {
                    //点在右侧，信息绘制左侧
                    radius = floatArrayOf(roundR, roundR, 0f, 0f, 0f, 0f, roundR, roundR)
                    trendVipRect.set(trendInfo.x - f12 - bgWidth, (chartHeight - bgHeight) / 2f, trendInfo.x - f12, (chartHeight + bgHeight) / 2f)
                    orangeLine[trendVipRect.right, trendVipRect.top, trendVipRect.right + f10 / 4f] = trendVipRect.bottom
                    canvas.drawLine(trendInfo.x, chartHeight / 2f, trendInfo.x - f12, chartHeight / 2f, linePaint)
                }
                canvas.drawRect(orangeLine, linePaint)
                linePaint.color = trendBgColor
                canvas.save()
                clipPath.addRoundRect(trendVipRect, radius, Path.Direction.CW)
                canvas.clipPath(clipPath)
                linePaint.maskFilter = BlurMaskFilter(f10 / 4, BlurMaskFilter.Blur.INNER)
                canvas.drawRect(trendVipRect, linePaint)
                linePaint.maskFilter = null
                canvas.restore()
                textPaint.color = textColorDark
                var textX: Float = (trendVipRect.left + trendVipRect.right - bound.width() - f12) / 2f
                var textY: Float = (trendVipRect.top + trendVipRect.bottom) / 2f - bound.height() * 0.3f
                canvas.drawText(vipTitle, textX, textY, textPaint)
                textPaint.isFakeBoldText = false
                textPaint.textSize = sp2px(9f)
                textPaint.getTextBounds(vipOpen, 0, vipOpen.length, bound)
                textX = (trendVipRect.left + trendVipRect.right - bound.width()) / 2f
                textY = (trendVipRect.top + trendVipRect.bottom) / 2f + bound.height() * 1.3f
                textPaint.color = trendTextColor
                canvas.drawText(vipOpen, textX, textY, textPaint)
            }
        }
    }

    /**
     * 计算最大最小值
     */
    override fun formatMaxAndMin() {
        if(listVisible.isNullOrEmpty())
            return
        xValue.clear()


        val selectorMax: (KLineData) -> Double = {
            when(mainType){
                MAIN_TYPE_MA -> it.ma.maxValue
                MAIN_TYPE_BOLL -> it.boll.maxValue
                MAIN_TYPE_MABOLL -> it.boll.mabollMax
                MAIN_TYPE_EXPMA -> it.expma.maxValue
                else -> it.high
            }
        }

        val selectorMin: (KLineData) -> Double = {
            when(mainType){
                MAIN_TYPE_MA -> it.ma.minValue
                MAIN_TYPE_BOLL -> it.boll.minValue
                MAIN_TYPE_MABOLL -> it.boll.mabollMin
                MAIN_TYPE_EXPMA -> it.expma.minValue
                else -> it.low
            }
        }

        val maxEntity: KLineData? = listVisible.maxByOrNull(selectorMax)
        val minEntity: KLineData? = listVisible.minByOrNull(selectorMin)

        val firstData = listVisible.first()
        val lastData = listVisible.last()

        textPaint.textSize = textSizeDef

        firstData.getDate(periodTag).let {
            val bound = Rect()
            textPaint.getTextBounds(it, 0, it.length, bound)
            xValue.add(ChartXYValue(it, 0f, chartHeight + bound.height() + dp2px(2f), bound))
        }

        lastData.getDate(periodTag).let {
            val bound = Rect()
            textPaint.getTextBounds(it, 0, it.length, bound)
            xValue.add(ChartXYValue(it, chartWidth - bound.width(), chartHeight + bound.height() + dp2px(2f), bound))
        }

        if(minEntity != null && maxEntity != null){
            val max = maxEntity.let{
                when(mainType){
                    MAIN_TYPE_MA -> it.ma.maxValue
                    MAIN_TYPE_BOLL -> it.boll.maxValue
                    MAIN_TYPE_MABOLL -> it.boll.mabollMax
                    MAIN_TYPE_EXPMA -> it.expma.maxValue
                    else -> it.high
                }
            }

            val min = minEntity.let{
                when(mainType){
                    MAIN_TYPE_MA -> it.ma.minValue
                    MAIN_TYPE_BOLL -> it.boll.minValue
                    MAIN_TYPE_MABOLL -> it.boll.mabollMin
                    MAIN_TYPE_EXPMA -> it.expma.minValue
                    else -> it.low
                }
            }

            val offsetGap = (max - min) * 0.15
            maxValue = max
            minValue = min
            topValue = max + offsetGap
            bottomValue = min - offsetGap
            formatLeftValue()
        }

        assistChartList.forEach {
            it.formatMaxAndMin()
        }
    }

    /**
     * 十字光标
     */
    override fun drawCross(canvas: Canvas) {
        linePaint.color = colorCross
        textPaint.color = textColorDark
        textPaint.textSize = textSizeDef

        crossData = listVisible.minByOrNull { abs(crossX - it.chartX) }
        crossData?.let {
            val crossX = getValueX(listVisible.indexOf(it))
            onDataSelectListener?.onDataSelect(it, showCross, crossX + marginLeft)
            canvas.drawLine(crossX, 0f, crossX, chartHeight, linePaint)
            canvas.drawLine(0f, crossY, chartWidth, crossY, linePaint)
            it.high

            formatDouble(getValueByY(crossY), mDec).let { value ->
                val rect = Rect()
                textPaint.getTextBounds(value, 0, value.length, rect)
                if(crossX < chartWidth/2f){
                    //点在左侧，数值展示在右侧
                    val rectLeft = RectF(
                        chartWidth - rect.width() - dp2px(6f),
                        crossY - dp2px(2f) - rect.height() / 2,
                        chartWidth,
                        crossY + dp2px(2f) + rect.height() / 2)
                    drawDataRect(canvas, rectLeft)
                    canvas.drawText(value, chartWidth - rect.width() - dp2px(3f), crossY + rect.height() / 2, textPaint)
                }else{
                    //点在右侧，数值展示在左侧
                    val rectRight = RectF(0f,
                        crossY - dp2px(2f) - rect.height() / 2,
                        rect.width() + dp2px(6f),
                        crossY + dp2px(2f) + rect.height() / 2)
                    drawDataRect(canvas, rectRight)
                    canvas.drawText(value, dp2px(3f), crossY + rect.height() / 2, textPaint)
                }
                //底部数值
                it.getDate(periodTag).let { valueX ->
                    textPaint.getTextBounds(valueX, 0, valueX.length, rect)
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
                    canvas.drawText(valueX, bottomRectX + dp2px(3f), chartHeight - dp2px(2f), textPaint)
                }
            }

            /** 刷新副图 */
            assistChartList.forEach { chart ->
                chart.refreshView()
            }
        }
    }

    /**
     * 盯盘神器
     */
    private class SymbolTrendInfo(val x: Float, //信息点横坐标
                                  val rect: RectF,  //展示区域
                                  val trend: QuoteSymbolTrendBean)//盯盘神器信息

    override fun lastVisibleChanged() {

    }

    var delayOpen = false

    override fun openGoldenCut(open: Boolean) {
        if(open){
            if(listVisible.isNullOrEmpty()){
                delayOpen = true
                return
            }else{
                delayOpen = false
            }
            val maxEntity: KLineData? = listVisible.maxByOrNull { it.high }
            val minEntity: KLineData? = listVisible.minByOrNull { it.low }

            if(maxEntity != null && minEntity != null){
                val indexMax = listVisible.indexOf(maxEntity)
                val indexMin = listVisible.indexOf(minEntity)
                formatGoldenParams(maxEntity.high, minEntity.low, indexMin < indexMax)
            }
        }else{
            delayOpen = false
            clearGoldenParams()
        }
    }

    private var touched = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                touched = true
                removeKCallbacks()
            }
            MotionEvent.ACTION_UP -> {
                touched = false
                if(showCross && !QuoteDao.getCursorEnable()){
                    postDelayed(cross, 3000)
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private var cross = Runnable {
        if(!QuoteDao.getCursorEnable())
            hideCross()
    }

    private fun removeKCallbacks() {
        removeCallbacks(cross)
    }

    interface OnScrollEndListener{
        fun onScrollLeftEnd(firstData: KLineData)
        fun onScrollRightEnd(lastData: KLineData)
    }

    /** 需要实时更新的周期 */
    private val updatePeriod = listOf(
        ChartConstant.PERIOD_1_MINUTE,
        ChartConstant.PERIOD_3_MINUTE,
        ChartConstant.PERIOD_5_MINUTE,
        ChartConstant.PERIOD_10_MINUTE,
        ChartConstant.PERIOD_15_MINUTE,
        ChartConstant.PERIOD_20_MINUTE,
        ChartConstant.PERIOD_30_MINUTE,
        ChartConstant.PERIOD_60_MINUTE,
        ChartConstant.PERIOD_2_HOUR,
        ChartConstant.PERIOD_3_HOUR,
        ChartConstant.PERIOD_4_HOUR,
        ChartConstant.PERIOD_6_HOUR,
        ChartConstant.PERIOD_8_HOUR,
        ChartConstant.PERIOD_12_HOUR,
        ChartConstant.PERIOD_DAY,
        ChartConstant.PERIOD_WEEK,
        ChartConstant.PERIOD_MONTH,
        ChartConstant.PERIOD_SEASON,
        ChartConstant.PERIOD_YEAR)

    private val hourPeriod = listOf(
        ChartConstant.PERIOD_2_HOUR,
        ChartConstant.PERIOD_3_HOUR,
        ChartConstant.PERIOD_4_HOUR,
        ChartConstant.PERIOD_6_HOUR,
        ChartConstant.PERIOD_8_HOUR,
        ChartConstant.PERIOD_12_HOUR)

    /**
     * 更新推送数据
     */
    fun updateQuote(quoteBean: QuoteBean) {
        this.quoteBean = quoteBean

        if(dataInited && updatePeriod.contains(periodTag)){
            val lastVisible = listVisible.lastOrNull()
            val lastSource = source.lastOrNull()
            formatNewDataTime(quoteBean)
            val isLast = if(lastVisible != null && lastSource != null){
                inSamePeriod(lastVisible.time, lastSource.time)
            }else{
                true
            }
            addKLineNewData(KLineData(quoteBean), isLast)
        }
    }

    private fun formatNewDataTime(quoteBean: QuoteBean){

        val calendar =  Calendar.getInstance()
        calendar.timeInMillis = quoteBean.recentTime

        when(periodTag){
            ChartConstant.PERIOD_3_MINUTE -> calendar.set(Calendar.MINUTE, (calendar.get(Calendar.MINUTE)/3)*3)
            ChartConstant.PERIOD_5_MINUTE -> calendar.set(Calendar.MINUTE, (calendar.get(Calendar.MINUTE)/5)*5)
            ChartConstant.PERIOD_10_MINUTE -> calendar.set(Calendar.MINUTE, (calendar.get(Calendar.MINUTE)/10)*10)
            ChartConstant.PERIOD_15_MINUTE -> calendar.set(Calendar.MINUTE, (calendar.get(Calendar.MINUTE)/15)*15)
            ChartConstant.PERIOD_20_MINUTE -> calendar.set(Calendar.MINUTE, (calendar.get(Calendar.MINUTE)/20)*20)
            ChartConstant.PERIOD_30_MINUTE -> calendar.set(Calendar.MINUTE, (calendar.get(Calendar.MINUTE)/30)*30)
            ChartConstant.PERIOD_60_MINUTE -> calendar.set(Calendar.MINUTE, (calendar.get(Calendar.MINUTE)/60)*60)
            ChartConstant.PERIOD_2_HOUR -> calendar.set(Calendar.HOUR_OF_DAY, (calendar.get(Calendar.HOUR_OF_DAY)/2)*2)
            ChartConstant.PERIOD_3_HOUR -> calendar.set(Calendar.HOUR_OF_DAY, (calendar.get(Calendar.HOUR_OF_DAY)/3)*3)
            ChartConstant.PERIOD_4_HOUR -> calendar.set(Calendar.HOUR_OF_DAY, (calendar.get(Calendar.HOUR_OF_DAY)/4)*4)
            ChartConstant.PERIOD_6_HOUR -> calendar.set(Calendar.HOUR_OF_DAY, (calendar.get(Calendar.HOUR_OF_DAY)/6)*6)
            ChartConstant.PERIOD_8_HOUR -> calendar.set(Calendar.HOUR_OF_DAY, (calendar.get(Calendar.HOUR_OF_DAY)/8)*8)
            ChartConstant.PERIOD_12_HOUR -> calendar.set(Calendar.HOUR_OF_DAY, (calendar.get(Calendar.HOUR_OF_DAY)/12)*12)
            ChartConstant.PERIOD_SEASON -> calendar.set(Calendar.MONTH, (calendar.get(Calendar.MONTH)/3)*3)
        }
        quoteBean.recentTime = calendar.timeInMillis
    }

    fun scrollToPreDay(time: Long){
        val index = listVisible.indexOfFirst { it.time == time }
        if(showCross && index > 0){
            crossX = listVisible[index - 1].chartX
            postInvalidate()
        }
    }

    fun scrollToNextDay(time: Long){
        val index = listVisible.indexOfFirst { it.time == time }
        if(showCross && index >= 0 && index < listVisible.size - 1){
            crossX = listVisible[index + 1].chartX
            postInvalidate()
        }
    }
}