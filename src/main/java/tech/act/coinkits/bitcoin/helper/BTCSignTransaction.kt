/*
 * Copyright (c) 2019.
 */

package tech.act.coinkits.bitcoin.helper

import tech.act.coinkits.bitcoin.model.UnspentOutputsItem
import tech.act.coinkits.walletManager.model.CoinDataTransfer

interface ISignTransaction{
    fun signTransaction(coinDataTransfer: CoinDataTransfer, unspentData : List<UnspentOutputsItem>, isMainNet : Boolean) : String
}
class BTCSignTransaction {
    companion object {
        private val singTransactionTool : ISignTransaction = SignTransactionByBitcoinJ()
        fun singTransaction(coinDataTransfer: CoinDataTransfer,unspentData : List<UnspentOutputsItem>,isMainNet: Boolean) : String{
            return singTransactionTool.signTransaction(coinDataTransfer,unspentData,isMainNet)
        }
    }
}