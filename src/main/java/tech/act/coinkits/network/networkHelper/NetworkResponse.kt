/*
 * Copyright (c) 2019.
 */

package tech.act.coinkits.network.networkHelper

interface NetworkResponse<T> {
    fun onRequest(){}
    fun onComplete(){}
    fun onError(t: NetworkErrorEnum){}
    fun onResponse(t: T)
}