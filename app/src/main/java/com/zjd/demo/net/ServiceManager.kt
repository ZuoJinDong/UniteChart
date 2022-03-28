package com.zjd.demo.net

/**
 * @author : Zuo JinDong
 * @time : 2020/3/26
 * @desc :
 */
class ServiceManager {
    fun <T> getApi(service: Class<T>, baseUrl: String): T {
        return RetrofitService.getRetrofit(baseUrl).create(service)
    }
}