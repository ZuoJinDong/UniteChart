package com.zjd.demo.net

import com.google.gson.JsonObject
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * @author ZJD
 * @date 2021/6/30
 * @desc 行情服务
 **/
interface ServiceGw {

    @POST("/api/v2/{title}/front/app")
    fun getQuoteData(
        @Header("clientType") clientType: Int,
        @Header("type") type: String,
        @Path("title") title: String,
        @Body jsonObject: JsonObject
    ): Observable<JsonObject>
}

fun getGw(): ServiceGw {
    return ServiceManager().getApi(ServiceGw::class.java, "https://api.quheqh.cn")
}