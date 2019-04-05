package com.piyush052.locationstrategies.network

import android.util.Log
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.piyush052.locationstrategies.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class NetworkService {

    fun getInstance(): ApiEndPoints {
        val client = OkHttpClient().newBuilder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            })
            .addInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    val request = chain.request()
                    Log.e("Interceptor", chain.request().url().toString())

                    val url = request.url().toString()
                    val newUrl = url.replace("/?","?")
                    Log.e("New URL ", newUrl)

                    val newRequest = request.newBuilder()
                        .url(newUrl)
                        .build()
                    Log.e("Interceptor", newRequest.url().toString())
                    return chain.proceed(newRequest)
                }

            })
            .connectTimeout(4, TimeUnit.MINUTES)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://api.traxsmart.in:5055")
            .client(client)

            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        return retrofit.create(ApiEndPoints::class.java)
    }
}
