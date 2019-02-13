/*
 * Copyright (c) 2019.
 */

package tech.act.coinkits.walletManager.helper.exchangeRate

import tech.act.coinkits.walletManager.model.CoinRxBaseModel
import java.math.BigDecimal

data class ExchangeModel(val crySymbol : String,val fiatSymbol:FiatSymbol,val rate : BigDecimal)
enum class FiatSymbol(val value : String){
    VND("VND"),YEN("YEN"),USE("USD")
}
class ExchangeRateRx : CoinRxBaseModel<ArrayList<ExchangeModel>>(){
    var mData = ArrayList<ExchangeModel>()
    override fun onNext(data: ArrayList<ExchangeModel>) {
        emitter?.onNext(data)
    }
    fun updateRate(data: ArrayList<ExchangeModel>){
        mData = data
        onNext(mData)
    }
}