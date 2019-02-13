/*
 * Copyright (c) 2019.
 */

package tech.act.coinkits.bitcoin.wallet

import android.content.Context
import tech.act.coinkits.SendCoinHandle
import tech.act.coinkits.TransactionsHandle
import tech.act.coinkits.bitcoin.helper.BTCSignTransaction
import tech.act.coinkits.bitcoin.model.UnspentOutputsItem
import tech.act.coinkits.hdwallet.bip32.ACTNetwork
import tech.act.coinkits.hdwallet.bip44.ACTAddress
import tech.act.coinkits.hdwallet.bip44.ACTHDWallet
import tech.act.coinkits.walletManager.helper.privateData.PlutoKeyStore
import tech.act.coinkits.walletManager.model.BalanceDataRx
import tech.act.coinkits.walletManager.model.CoinDataTransfer
import tech.act.coinkits.walletManager.model.EstimateFeeData
import tech.act.coinkits.walletManager.wallet.IWalletMaster
import java.math.BigDecimal

class BTCWallet : IWalletMaster {
    companion object {
        private var instance : BTCWallet? = null
        private var mnemonic = ""
        fun getInstance(context: Context) : BTCWallet{
            if (instance != null){
                 mnemonic = PlutoKeyStore.getPhraseString(context)
                 instance = BTCWallet()
            }
            return instance!!
        }
    }
    override fun getHDWallet(): ACTHDWallet? {
        return ACTHDWallet(mnemonic)
    }

    var mBalanceData = BalanceDataRx()
    override fun getBalance(address: String): BalanceDataRx {
        return mBalanceData
    }

    override fun triggerRefreshBalance(){
        //...
        mBalanceData.updateBalance(BigDecimal.ONE)
    }

    override fun interruptRefreshBalance() {

    }

    override fun firstAddress(): ACTAddress? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addresses(): Array<ACTAddress>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTransactions(completionHandler: TransactionsHandle) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun signTransaction(coinDataTransfer: CoinDataTransfer): String {
        val unspentList = ArrayList<UnspentOutputsItem>()
        return BTCSignTransaction.singTransaction(coinDataTransfer,unspentList,false)
    }

    override fun sendCoin(transaction: String, completionHandler: SendCoinHandle) {

    }

    override fun estimateFee(serAddressStr: String, paramFee: Double, networkMinFee: Double, network: ACTNetwork, completionHandler: EstimateFeeData) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}