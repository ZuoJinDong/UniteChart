package com.zjd.demo.frag

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewbinding.ViewBinding
import com.alibaba.fastjson.JSON
import com.blankj.utilcode.util.TimeUtils
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.zjd.demo.MainActivity
import com.zjd.demo.databinding.FragmentChartKBinding
import com.zjd.demo.net.getGw
import com.zjd.unite.chart.chart.*
import com.zjd.unite.chart.constant.ChartConstant
import com.zjd.unite.chart.constant.QuoteConstant
import com.zjd.unite.chart.entity.*
import com.zjd.unite.chart.event.DrawLineEvent
import com.zjd.unite.chart.utils.formatDouble
import com.zjd.unite.chart.utils.setParamsText
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.Exception

/**
 * @author ZJD
 * @date 2021/6/30
 * @desc K线图
 **/
class ChartKFragment : BaseChartFragment<KLineData>(),
    KChart.OnScrollEndListener, OnChartSingleClick {

    private lateinit var mBinding: FragmentChartKBinding

    private lateinit var assistViews: List<LinearLayout>
    private lateinit var tvSwitchList: List<TextView>
    private lateinit var tvParamList: List<TextView>
    private lateinit var assistCharts: List<KAssistChart>
    
    private var mDec = 2

    private var kIndexMain = MAIN_TYPE_MA

    companion object {
        
        const val KEY_ID = "KEY_ID"
        const val KEY_PERIOD = "KEY_PERIOD"
        
        fun newInstance(quoteId: Int, period: Int = ChartConstant.PERIOD_DAY) = ChartKFragment().apply {
            arguments = Bundle().apply {
                putInt(KEY_ID, quoteId)
                putInt(KEY_PERIOD, period)
            }
        }
    }

    override fun init() {
        arguments?.let {
            mQuoteId = it.getInt(KEY_ID, mQuoteId)
            mPeriod = it.getInt(KEY_PERIOD, mPeriod)
        }

        startTime = System.currentTimeMillis() + QuoteConstant.day_time * 4L
        
        initAssistChart()
        initKChart()
        initData()
    }

    private fun initAssistChart() {
        assistViews = listOf(
            mBinding.llAssist1,
            mBinding.llAssist2,
            mBinding.llAssist3,
            mBinding.llAssist4)

        tvSwitchList = listOf(
            mBinding.tvSwitch1,
            mBinding.tvSwitch2,
            mBinding.tvSwitch3,
            mBinding.tvSwitch4)

        tvParamList = listOf(
            mBinding.tvParams1,
            mBinding.tvParams2,
            mBinding.tvParams3,
            mBinding.tvParams4)

        assistCharts = listOf(
            mBinding.kAssistChart1,
            mBinding.kAssistChart2,
            mBinding.kAssistChart3,
            mBinding.kAssistChart4)

        assistCharts.forEachIndexed { index, kAssistChart ->
            kAssistChart.assistType = kAssistChart.typeList[index]
            tvSwitchList[index].text = kAssistChart.assistType
            mBinding.kChart.bindAssist(kAssistChart)
            kAssistChart.setOnChartSingleClick(object : OnChartSingleClick{
                override fun onChartSingleClick(chart: BaseChart<Any>) {
                    kAssistChart.switchAssist()
                    setAssistIndex(index, kAssistChart.assistType)
                }
            })
        }

        refreshAssistCount(2)
    }

    private fun setAssistIndex(index: Int, indexName: String) {
        mBinding.kChart.hideCross()
        tvSwitchList[index].text = indexName
    }

    /**
     * 刷新副图指标数量
     */
    private fun refreshAssistCount(count: Int = 1) {
        assistViews.forEachIndexed { index, kAssistViews ->
            kAssistViews.visibility = if(index < count){
                View.VISIBLE
            }else{
                View.GONE
            }
        }
    }

    private var offset = 150
    private var periodStr: String = "day"
    private var startTime: Long = 0
    private var endFlag = -1

    private fun initData() {
        when (mPeriod) {
            // 日k
            ChartConstant.PERIOD_DAY -> {
                periodStr = "day"
                offset = 300
            }
            // 1
            ChartConstant.PERIOD_1_MINUTE -> {
                periodStr = "m1"
                offset = 400
            }
            // 3
            ChartConstant.PERIOD_3_MINUTE -> {
                periodStr = "m1"
                offset = 1200
            }
            // 5
            ChartConstant.PERIOD_5_MINUTE -> {
                periodStr = "m5"
                offset = 400
            }
            // 10
            ChartConstant.PERIOD_10_MINUTE -> {
                periodStr = "m5"
                offset = 800
            }
            // 15
            ChartConstant.PERIOD_15_MINUTE -> {
                periodStr = "m5"
                offset = 1200
            }
            // 20
            ChartConstant.PERIOD_20_MINUTE -> {
                periodStr = "m5"
                offset = 1600
            }
            // 30
            ChartConstant.PERIOD_30_MINUTE -> {
                periodStr = "m5"
                offset = 2400
            }
            // 1h
            ChartConstant.PERIOD_60_MINUTE -> {
                periodStr = "hr"
                offset = 400
            }
            // 2h
            ChartConstant.PERIOD_2_HOUR -> {
                periodStr = "hr"
                offset = 800
            }
            // 3h
            ChartConstant.PERIOD_3_HOUR -> {
                periodStr = "hr"
                offset = 1200
            }
            // 4h
            ChartConstant.PERIOD_4_HOUR -> {
                periodStr = "hr"
                offset = 1600
            }
            // 6h
            ChartConstant.PERIOD_6_HOUR -> {
                periodStr = "hr"
                offset = 2400
            }
            // 8h
            ChartConstant.PERIOD_8_HOUR -> {
                periodStr = "hr"
                offset = 3200
            }
            // 12h
            ChartConstant.PERIOD_12_HOUR -> {
                periodStr = "hr"
                offset = 4800
            }
            // 周K
            ChartConstant.PERIOD_WEEK -> {
                periodStr = "day"
                offset = 2800
            }
            // 月K
            ChartConstant.PERIOD_MONTH -> {
                periodStr = "month"
                offset = 400
            }
            // 季K
            ChartConstant.PERIOD_SEASON -> {
                periodStr = "month"
                offset = 1200
            }
            // 年K
            ChartConstant.PERIOD_YEAR -> {
                periodStr = "month"
                offset = 2400
            }
        }
        if(endFlag == 1){
            queryKLineHisData(mQuoteId, periodStr, startTime + QuoteConstant.min_time, offset * endFlag)
        }else{
            queryKLineHisData(mQuoteId, periodStr, startTime, offset * endFlag)
        }
    }

    private fun getSuccess(data: KHisData) {
        addKChart(data)
    }

    private fun initKChart() {
        setMainIndex()
        mBinding.kChart.periodTag = mPeriod
        mBinding.kChart.setOnChartSingleClick(this)
        mBinding.kChart.onDataSelectListener = this
        mBinding.kChart.onScrollEndListener = this
        mBinding.kChart.setDec(mDec)
    }

    /**
     * 新增K线数据
     */
    private fun addKChart(kHisData: KHisData?) {
        if(mBinding.kChart.isDataEmpty()){
            mBinding.kChart.setKLineList(kHisData?.pointList)

            if(activity is MainActivity){
                goldenCutEvent((activity as MainActivity).drawLineStatus)
            }
        }else{
            mBinding.kChart.addKLineList(kHisData?.pointList, endFlag)
        }
    }

    /**
     * 数据选择展示
     */
    override fun onDataSelect(data: KLineData, showCross: Boolean, crossX: Float) {
        when(mBinding.kChart.mainType){
            MAIN_TYPE_MABOLL -> {
                mBinding.llMa.visibility = View.VISIBLE
                mBinding.tvParams.visibility = View.VISIBLE
                setMaParams(data.ma)
                setParamsText(mBinding.tvParams, data, mBinding.kChart.mainType)
            }
            MAIN_TYPE_MA -> {
                mBinding.llMa.visibility = View.VISIBLE
                mBinding.tvParams.visibility = View.GONE
                setMaParams(data.ma)
            }
            else -> {
                mBinding.llMa.visibility = View.GONE
                mBinding.tvParams.visibility = View.VISIBLE
                setParamsText(mBinding.tvParams, data, mBinding.kChart.mainType)
            }
        }

        tvParamList.forEachIndexed { index, tvParams ->
            setParamsText(tvParams, data, assistCharts[index].assistType)
        }
    }

    /**
     * 展示MA参数
     */
    @SuppressLint("SetTextI18n")
    private fun setMaParams(ma: MA) {
        mBinding.tvPriceMa1.text = "${ChartParams.PARAM_MAIN_MA[0]}:${formatDouble(ma.ma1, mDec) }"
        mBinding.tvPriceMa2.text = "${ChartParams.PARAM_MAIN_MA[1]}:${formatDouble(ma.ma2, mDec) }"
        mBinding.tvPriceMa3.text = "${ChartParams.PARAM_MAIN_MA[2]}:${formatDouble(ma.ma3, mDec) }"
        mBinding.tvPriceMa4.text = "${ChartParams.PARAM_MAIN_MA[3]}:${formatDouble(ma.ma4, mDec) }"
        mBinding.tvPriceMa5.text = "${ChartParams.PARAM_MAIN_MA[4]}:${formatDouble(ma.ma5, mDec) }"
    }

    override fun onResume() {
        super.onResume()
        maParamVisible(mBinding.tvPriceMa1, ChartParams.PARAM_MAIN_MA[0])
        maParamVisible(mBinding.tvPriceMa2, ChartParams.PARAM_MAIN_MA[1])
        maParamVisible(mBinding.tvPriceMa3, ChartParams.PARAM_MAIN_MA[2])
        maParamVisible(mBinding.tvPriceMa4, ChartParams.PARAM_MAIN_MA[3])
        maParamVisible(mBinding.tvPriceMa5, ChartParams.PARAM_MAIN_MA[4])
    }

    private fun maParamVisible(view: View, param: Int){
        view.visibility = if(param == 0) View.GONE else View.VISIBLE
    }

    /**
     * 滚动到左侧
     */
    override fun onScrollLeftEnd(firstData: KLineData) {
        firstData.time.let { time ->
            if (startTime != time) {
                endFlag = -1
                startTime = time
                initData()
            }
        }
    }

    /**
     * 滚动到右侧
     */
    override fun onScrollRightEnd(lastData: KLineData) {
        lastData.time.let { time ->
            if (startTime != time) {
                endFlag = 1
                startTime = time
                initData()
            }
        }
    }

    /**
     * 更新数据
     */
    override fun updateQuote(quote: QuoteBean) {
        try{
            mBinding.kChart.getAllData().lastOrNull()?.let {
                val testNew = QuoteBean().apply {
                    recentTime = TimeUtils.string2Millis(TimeUtils.millis2String(System.currentTimeMillis(), "yyyy-MM-dd HH:mm"), "yyyy-MM-dd HH:mm")
                    currentPrice =  (Math.random() * 0.002 + 0.999) * it.close
                    holding = it.holding*0.1
                    vol = 2
                    contractSize = 1
                }
                mBinding.kChart.updateQuote(testNew)
            }
        }catch (e: Exception){

        }
    }

    override fun onChartSingleClick(chart: BaseChart<Any>) {
        if(activity is MainActivity && (activity as MainActivity).drawLineStatus == 1){
            if(!mBinding.kChart.isShowGoldenCut()){
                mBinding.kChart.openGoldenCut(true)
            }
        }else{
            //切换指标
            val kChartNames: List<String> = mBinding.kChart.typeList
            val currentIndex = kChartNames.indexOf(kIndexMain)
            kIndexMain = if (currentIndex < 0 || currentIndex >= kChartNames.size - 1) {
                kChartNames[0]
            } else {
                kChartNames[currentIndex + 1]
            }
            setMainIndex(kIndexMain)
        }
    }

    /**
     * 设置主图 类型
     * @param checkedIndex
     */
    private fun setMainIndex(checkedIndex: String = kIndexMain) {
        mBinding.kChart.hideCross()
        mBinding.kChart.setMainType(checkedIndex)
        mBinding.kChart.postInvalidate()
        mBinding.tvSwitch.text = checkedIndex
    }

    override fun goldenCutEvent(status: Int) {
        when(status){
            DrawLineEvent.STATUS_CLOSE -> mBinding.kChart.openGoldenCut(false)
            DrawLineEvent.STATUS_OPEN -> mBinding.kChart.openGoldenCut(true)
        }
    }

    override fun binding(inflater: LayoutInflater, container: ViewGroup?): ViewBinding {
        mBinding = FragmentChartKBinding.inflate(inflater, container, false)
        return mBinding
    }

    private fun queryKLineHisData(id: Int, period: String, time: Long, offset: Int) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("symbol", id)
        jsonObject.addProperty("period", period)
        jsonObject.addProperty("startTime", time)
        jsonObject.addProperty("offset", offset)
        jsonObject.addProperty("clientType", 73)

        getGw().getQuoteData(73,"T222371", "his", jsonObject)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({ result ->
                val data = (result as JsonObject)["data"].asJsonObject
                val newKHis = Gson().fromJson(data.toString(), KHisData::class.java)
                getSuccess(newKHis)
            }) { throwable: Throwable ->
                throwable.printStackTrace()
            }
    }
}