/*
 * Copyright (c) 2019.
 */

package tech.act.coinkits.walletManager.wallet

import android.content.Context
import tech.act.coinkits.bitcoin.wallet.BTCWallet
import tech.act.coinkits.hdwallet.bip32.ACTCoin


class WalletHandler {
    companion object {
        private var instance : IWalletMaster? = null
        fun getCoin(context: Context,symbols: String) : IWalletMaster?{
           return when(symbols.toLowerCase()){
                ACTCoin.Bitcoin.symbolName().toLowerCase() ->{
                     BTCWallet.getInstance(context)
                }
                else ->{
                    throw(Throwable("$symbols is not supported"))
                }
            }
        }

        fun getAllWallet(){

        }

        fun getTotalBalance(){

        }
    }

}