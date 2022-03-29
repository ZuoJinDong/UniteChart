package com.zjd.unite.chart.event

/** 画线 */
data class DrawLineEvent(val status: Int = 0){
    companion object{
        //关闭
        const val STATUS_CLOSE = 0
        //打开黄金分割
        const val STATUS_OPEN = 1
        //黄金分割有历史
        const val STATUS_STEP = 2
        //黄金分割无历史
        const val STATUS_STEP_NONE = 3
    }
}