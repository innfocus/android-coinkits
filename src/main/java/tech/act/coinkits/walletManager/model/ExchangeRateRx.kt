/*
 * Copyright (c) 2019.
 */

package tech.act.coinkits.walletManager.model

import java.math.BigDecimal

class ExchangeRateRx : CoinRxBaseModel<BigDecimal>() {
    var mBalance : BigDecimal = BigDecimal.ZERO

    fun updateBalance(balance : BigDecimal){
        onNext(balance)
    }
    override fun onNext(data: BigDecimal) {
        emitter?.onNext(data)
    }
}