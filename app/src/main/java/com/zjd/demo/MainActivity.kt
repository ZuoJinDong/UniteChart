package com.zjd.demo

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.FragmentUtils
import com.blankj.utilcode.util.ToastUtils
import com.zjd.demo.databinding.ActivityMainBinding
import com.zjd.demo.frag.*
import com.zjd.unite.chart.entity.QuoteBean
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /** 画线状态 */
    var drawLineStatus = 0

    private val quoteId = 1173

    private var currentFrag: BaseChartFragment<*>? = null

    private var timer = Timer()

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.bind(layoutInflater.inflate(R.layout.activity_main, null))
        setContentView(binding.root)

        binding.cbGold.setOnCheckedChangeListener { compoundButton, b ->
            drawLineStatus = if(b) 1 else 0
            currentFrag?.goldenCutEvent(drawLineStatus)
        }

        timer.scheduleAtFixedRate(object : TimerTask(){
            override fun run() {
                runOnUiThread {
                    currentFrag?.updateQuote(QuoteBean())
                }
            }
        }, 1000, 1000)
    }

    fun onClick(view: View) {
        when(view.id){
            R.id.btn_ts -> changeFrag(ChartTsFragment.newInstance(quoteId))
            R.id.btn_k -> changeFrag(ChartKFragment.newInstance(quoteId))
            R.id.btn_duo -> changeFrag(ChartTsDuoFragment.newInstance(quoteId))
            R.id.btn_flash -> changeFrag(ChartFlashFragment.newInstance(quoteId))
        }
    }

    private fun changeFrag(frag: BaseChartFragment<*>){
        drawLineStatus = 0
        binding.cbGold.isChecked = false
        currentFrag = frag
        FragmentUtils.replace(supportFragmentManager, frag, binding.container.id)
    }
}