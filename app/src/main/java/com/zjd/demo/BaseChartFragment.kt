package com.zjd.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.zjd.unite.chart.chart.BaseChart
import com.zjd.unite.chart.entity.BaseQuoteData
import com.zjd.unite.chart.entity.QuoteBean

/**
 * @author ZJD
 * @date 2021/6/30
 **/
abstract class BaseChartFragment<T: BaseQuoteData> : Fragment(), BaseChart.OnDataSelectListener<T> {

    /** 行情id */
    protected var mQuoteId = 0
    /** 周期 */
    protected var mPeriod = 0
    /** 行情 */
    protected lateinit var quoteBean: QuoteBean

    /** 更新数据 */
    abstract fun updateQuote(quote: QuoteBean)
    /** 黄金分割 */
    abstract fun goldenCutEvent(status: Int = 0)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding(inflater, container).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    abstract fun binding(inflater: LayoutInflater, container: ViewGroup?): ViewBinding

    abstract fun init()

    /** 重新请求数据 */
    open fun refreshData(){

    }

    override fun onDataSelect(data: T, showCross: Boolean, crossX: Float) {

    }
}