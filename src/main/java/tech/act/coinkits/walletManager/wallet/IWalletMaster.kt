/*
 * Copyright (c) 2019.
 */

package tech.act.coinkits.walletManager.wallet

import tech.act.coinkits.SendCoinHandle
import tech.act.coinkits.TransactionsHandle
import tech.act.coinkits.hdwallet.bip32.ACTNetwork
import tech.act.coinkits.hdwallet.bip44.ACTAddress
import tech.act.coinkits.hdwallet.bip44.ACTHDWallet
import tech.act.coinkits.walletManager.model.BalanceDataRx
import tech.act.coinkits.walletManager.model.CoinDataTransfer
import tech.act.coinkits.walletManager.model.EstimateFeeData

interface IWalletMaster {
    fun getHDWallet     (): ACTHDWallet?
    fun firstAddress    (): ACTAddress?
    fun addresses       (): Array<ACTAddress>?
    fun getBalance      (address : String):BalanceDataRx
    fun getTransactions (completionHandler: TransactionsHandle)
    fun sendCoin        (transaction : String,completionHandler : SendCoinHandle)
    fun signTransaction(coinDataTransfer: CoinDataTransfer) : String {
        return ""
    }
    fun triggerRefreshBalance()
    fun interruptRefreshBalance()
    fun estimateFee      (serAddressStr     : String,
                          paramFee          : Double,
                          networkMinFee     : Double = 0.0,
                          network           : ACTNetwork,
                          completionHandler : EstimateFeeData)
}