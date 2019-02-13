/*
 * Copyright (c) 2019.
 */

package tech.act.coinkits.network.networkBuilder

import com.google.gson.JsonObject
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.*
import tech.act.coinkits.bitcoin.model.BitcoinHistory
import tech.act.coinkits.bitcoin.model.UnspentOutput

interface CoinService {
    //BTC service
    @GET("/balance")
    fun btcGetBlance(@Query("active") active : String) : Observable<Response<JsonObject>>
    @FormUrlEncoded
    @POST("/pushtx")
    fun btcSendTransaction(@Field("tx") hashOfTx : String) : Observable<Response<String>>
    @GET("multiaddr")
    fun btcGetTransactionHistory(@Query("active") active: String, @Query("offset") offset : Int, @Query("n") litmit : Int ) : Observable<Response<BitcoinHistory>>
    @GET("/unspent")
    fun btcGetUnspentTransaction(@Query("active") active: String) : Observable<Response<UnspentOutput>>
}