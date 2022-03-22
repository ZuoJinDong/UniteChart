package com.zjd.unite.chart.utils

import android.content.res.Resources

/**
 * @author ZJD
 * @date 2021/4/1
 * @desc
 **/

fun dp2px(dpValue: Float): Float = dpValue * Resources.getSystem().displayMetrics.density + 0.5f

fun sp2px(spValue: Float): Float = spValue * Resources.getSystem().displayMetrics.scaledDensity + 0.5f
