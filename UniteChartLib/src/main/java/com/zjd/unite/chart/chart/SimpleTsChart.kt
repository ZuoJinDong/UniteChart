package com.zjd.unite.chart.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.Gravity
import androidx.core.view.ViewCompat
import com.zjd.unite.chart.entity.TradeTimeBean
import com.zjd.unite.chart.entity.TsHisBean
import com.zjd.unite.chart.entity.TsLineData
import com.zjd.unite.chart.utils.getColor
import com.zjd.unite.chart.R
import kotlin.math.abs

/**
 * @author ZJD
 * @date 2022/3/7
 * @desc 列表分时图
 **/
class SimpleTsChart @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        gravity = Gravity.CENTER
        textSize = 12f
        setTextColor(getColor(context, R.color.uc_text_dark))
    }

    fun refreshLine(quoteId: Int, color: Int? = null){
        TsHisBean.getFromCache(quoteId)?.let {
            text = ""
            setTsHisBean(it, color)
        }?:let {
            background = null
            text = "加载中"
        }
    }

    fun setTsHisBean(tsHisBean: TsHisBean?, color: Int? = null){
        tsHisBean?.let {
            val timeAll = getAllTime(tsHisBean.tradeTime)

            if(it.pointList.isEmpty()){
                it.pointList.add(TsLineData().apply {
                    close = it.yersterdayClose
                    System.currentTimeMillis().let { timeLong ->
                        val current = DateUtil.getMinuteDate(DateUtil.getMinuteStr(timeLong)).time
                        timeAll.minByOrNull { abs(current - it) }?.let {
                            time = it
                        }
                    }
                })
            }

            val lineBitmap = WidgetLine.getLineBitmap(measuredWidth, measuredHeight, tsHisBean, timeAll, color)
            ViewCompat.setBackground(this, BitmapDrawable(context.resources, lineBitmap))
        }
    }

    /**
     * 所有点的时间戳
     */
    private fun getAllTime(tradeTime: List<TradeTimeBean>): List<Long>{
        val timeAll = mutableListOf<Long>()
        tradeTime.forEach { tradeTimeBean ->
            tradeTimeBean.apply {
                for(index in 0.. ((end - start)/60000).toInt() + 1){
                    timeAll.add(start + index * 60000)
                }
            }
        }
        return timeAll
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

}