package com.bluethunder.tar2.networking

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val MAP_BASE_URL = "https://mapapi.cloud.huawei.com/"
    private const val NTIFICATIONS_BASE_URL = "https://push-api.cloud.huawei.com/"
    private const val HMS_TOKEN_BASE_URL = "https://oauth-login.cloud.huawei.com/"

    private const val TIME_OUT: Long = 120

    private val gson = GsonBuilder().setLenient().create()

    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(TIME_OUT, TimeUnit.SECONDS)
        .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
        .addInterceptor {
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }.intercept(it)
        }.build()

    val retrofitMap: Api by lazy {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(MAP_BASE_URL)
            .client(okHttpClient)
            .build().create(Api::class.java)
    }

    val retrofitNotification: Api by lazy {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(NTIFICATIONS_BASE_URL)
            .client(okHttpClient)
            .build().create(Api::class.java)
    }


    val retrofitToken: Api by lazy {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(HMS_TOKEN_BASE_URL)
            .client(okHttpClient)
            .build().create(Api::class.java)
    }

}
