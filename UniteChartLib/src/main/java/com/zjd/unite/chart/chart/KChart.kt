package com.zjd.unite.chart.chart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import com.blankj.utilcode.util.ThreadUtils
import com.zjd.unite.chart.R
import com.zjd.unite.chart.constant.ChartConstant
import com.zjd.unite.chart.entity.*
import com.zjd.unite.chart.utils.*
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

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

class KChart @JvmOverloads constructor(mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : GoldenCutChart<KLineData>(mContext, attrs, defStyleAttr) {

    /** 源数据（未合并） */
    private val source = mutableListOf<KLineData>()
    /** 展示数据（未合并） */
    private var sourceVisible = mutableListOf<KLineData>()

    /** 指标 */
    val typeList = listOf(
            MAIN_TYPE_MA,
            MAIN_TYPE_BOLL,
            MAIN_TYPE_MABOLL,
            MAIN_TYPE_SAR,
            MAIN_TYPE_EXPMA)

    /** 主图指标 */
    var mainType = MAIN_TYPE_MA
    /** 副图 */
    private val assistChartList = mutableListOf<KAssistChart>()
    /** 周期 */
    var periodTag = ChartConstant.PERIOD_DAY

    /** 画笔-线 */
    private var candlePaint = Paint().apply {
        isAntiAlias = true
        color = colorRed
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = dp2px(0.5f)
    }

    /** 滚动至数据尽头 */
    var onScrollEndListener: OnScrollEndListener? = null

    init {
        visibleCount = 40
        emptyOffsetFlag = 1
    }

    fun setDec(dec: Int){
        mDec = dec
    }

    /**
     * 绑定副图
     */
    fun bindAssist(assistChart: KAssistChart){
        assistChart.bindMainChart(this)
        assistChartList.add(assistChart)
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
            //可见数据大于290时简单绘制趋势
            drawLine(canvas)
        }else{
            //绘制蜡烛图
            drawCandle(canvas)
            //绘制指标
            drawMainParams(canvas)
        }
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
                //合并数据
                val listMerge = QuoteUtils.mergeKHisData(sourceVisible, periodTag)
                //指标计算
                DataFormatHelper.calculateK(listMerge)
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
            }
        }

        drawMaxAndMin(canvas, minIndex, false)
        drawMaxAndMin(canvas, maxIndex, true)
    }

    /**
     * 达到最大显示量时
     * 路径 path
     */
    private fun drawLine(canvas: Canvas) {
        val path = Path()

        listVisible.forEachIndexed { index, kLine ->
            kLine.apply {
                if(path.isEmpty){
                    path.moveTo(chartX, getValueY(high))
                }else{
                    path.lineTo(chartX, getValueY(high))
                }
            }
        }
        linePaint.style = Paint.Style.STROKE
        linePaint.color = colorBlue
        canvas.drawPath(path, linePaint)
    }

    override fun requestParentDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        assistChartList.forEach {
            it.requestParentDisallowInterceptTouchEvent(disallowIntercept)
        }
        super.requestParentDisallowInterceptTouchEvent(disallowIntercept)
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