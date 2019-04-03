package com.piyush052.locationstrategies.network

import okhttp3.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiEndPoints {

    @GET("/2.0/?method=artist.search")
    fun searchArtist(@Query("artist") artist: String): Call
}