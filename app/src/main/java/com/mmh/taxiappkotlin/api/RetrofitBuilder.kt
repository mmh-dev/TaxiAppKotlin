package com.mmh.taxiappkotlin.api

import com.mmh.taxiappkotlin.utils.APPLICATION_ID
import com.mmh.taxiappkotlin.utils.CONTENT_TYPE
import com.mmh.taxiappkotlin.utils.REST_API_KEY
import com.mmh.taxiappkotlin.utils.SERVER_URL
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitBuilder {

    private val requestInterceptor = Interceptor { chain ->
        val request = chain.request()
            .newBuilder()
            .addHeader("X-Parse-Application-Id", APPLICATION_ID)
            .addHeader("X-Parse-REST-API-Key", REST_API_KEY)
            .addHeader("Content-Type", CONTENT_TYPE)
            .build()
        return@Interceptor chain.proceed(request)
    }

    private val httpLoggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    private val client = OkHttpClient.Builder()
        .addInterceptor(requestInterceptor)
        .addInterceptor(httpLoggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val builder: Retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

    val api: Api = builder.create(Api::class.java)

}