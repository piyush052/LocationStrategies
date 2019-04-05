package com.piyush052.locationstrategies.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface ApiEndPoints {
    @POST("./")
    fun sendDataToServer(
        @QueryMap fullUrl:HashMap<String,Any>
//        @Query("timestamp") timestamp:Long,
//        @Query("lat") lat:Double,
//        @Query("lon") lon:Double,
//        @Query("bearing") bearing:Double,
//        @Query("accuracy") accuracy:Int


    ): Call<Any>
}