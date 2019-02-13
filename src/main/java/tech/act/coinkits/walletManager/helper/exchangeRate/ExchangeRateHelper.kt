/*
 * Copyright (c) 2019.
 */

package tech.act.coinkits.walletManager.helper.exchangeRate

class ExchangeRateHelper {
    val exchangeModel = ExchangeRateRx()
    fun getExchangeRate() : ExchangeRateRx{
        return exchangeModel
    }
    fun triggerRefreshExchangeRate(){
        //exchangeModel.updateRate()
    }
    fun interruptRefreshExchangeRate(){

    }
}