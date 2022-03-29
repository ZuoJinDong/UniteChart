package com.zjd.demo.frag

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.alibaba.fastjson.JSON
import com.blankj.utilcode.util.TimeUtils
import com.google.gson.JsonObject
import com.zjd.demo.MainActivity
import com.zjd.demo.databinding.FragmentChartTsDuoBinding
import com.zjd.demo.net.getGw
import com.zjd.unite.chart.chart.BaseChart
import com.zjd.unite.chart.chart.MAIN_TYPE_TS
import com.zjd.unite.chart.chart.OnChartSingleClick
import com.zjd.unite.chart.chart.TsDuoMultiChart
import com.zjd.unite.chart.constant.ChartConstant
import com.zjd.unite.chart.entity.QuoteBean
import com.zjd.unite.chart.entity.TsHisBean
import com.zjd.unite.chart.entity.TsLineData
import com.zjd.unite.chart.event.DrawLineEvent
import com.zjd.unite.chart.utils.DataRequest
import com.zjd.unite.chart.utils.setParamsText
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.Exception

/**
 * @author ZJD
 * @date 2021/6/30
 * @desc 多日分时图
 **/
class ChartTsDuoFragment : BaseChartFragment<TsLineData>(), OnChartSingleClick, TsDuoMultiChart.OnDuoDataSelectListener {

    companion object {
        const val KEY_ID = "KEY_ID"
        const val KEY_PERIOD = "KEY_PERIOD"
        
        fun newInstance(quoteId: Int, period: Int = ChartConstant.PERIOD_5_DAY) = ChartTsDuoFragment().apply {
            arguments = Bundle().apply {
                putInt(KEY_ID, quoteId)
                putInt(KEY_PERIOD, period)
            }
        }
    }
    
    private lateinit var mBinding: FragmentChartTsDuoBinding

    override fun init() {
        arguments?.let {
            mQuoteId = it.getInt(KEY_ID, mQuoteId)
            mPeriod = it.getInt(KEY_PERIOD, mPeriod)
        }

        mBinding.btnSwitchTsMode.isChecked = false
        mBinding.tvParams.visibility = View.VISIBLE
        mBinding.tsDuoChart.visibility = View.VISIBLE
        mBinding.tsMultiChart.visibility = View.GONE

        mBinding.btnSwitchTsMode.setOnCheckedChangeListener { buttonView, isChecked ->
            mBinding.tvParams5.text = ""
            mBinding.tvParams.visibility = if(isChecked) View.INVISIBLE else View.VISIBLE

            if (isChecked) {
                mBinding.tsDuoChart.visibility = View.GONE
                mBinding.tsMultiChart.visibility = View.VISIBLE
            }else{
                mBinding.tsDuoChart.visibility = View.VISIBLE
                mBinding.tsMultiChart.visibility = View.GONE
            }
        }

        initData()
    }

    private fun initData() {
        val offset = when(mPeriod){
            ChartConstant.PERIOD_3_DAY -> -2
            ChartConstant.PERIOD_5_DAY -> -4
            else -> -1
        }
        queryTSLineHisData(mQuoteId, offset, 1)
    }

    private fun getSuccess(tsHisList: MutableList<TsHisBean>) {
            if(mBinding.tsDuoChart.isLoaded()){
                mBinding.tsDuoChart.setTsLineList(tsHisList)
                mBinding.tsDuoChart.postInvalidate()
            }else{
                initTsChart(tsHisList)
            }
    }

    @SuppressLint("SetTextI18n")
    private fun initTsChart(tsHisList: MutableList<TsHisBean>) {
        mBinding.tsMultiChart.setTsLineList(tsHisList)
        mBinding.tsMultiChart.onDuoDataSelectListener = this
        mBinding.tsMultiChart.setOnChartSingleClick(this)

        mBinding.tsDuoChart.setTsLineList(tsHisList)
        mBinding.tsDuoChart.postInvalidate()
        mBinding.tsDuoChart.onDataSelectListener = this
        mBinding.tsDuoChart.setOnChartSingleClick(this)

        //黄金分割线状态
        if(activity is MainActivity){
            goldenCutEvent((activity as MainActivity).drawLineStatus)
        }
    }

    /** 连续走势选中数据 */
    override fun onDataSelect(data: TsLineData, showCross: Boolean, crossX: Float) {
        setParamsText(mBinding.tvParams, data, MAIN_TYPE_TS, mBinding.tsDuoChart.midValue)
    }

    /** 叠加走势选中数据 */
    override fun onDuoDataSelect(params: List<String>) {
        val builder = SpannableStringBuilder()
        var preIndex: Int
        params.forEachIndexed { index, param ->
            preIndex = builder.length
            builder.append(param).append("\n")
            builder.setSpan(ForegroundColorSpan(mBinding.tsDuoChart.pathColors[mBinding.tsDuoChart.pathColors.size - 1 - index]), preIndex, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        mBinding.tvParams5.text = builder
    }

    /** 更新行情 */
    override fun updateQuote(quote: QuoteBean) {
        try{
            mBinding.tsDuoChart.listVisible.lastOrNull()?.let {
                val testNew = QuoteBean().apply {
                    recentTime = TimeUtils.string2Millis(TimeUtils.millis2String(System.currentTimeMillis(), "yyyy-MM-dd HH:mm"), "yyyy-MM-dd HH:mm")
                    currentPrice =  (Math.random() * 0.002 + 0.999) * it.close
                    holding = it.holding*0.1
                    vol = 2
                    contractSize = 1
                }
                mBinding.tsDuoChart.updateQuote(testNew)
                mBinding.tsMultiChart.updateQuote(testNew)
            }
        }catch (e: Exception){

        }
    }

    /** 重新请求数据 */
    override fun refreshData() {
        super.refreshData()
        initData()
    }

    override fun goldenCutEvent(status: Int) {
        when(status){
            //关闭黄金分割
            DrawLineEvent.STATUS_CLOSE -> {
                mBinding.tsDuoChart.openGoldenCut(false)
                mBinding.tsMultiChart.openGoldenCut(false)
            }
            //打开黄金分割
            DrawLineEvent.STATUS_OPEN -> {
                mBinding.tsDuoChart.openGoldenCut(true)
                mBinding.tsMultiChart.openGoldenCut(true)
            }
        }
    }

    override fun onChartSingleClick(chart: BaseChart<Any>) {
        if(activity is MainActivity && (activity as MainActivity).drawLineStatus == 1){
            if(!mBinding.tsDuoChart.isShowGoldenCut()){
                mBinding.tsDuoChart.openGoldenCut(true)
            }
            if(!mBinding.tsMultiChart.isShowGoldenCut()){
                mBinding.tsMultiChart.openGoldenCut(true)
            }
        }
    }

    override fun binding(inflater: LayoutInflater, container: ViewGroup?): ViewBinding {
        mBinding = FragmentChartTsDuoBinding.inflate(inflater, container, false)
        return mBinding
    }

    private fun queryTSLineHisData(id: Int, offset: Int = 0, contractSize: Int = 0, time: Long = 0) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("symbol", id)
        jsonObject.addProperty("time", time)
        jsonObject.addProperty("offset", offset)
        jsonObject.addProperty("clientType", 73)
        getGw().getQuoteData(73,"T222441", "his", jsonObject)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                val jsonArray = (result as JsonObject)["data"].asJsonObject["items"].asJsonArray
                val list: MutableList<TsHisBean> = JSON.parseArray(jsonArray.toString(), TsHisBean::class.java)
                val allTsLine = DataRequest.getALLTsLine(list, id, contractSize)
                getSuccess(allTsLine)
            }) { throwable: Throwable ->
                throwable.printStackTrace()
            }
    }
}