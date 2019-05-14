package com.piyush052.locationstrategies.service

import com.piyush052.locationstrategies.network.Request


interface NetworkResponse<T> {

    fun onNetworkResponse(request: Request<T>)

    fun onNetworkError(request: Request<T>)
}