package com.piyush052.locationstrategies.network

import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface ApiEndPoints {
    @GET
    fun sendDataToServer(
        @Url url : String
        //@QueryMap fullUrl:HashMap<String,Any>
//        @Query("timestamp") timestamp:Long,
//        @Query("lat") lat:Double,
//        @Query("lon") lon:Double,
//        @Query("bearing") bearing:Double,
//        @Query("accuracy") accuracy:Int


    ): Observable<Any>
}