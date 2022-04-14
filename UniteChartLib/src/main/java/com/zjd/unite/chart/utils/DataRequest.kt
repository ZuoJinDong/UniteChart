package com.zjd.unite.chart.utils

import android.content.Context
import com.blankj.utilcode.util.PathUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zjd.unite.chart.entity.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.nio.charset.Charset

/**
 * 模拟网络请求
 */

object DataRequest {
    private fun getStringFromAssert(context: Context, fileName: String): String {
        try {
            val `in` = context.resources.assets.open(fileName)
            val length = `in`.available()
            val buffer = ByteArray(length)
            `in`.read(buffer)
            return String(buffer, 0, buffer.size, Charset.defaultCharset())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    val dayPath = PathUtils.getExternalStoragePath() + "/Test/day.json"
    val tsPath = PathUtils.getExternalStoragePath() + "/Test/ts.json"
    val quotePath = PathUtils.getExternalStoragePath() + "/Test/quote.json"
    val ratePath = PathUtils.getExternalStoragePath() + "/Test/rate.json"
    val trendPath = PathUtils.getExternalStoragePath() + "/Test/SymbolTrend.json"
    val calendarPath = PathUtils.getExternalStoragePath() + "/Test/Calendar.json"
    val tracePath = PathUtils.getExternalStoragePath() + "/Test/TradeTrace.json"

    private fun readFile(path: String): String{
        var result = ""
        var fileReader: FileReader? = null
        var bufferedReader: BufferedReader? = null
        try {
            fileReader = FileReader(File(path))
            bufferedReader = BufferedReader(fileReader)
            try {
                var read: String? = null
                while ({ read = bufferedReader.readLine();read }() != null) {
                    result += read
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            bufferedReader?.close()
            fileReader?.close()
        }
        return result
    }

    fun getALLKLine(page: Int = 0, complete: ((maxVolume: Double) -> Unit)?): MutableList<KLineData> {
        val size = 1000
        val kDatas = try {
            val hisBean = Gson().fromJson<KHisData>(
                    readFile(dayPath),
                    object : TypeToken<KHisData>() {}.type
            )

//            hisBean.pointList.let {
//                DataFormatHelper.calculateK(it).apply {
//                    complete?.invoke(this)
//                }
//                it
//            }

            hisBean.pointList
        } catch (e: Exception) {
            e.printStackTrace()
            mutableListOf()
        }
        var startIndex = kDatas.size - (page + 1)*size
        if(startIndex < 0){
            startIndex = 0
        }

        var endIndex = kDatas.size - page*size
        if(endIndex < 0){
            endIndex = 0
        }
        return kDatas.subList(startIndex, endIndex)
    }

    fun getALLTsLine(list: MutableList<TsHisBean>, quoteId: Int, contractSize: Int): MutableList<TsHisBean> {
        return try {
            list.forEach {
                it.quoteId = quoteId
                DataFormatHelper.calculateTs(it.pointList, quoteId, contractSize)
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            mutableListOf()
        }
    }

    interface OnRequestResult<T>{
        fun onSuccess(result: T)
        fun onError()
    }
}


