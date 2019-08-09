package tech.act.coinkits.cardano.networking

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import tech.act.coinkits.cardano.helpers.ADACoin
import tech.act.coinkits.cardano.model.CarAddress
import tech.act.coinkits.cardano.model.transaction.*
import tech.act.coinkits.cardano.networking.models.ADATransaction
import tech.act.coinkits.cardano.networking.models.ADAUnspentTransaction
import tech.act.coinkits.exclude
import tech.act.coinkits.filter
import tech.act.coinkits.hdwallet.bip32.ACTPrivateKey
import tech.act.coinkits.hdwallet.bip44.ACTAddress
import tech.act.coinkits.hdwallet.core.helpers.toDateString

class YOROIAPI {
    companion object {
        const val server            = "https://iohk-mainnet.yoroiwallet.com/api/"
        const val utxo              = "txs/utxoForAddresses"
        const val utxoSum           = "txs/utxoSumForAddresses"
        const val history           = "txs/history"
        const val signed            = "txs/signed"
        const val addressUsed       = "addresses/filterUsed"
    }
}

interface ADABalanceHandle          { fun completionHandler(balance: Double, err: Throwable?)}
interface ADATransactionsHandle     { fun completionHandler(transactions:Array<ADATransaction>?, err: Throwable?)}
interface ADASendCoinHandle         { fun completionHandler(transID: String, success: Boolean, errStr: String)}
interface ADAUnspentOutputsHandle   { fun completionHandler(unspentOutputs: Array<ADAUnspentTransaction>, err: Throwable?)}
interface ADAAddressUsedHandle      { fun completionHandler(addressUsed: Array<String>, err: Throwable?)}
interface ADASendTxAuxHandle        { fun completionHandler(transID: String, success: Boolean, errStr: String)}
interface ADACreateTxAuxHandle      { fun completionHandler(txAux: TxAux?, errStr: String)}
interface ADAEstimateFeeHandle      { fun completionHandler(estimateFee: Double, errStr: String)}

private interface IGada {

    @POST(YOROIAPI.utxoSum)
    fun getBalance(@Body params: JsonObject): Call<JsonObject>

    @POST(YOROIAPI.history)
    fun transactions(@Body params: JsonObject): Call<JsonElement>

    @POST(YOROIAPI.utxo)
    fun unspentOutputs(@Body params: JsonObject): Call<JsonElement>

    @POST(YOROIAPI.addressUsed)
    fun addressUsed(@Body params: JsonObject): Call<JsonElement>

    @POST(YOROIAPI.signed)
    fun sendTxAux(@Body params: JsonObject): Call<JsonElement>

    companion object {
        fun create(): IGada {
            val retrofit = Retrofit.Builder()
                .baseUrl(YOROIAPI.server)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(IGada::class.java)
        }
    }
}

class Gada {

    companion object {
        val shared = Gada()
    }
    private val apiService = IGada.create()

    fun getBalance(addresses           : Array<String>,
                   completionHandler   : ADABalanceHandle) {
        val params = JsonObject()
        params.add("addresses", addresses.toJsonArray())
        val call = apiService.getBalance(params)
        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val body = response.body()
                if ((body != null) && (body!!.get("sum") != null) && !body!!.get("sum").isJsonNull) {
                    try {
                        val sum = (body!!.get("sum").asString.toLongOrNull() ?: -1).toDouble() / ADACoin
                        completionHandler.completionHandler(sum, null)
                    }catch (e: ClassCastException) {
                        completionHandler.completionHandler(-1.0, null)
                    }catch (e: IllegalStateException) {
                        completionHandler.completionHandler(-1.0, null)
                    }
                }else{
                    completionHandler.completionHandler(-1.0, null)
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                completionHandler.completionHandler(0.0, t)
            }
        })
    }

    fun transactions(addresses          : Array<String>,
                     dateFrom           : String = "1970-01-01T00:00:00.000Z",
                     transJoin          : Array<ADATransaction> = arrayOf(),
                     ignoreAddsUsed     : Boolean = false,
                     completionHandler  : ADATransactionsHandle) {
        addressUsed(addresses, ignoreAddsUsed, object : ADAAddressUsedHandle {
            override fun completionHandler(addressUsed: Array<String>, err: Throwable?) {
                if (err == null) {
                    if (addressUsed.isNotEmpty()) {
                        val params = JsonObject()
                        params.add("addresses", addressUsed.toJsonArray())
                        params.addProperty("dateFrom", dateFrom)
                        val call = apiService.transactions(params)
                        call.enqueue(object: Callback<JsonElement> {
                            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                                val errBody = response.errorBody()
                                if (errBody != null) {
                                    completionHandler.completionHandler(transJoin, null)
                                }else{
                                    val body = response.body()
                                    if ((body != null) && body!!.isJsonArray) {
                                        val trans = ADATransaction.parser(body!!.asJsonArray)
                                        trans.forEach { tran ->
                                            val outs    = tran.outputs
                                            val ins     = tran.inputs
                                            tran.fee    = ins.map { it.value }.sum() - outs.map { it.value }.sum()
                                            val inputsFilter    = tran.inputs.filter(addressUsed)
                                            val outputsFilter   = tran.outputs.filter(addressUsed)
                                            val outputsexclude  = tran.outputs.exclude(addressUsed)
                                            if (inputsFilter.isNotEmpty()){
                                                tran.inputs  = inputsFilter
                                                tran.outputs = when(outputsexclude.isNotEmpty()) {
                                                                    true    -> outputsexclude
                                                                    false   -> outputsFilter}
                                            }else if (outputsFilter.isNotEmpty()){
                                                tran.outputs = outputsFilter
                                            }
                                            tran.amount = tran.outputs.map { it.value }.sum()
                                        }
                                        trans.sortByDescending { it.lastUpdate }
                                        val sumTrans = arrayOf<ADATransaction>().plus(trans).plus(transJoin)
                                        if (trans.size != 20) {
                                            completionHandler.completionHandler(sumTrans.distinctBy {it.transactionID}
                                                .filter { it.state.toLowerCase() != "failed" }.toTypedArray(), null)
                                        }else{
                                            val last        = sumTrans.first()
                                            val newDateFrom = last.lastUpdate.toDateString()
                                            transactions(addressUsed, newDateFrom, sumTrans, true, completionHandler)
                                        }
                                    }else{
                                        completionHandler.completionHandler(transJoin, null)
                                    }
                                }
                            }

                            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                                completionHandler.completionHandler(null, t)
                            }
                        })
                    }else{
                        completionHandler.completionHandler(transJoin, null)
                    }
                }else{
                    completionHandler.completionHandler(null, err)
                }
            }
        })
    }

    fun calculateEstimateFee(prvKeys           : Array<ACTPrivateKey>,
                             unspentAddresses  : Array<String>,
                             fromAddress       : ACTAddress,
                             toAddressStr      : String,
                             serAddressStr     : String,
                             minerFee          : Double,
                             minFee            : Double,
                             completionHandler : ADAEstimateFeeHandle) {
        createTxAux(prvKeys,
            unspentAddresses,
            fromAddress,
            toAddressStr,
            serAddressStr,
            0.0001,
            0.0001,
            0.0001,
            object  : ADACreateTxAuxHandle {
                override fun completionHandler(txAux: TxAux?, errStr: String) {
                    if (txAux != null) {
                        val estimateFee = (txAux!!.encode().size * minerFee + minFee) / ADACoin
                        completionHandler.completionHandler(estimateFee, "")
                    }else{
                        completionHandler.completionHandler(0.0, errStr)
                    }
                }
            })
    }

    private data class MapKeys(val priKey: ACTPrivateKey, val address: String)
    fun sendCoin(prvKeys           : Array<ACTPrivateKey>,
                 unspentAddresses  : Array<String>,
                 fromAddress       : ACTAddress,
                 toAddressStr      : String,
                 serAddressStr     : String,
                 amount            : Double,
                 networkFee        : Double,
                 serviceFee        : Double,
                 completionHandler : ADASendCoinHandle) {
        createTxAux(prvKeys,
            unspentAddresses,
            fromAddress,
            toAddressStr,
            serAddressStr,
            amount,
            networkFee,
            serviceFee,
            object  : ADACreateTxAuxHandle {
                override fun completionHandler(txAux: TxAux?, errStr: String) {
                    if (txAux != null) {
                        sendTxAux(txAux!!.base64(), txAux!!.tx.getID(), object : ADASendTxAuxHandle {
                            override fun completionHandler(transID: String, success: Boolean, errStr: String) {
                                completionHandler.completionHandler(transID, success, errStr)
                            }
                        })
                    }else{
                        completionHandler.completionHandler("", false, errStr)
                    }
                }
            })
    }

    fun createTxAux(prvKeys           : Array<ACTPrivateKey>,
                    unspentAddresses  : Array<String>,
                    fromAddress       : ACTAddress,
                    toAddressStr      : String,
                    serAddressStr     : String,
                    amount            : Double = 0.0,
                    networkFee        : Double = 0.0,
                    serviceFee        : Double = 0.0,
                    completionHandler  : ADACreateTxAuxHandle) {
        addressUsed(unspentAddresses, completionHandler = object : ADAAddressUsedHandle {
            override fun completionHandler(addressUsed: Array<String>, err: Throwable?) {
                if (err == null) {
                    unspentOutputs(addressUsed, object : ADAUnspentOutputsHandle {
                        override fun completionHandler(unspents: Array<ADAUnspentTransaction>, err: Throwable?) {
                            if (err == null) {
                                var mapKeys = arrayOf<MapKeys>()
                                for (i in 0 until prvKeys.size) {
                                    if (addressUsed.contains(unspentAddresses[i])) {
                                        mapKeys = mapKeys.plus(MapKeys(prvKeys[i], unspentAddresses[i]))
                                    }
                                }
                                var prvKeys     = arrayOf<ByteArray>()
                                var chainCodes  = arrayOf<ByteArray>()
                                val total       = unspents.map{ it.amount }.sum()
                                val serFee      = when(CarAddress.isValidAddress(serAddressStr)) {true -> serviceFee * ADACoin  false -> 0.0}
                                val netFee      = networkFee * ADACoin
                                val amountSend  = amount * ADACoin
                                val change      = (total - amountSend - netFee - serFee).toLong()
                                val tx          = Tx()
                                if (change > netFee) {
                                    val out1 = TxOut(fromAddress.rawAddressString(), change)
                                    tx.addOutput(out1)
                                }

                                val out2        = TxOut(toAddressStr, amountSend.toLong())
                                tx.addOutput(out2)

                                if (serFee > 0) {
                                    val out3    = TxOut(serAddressStr, serFee.toLong())
                                    tx.addOutput(out3)
                                }

                                unspents!!.forEach {
                                    val input = TxoPointer(it.transationHash, it.transactionIdx.toLong())
                                    val add = it.receiver

                                    val keys    = mapKeys.filter{item -> item.address == add}.first()
                                    prvKeys     = prvKeys.plus(keys.priKey.raw!!)
                                    chainCodes  = chainCodes.plus(keys.priKey.chainCode!!)
                                    tx.addInput(input)
                                }

                                val txId        = tx.getID()
                                val inWitnesses = TxWitnessBuilder.builder(txId, prvKeys, chainCodes)
                                val txAux       = TxAux(tx, inWitnesses)

                                completionHandler.completionHandler(txAux, "")
                            }else{
                                completionHandler.completionHandler(null, err!!.localizedMessage)
                            }
                        }
                    })
                }else{
                    completionHandler.completionHandler(null, err!!.localizedMessage)
                }
            }
        })
    }

    fun unspentOutputs(addresses            : Array<String>,
                       completionHandler    : ADAUnspentOutputsHandle) {
        val params = JsonObject()
        params.add("addresses", addresses.toJsonArray())
        val call = apiService.unspentOutputs(params)
        call.enqueue(object : Callback<JsonElement> {
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                val body = response.body()
                if ((body != null) && (body!!.isJsonArray)) {
                    completionHandler.completionHandler(ADAUnspentTransaction.parser(body!!), null)
                }else{
                    completionHandler.completionHandler(arrayOf(), null)
                }
            }
            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                completionHandler.completionHandler(arrayOf(), t)
            }
        })
    }

    fun addressUsed(addresses           : Array<String>,
                    skip                : Boolean = false,
                    completionHandler   : ADAAddressUsedHandle){
        when(skip) {
            true    -> {completionHandler.completionHandler(addresses, null)}
            false   -> {
                val params = JsonObject()
                params.add("addresses", addresses.toJsonArray())
                val call = apiService.addressUsed(params)
                call.enqueue(object : Callback<JsonElement> {
                    override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                        val body = response.body()
                        if ((body != null) && (body!!.isJsonArray)) {
                            val items = body!!.asJsonArray.map { it.asString }
                            completionHandler.completionHandler(items.toTypedArray(), null)
                        }else{
                            completionHandler.completionHandler(arrayOf(), null)
                        }
                    }
                    override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                        completionHandler.completionHandler(arrayOf(), t)
                    }
                })
            }
        }
    }

    fun sendTxAux(signedTx          : String,
                  txId              : String,
                  completionHandler : ADASendTxAuxHandle) {
        val params = JsonObject()
        params.addProperty("signedTx", signedTx)
        val call = apiService.sendTxAux(params)
        call.enqueue(object : Callback<JsonElement> {
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                val errBody = response.errorBody()
                if ((errBody != null)) {
                    val js = JSONObject(errBody.string())
                    val msg = when(js.has("message")) {true -> js.get("message").toString() false -> "ERROR"}
                    completionHandler.completionHandler(txId, false, msg)
                }else{
                    completionHandler.completionHandler(txId, true, "")
                }
            }
            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                completionHandler.completionHandler("", false, t.localizedMessage)
            }
        })
    }

}

private fun Array<String>.toJsonArray(): JsonArray {
    val arrJson = JsonArray()
    forEach { arrJson.add(it) }
    return arrJson
}