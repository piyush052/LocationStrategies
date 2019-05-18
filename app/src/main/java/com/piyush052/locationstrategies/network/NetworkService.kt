package com.piyush052.locationstrategies.network

import android.util.Log
import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.piyush052.locationstrategies.BuildConfig
import com.piyush052.locationstrategies.service.NetworkResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

class NetworkService {

    var api: ApiEndPoints? = null

    fun getInstance(): ApiEndPoints {
        if (api == null) {
            val client = OkHttpClient().newBuilder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor(object : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        val request = chain.request()
                        Log.e("Interceptor", chain.request().url().toString())

                        val url = request.url().toString()
                        val newUrl = url.replace("/?", "?")
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
        } else {
            return api as ApiEndPoints
        }
    }

    fun callLoginApi(
        request: com.piyush052.locationstrategies.network.Request<String>,
        networkResponse: NetworkResponse<String>
    ) {
        getInstance().sendDataToServer("https://j1rd28gar0.execute-api.ap-southeast-1.amazonaws.com/dev/organizations/5cd14207ff66d228b60586b4")
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe() {
                request.response = Gson().toJson(it);
                networkResponse.onNetworkResponse(request)
                Log.e("API res ", Gson().toJson(it))
            }
    }
}
