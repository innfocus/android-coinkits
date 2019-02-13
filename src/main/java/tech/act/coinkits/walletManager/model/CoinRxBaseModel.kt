/*
 * Copyright (c) 2019.
 */

package tech.act.coinkits.walletManager.model

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers

abstract class CoinRxBaseModel<T> {
    var emitter: ObservableEmitter<T>? = null
    private var notifier: Observable<T> = Observable.create<T> {
        emitter = it
    }
    fun onMainUI(): Observable<T> {
        return notifier.observeOn(AndroidSchedulers.mainThread())
    }
    fun onError(e: Throwable) {
        emitter?.onError(e)
    }
    abstract fun onNext(data : T)
    fun onComplete() {
        emitter?.onComplete()
    }
}