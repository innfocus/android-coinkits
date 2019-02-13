/*
 * Copyright (c) 2019.
 */

package tech.act.coinkits.extension

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import tech.act.coinkits.network.networkHelper.NetworkErrorEnum
import tech.act.coinkits.network.networkHelper.NetworkResponse
import java.io.IOException

fun <T> Observable<T>.networkResponse(networkResponse: NetworkResponse<T>): Disposable {
    return this.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                networkResponse.onComplete()
            }
            .subscribe({
                networkResponse.onResponse(it)
            }, {
                networkResponse.onError(it.convert())
            })
}

fun Throwable.convert(): NetworkErrorEnum {
    return when (this) {
        is IOException -> {
            NetworkErrorEnum.NETWORK_NOT_AVAILABLE
        }
        is HttpException -> {
            NetworkErrorEnum.REQUEST_TIMEOUT
        }
        else -> {
            NetworkErrorEnum.UNKNOWN
        }
    }
}