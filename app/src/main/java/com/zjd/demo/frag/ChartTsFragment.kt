package com.zjd.demo.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.TimeUtils
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.zjd.demo.MainActivity
import com.zjd.demo.databinding.FragmentChartTsBinding
import com.zjd.demo.net.getGw
import com.zjd.unite.chart.chart.*
import com.zjd.unite.chart.entity.QuoteBean
import com.zjd.unite.chart.entity.TsHisBean
import com.zjd.unite.chart.entity.TsLineData
import com.zjd.unite.chart.utils.DataRequest
import com.zjd.unite.chart.utils.setParamsText
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.Exception

/**
 * @author ZJD
 * @date 2021/6/30
 * @desc 分时图
 **/
class ChartTsFragment : BaseChartFragment<TsLineData>(), OnChartSingleClick {

    /** 竖屏 */
    private lateinit var mBinding: FragmentChartTsBinding

    /** 副图相关  */
    private var assistIndex = "分时量"

    private val KEY_ID = "KEY_ID"

    companion object {
        @JvmStatic
        fun newInstance(quoteId: Int) = ChartTsFragment().apply {
            arguments = Bundle().apply {
                putInt(KEY_ID, quoteId)
            }
        }
    }

    override fun init() {
        arguments?.let {
            mQuoteId = it.getInt(KEY_ID, mQuoteId)
        }

        initAssistChart()
        initData()
        initListener()

    }

    private fun initListener() {
        mBinding.tsAssistChart.setOnChartSingleClick(this)
    }

    /**
     * 初始化副图
     */
    private fun initAssistChart() {
        mBinding.llTsAssist.visibility = View.VISIBLE
        mBinding.tsAssistChart.visibility = View.VISIBLE

            if (assistIndex == "MACDFS") {
                mBinding.tsAssistChart.setAssistType(TS_ASSIST_TYPE_MACDFS)
            } else {
                mBinding.tsAssistChart.setAssistType(TS_ASSIST_TYPE_VOL)
            }
        mBinding.tsChart.postInvalidate()
        mBinding.tvSwitch.text = assistIndex
    }

    private fun initData() {
        queryTSLineHisData(mQuoteId)
    }

    private fun getSuccess(tsHisList: MutableList<TsHisBean>?) {
        if(mBinding.tsChart.isLoaded()){
            mBinding.tsChart.setTsHisBean(tsHisList?.last())
            mBinding.tsChart.postInvalidate()
        }else{
            initTsChart(tsHisList)
        }
    }

    private fun initTsChart(tsHisList: MutableList<TsHisBean>?) {
        mBinding.tsChart.bindAssist(mBinding.tsAssistChart)
        mBinding.tsChart.setQuoteBean(2)
        mBinding.tsChart.setTsHisBean(tsHisList?.last())
        mBinding.tsChart.onDataSelectListener = this
        mBinding.tsChart.setOnChartSingleClick(this)
        mBinding.tsChart.postInvalidate()

        if(activity is MainActivity){
            goldenCutEvent((activity as MainActivity).drawLineStatus)
        }
    }

    override fun onDataSelect(data: TsLineData, showCross: Boolean, crossX: Float) {
        setParamsText(mBinding.tvParamsMain, data, MAIN_TYPE_TS, mBinding.tsChart.midValue, 2)
        setParamsText(mBinding.tvParamsAssist, data, mBinding.tsAssistChart.assistType, dec = 2)
    }

    override fun updateQuote(quote: QuoteBean) {
        //更新图表数据
        try{
            mBinding.tsChart.listVisible.lastOrNull()?.let {
                val testNew = QuoteBean().apply {
                    recentTime = TimeUtils.string2Millis(TimeUtils.millis2String(System.currentTimeMillis(), "yyyy-MM-dd HH:mm"), "yyyy-MM-dd HH:mm")
                    currentPrice =  (Math.random() * 0.002 + 0.999) * it.close
                    holding = it.holding*0.1
                    vol = 2
                    contractSize = 1
                }
                mBinding.tsChart.updateQuote(testNew)
            }
        }catch (e: Exception){

        }
    }

    override fun refreshData() {
        super.refreshData()
        initData()
    }

    private fun setAssistIndex(index: String) {
        try {
            assistIndex = index
            initAssistChart()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onChartSingleClick(chart: BaseChart<Any>) {
        if(chart is TsChart){
            if(activity is MainActivity && (activity as MainActivity).drawLineStatus == 1){
                if(!mBinding.tsChart.isShowGoldenCut()){
                    mBinding.tsChart.openGoldenCut(true)
                }
            }
        }else{
            //切换指标
            val tsChartNames: List<String> = getTsAssistChartName()
            if (tsChartNames.isNotEmpty()) {
                val currentIndex = tsChartNames.indexOf(assistIndex)
                if (currentIndex < 0 || currentIndex >= tsChartNames.size - 1) {
                    setAssistIndex(tsChartNames[0])
                } else {
                    setAssistIndex(tsChartNames[currentIndex + 1])
                }
            }
        }
    }

    private fun getTsAssistChartName(): List<String> {
        return mutableListOf("分时量","MACDFS")
    }

    override fun goldenCutEvent(status: Int) {
        when(status){
            0 -> {
                mBinding.tsChart.openGoldenCut(false)
            }
            1 -> {
                mBinding.tsChart.openGoldenCut(true)
            }
            2 -> {

            }
            3 -> {

            }
        }
    }

    override fun binding(inflater: LayoutInflater, container: ViewGroup?): ViewBinding {
        mBinding = FragmentChartTsBinding.inflate(inflater, container, false)
        return mBinding
    }

    private fun queryTSLineHisData(id: Int, offset: Int = 0, contractSize: Int = 1, time: Long = 0) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("symbol", id)
        jsonObject.addProperty("time", time)
        jsonObject.addProperty("offset", offset)
        jsonObject.addProperty("clientType", 73)
        getGw().getQuoteData(73, "T222441", "his", jsonObject)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                val jsonArray = (result as JsonObject)["data"].asJsonObject["items"].asJsonArray
                val list: MutableList<TsHisBean> = Gson().fromJson(jsonArray.toString(), object : TypeToken<List<TsHisBean>>() {}.type)
                val allTsLine = DataRequest.getALLTsLine(list, id, contractSize)
                getSuccess(allTsLine)
            }) { throwable: Throwable ->
                throwable.printStackTrace()
            }
    }
}