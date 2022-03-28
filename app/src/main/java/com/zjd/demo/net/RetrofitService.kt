package com.zjd.demo.net

import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.X509TrustManager

object RetrofitService {

    private var mOkHttpClient: OkHttpClient

    init {
        val builder = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HeadInterceptor())
            .addInterceptor(generateLogInterceptor())
        sslTrustDebug(builder)
        mOkHttpClient = builder.build()
    }

    /**
     * OkHttpClient初始化
     */
    fun initOkHttpClient(interceptor: Interceptor) {
        mOkHttpClient = mOkHttpClient.newBuilder()
                .addInterceptor(interceptor)
                .build()
    }

    fun initOkHttpClient(okHttpClient: OkHttpClient) {
        mOkHttpClient = okHttpClient
    }

    fun getRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(mOkHttpClient)
                .build()
    }

    private fun sslTrustDebug(builder: OkHttpClient.Builder) {
        // 以Debug模式打包，信任所有证书，方便抓包
        val trustAllCerts = arrayOf<X509TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate?> {
                return arrayOfNulls(0)
            }
        })
        try {
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sslSocketFactory = sslContext.socketFactory
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0])
                .hostnameVerifier { _: String?, _: SSLSession? -> true }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateLogInterceptor(): HttpLoggingInterceptor {
        val loggerInterceptor = HttpLoggingInterceptor()
        loggerInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return loggerInterceptor
    }

    fun getJsonBody(params: Map<String, Any>): RequestBody {
        return Gson().toJson(params).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    }
}