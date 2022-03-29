package com.zjd.demo.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.alibaba.fastjson.JSON
import com.blankj.utilcode.util.TimeUtils
import com.google.gson.JsonObject
import com.zjd.demo.databinding.FragmentChartFlashBinding
import com.zjd.demo.frag.ChartKFragment.Companion.KEY_ID
import com.zjd.demo.net.getGw
import com.zjd.unite.chart.entity.FlashHisBean
import com.zjd.unite.chart.entity.QuoteBean
import com.zjd.unite.chart.entity.TsLineData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.Exception

/**
 * @author ZJD
 * @date 2021/6/30
 * @desc 闪电图
 **/
class ChartFlashFragment : BaseChartFragment<TsLineData>() {

    companion object {
        fun newInstance(quoteId: Int) = ChartFlashFragment().apply {
            arguments = Bundle().apply {
                putInt(KEY_ID, quoteId)
            }
        }
    }

    private lateinit var mBinding: FragmentChartFlashBinding

    override fun init() {
        arguments?.let {
            mQuoteId = it.getInt(KEY_ID, mQuoteId)
        }

        initData()
    }

    private fun initData() {
        queryTSLineHisData(mQuoteId, 300)
    }

    private fun getSuccess(tsHisList: MutableList<FlashHisBean>) {
            if(mBinding.flashChart.isLoaded()){
                mBinding.flashChart.setTsHisBean(tsHisList)
                mBinding.flashChart.postInvalidate()
            }else{
                initFlashChart(tsHisList)
            }
    }

    private fun initFlashChart(tsHisList: MutableList<FlashHisBean>?) {
        mBinding.flashChart.setDec(2)
        mBinding.flashChart.setTsHisBean(tsHisList)
        mBinding.flashChart.postInvalidate()
    }

    override fun updateQuote(quote: QuoteBean) {
        //更新图表数据
        try{
            mBinding.flashChart.listVisible.lastOrNull()?.let {
                val testNew = QuoteBean().apply {
                    recentTime = TimeUtils.string2Millis(TimeUtils.millis2String(System.currentTimeMillis(), "yyyy-MM-dd HH:mm"), "yyyy-MM-dd HH:mm")
                    currentPrice =  (Math.random() * 0.0002 + 0.9999) * it.orig
                }
                mBinding.flashChart.updateQuote(testNew)
            }
        }catch (e: Exception){

        }
    }

    override fun refreshData() {
        super.refreshData()
        initData()
    }

    override fun goldenCutEvent(status: Int) {

    }

    override fun binding(inflater: LayoutInflater, container: ViewGroup?): ViewBinding {
        mBinding = FragmentChartFlashBinding.inflate(inflater, container, false)
        return mBinding
    }

    private fun queryTSLineHisData(id: Int, offset: Int) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("symbol", id)
        jsonObject.addProperty("offset", offset)
        jsonObject.addProperty("version", 2)
        jsonObject.addProperty("clientType", 73)
        getGw().getQuoteData(73,"T222381", "his", jsonObject)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                val jsonArray = (result as JsonObject)["data"].asJsonArray
                val list: MutableList<FlashHisBean> = JSON.parseArray(jsonArray.toString(), FlashHisBean::class.java)

                list.forEachIndexed { index, flashHisBean ->
                    if(index == 0){
                        flashHisBean.sumPrice = flashHisBean.orig
                        flashHisBean.avgPrice = flashHisBean.orig
                    }else{
                        flashHisBean.sumPrice = list[index - 1].sumPrice + flashHisBean.orig
                        flashHisBean.avgPrice = flashHisBean.sumPrice/(index + 1)
                    }
                }
                getSuccess(list)
            }) { throwable: Throwable ->
                throwable.printStackTrace()
            }
    }
}