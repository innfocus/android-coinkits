/*
 * Copyright (c) 2019.
 */

package tech.act.coinkits.bitcoin.helper

import tech.act.coinkits.bitcoin.model.UnspentOutputsItem
import tech.act.coinkits.walletManager.model.CoinDataTransfer

class SignTransactionByBitcoinJ : ISignTransaction {
    override fun signTransaction(coinDataTransfer: CoinDataTransfer, unspentData: List<UnspentOutputsItem>, isMainNet: Boolean): String {
        return  ""
    }

}