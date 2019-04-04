package com.piyush052.locationstrategies.network

import retrofit2.Call
import retrofit2.http.*

interface ApiEndPoints {
    @GET
    fun sendDataToServer(
        @Url id:String
//        @Query("timestamp") timestamp:Long,
//        @Query("lat") lat:Double,
//        @Query("lon") lon:Double,
//        @Query("bearing") bearing:Double,
//        @Query("accuracy") accuracy:Int


    ): Call<Any>
}