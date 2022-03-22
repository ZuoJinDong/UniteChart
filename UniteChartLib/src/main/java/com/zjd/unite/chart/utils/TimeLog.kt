package com.zjd.unite.chart.utils

import android.util.Log

/**
 * @author ZJD
 * @date 2021/5/25
 * @desc
 **/
class TimeLog {

    companion object{
        private var timeStart = 0L

        fun start(tag: String = "TimeLog Start"){
            timeStart = System.currentTimeMillis()
        }

        fun log(tag: String = "TimeLog Log"){
            Log.d(tag,"${System.currentTimeMillis() - timeStart}")
//            timeStart = System.nanoTime()
        }
    }
}