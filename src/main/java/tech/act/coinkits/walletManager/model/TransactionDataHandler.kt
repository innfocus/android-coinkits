/*
 * Copyright (c) 2019.
 */

package tech.act.coinkits.walletManager.model

import tech.act.coinkits.TransationData

class TransactionDataHandler : CoinRxBaseModel<Array<TransationData>>() {
    override fun onNext(data: Array<TransationData>) {

    }
}