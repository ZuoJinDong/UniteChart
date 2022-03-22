package com.zjd.unite.chart.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.sangame.jjhcps.base.BaseApplication
import com.zjd.unite.chart.R

/**
 * @author ZJD
 * @date 2021/4/1
 * @desc
 **/

fun getColor(context: Context, resId: Int): Int = context.resources.getColor(resId)

/**
 * SVG 转 bitmap
 */
fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
    var drawable = ContextCompat.getDrawable(context, drawableId)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        drawable = DrawableCompat.wrap(drawable!!).mutate()
    }
    val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

@ColorInt
fun getSpecialTxtColor(context: Context, d1: Double): Int {
    if (d1 > 0.0) {
        return ContextCompat.getColor(context, R.color.uc_increase)
    } else if (d1 < 0.0) {
        return ContextCompat.getColor(context, R.color.uc_decrease)
    }
    return ContextCompat.getColor(context, R.color.equal_color)
}


/**
 * 行情数值颜色
 */
fun getQuoteColor(context: Context = BaseApplication.getApp(), offsetPrice: Double, colorId: Int = R.color.text_dark): Int {
    return when {
        offsetPrice < 0 -> context.resources.getColor(R.color.uc_decrease)
        offsetPrice > 0 -> context.resources.getColor(R.color.uc_increase)
        else -> context.resources.getColor(colorId)
    }
}