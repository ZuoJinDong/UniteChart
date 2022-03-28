package com.zjd.demo

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.FragmentUtils
import com.blankj.utilcode.util.ToastUtils
import com.zjd.demo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /** 画线状态 */
    var drawLineStatus = 0

    private val quoteId = 1173

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.bind(layoutInflater.inflate(R.layout.activity_main, null))
        setContentView(binding.root)
    }

    fun onClick(view: View) {
        when(view.id){
            R.id.btn_ts -> changeFrag(ChartTsFragment.newInstance(quoteId))
            R.id.btn_k -> ToastUtils.showShort("K线")
            R.id.btn_duo -> ToastUtils.showShort("多日连续")
            R.id.btn_duo_multi -> ToastUtils.showShort("多日叠加")
            R.id.btn_flash -> ToastUtils.showShort("闪电图")
        }
    }

    private fun changeFrag(frag: Fragment){
        FragmentUtils.replace(supportFragmentManager, frag, binding.container.id)
    }
}