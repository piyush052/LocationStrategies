package com.piyush052.locationstrategies.network

import com.piyush052.locationstrategies.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NetworkService {

    fun getInstance() : ApiEndPoints{
        val client = OkHttpClient().newBuilder()
//        .cache(cache)
//        .addInterceptor(LastFmRequestInterceptor(apiKey, cacheDuration))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            })
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://ws.audioscrobbler.com")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())

            .build()
        return retrofit.create(ApiEndPoints::class.java)
    }


}
