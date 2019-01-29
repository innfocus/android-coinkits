package tech.act.coinkits.cardano.networking.models

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import tech.act.coinkits.hdwallet.core.helpers.toDate
import java.util.*

class ADATransaction (
    @SerializedName("hash")
    val transactionID   : String                = "",

    @SerializedName("time")
    private val _time            : String       = "",

    @SerializedName("last_update")
    private val _lastUpdate      : String       = "",

    @SerializedName("block_num")
    private val _blockNumber     : String       = "",

    @SerializedName("tx_state")
    val state                    : String       = "",

    @SerializedName("best_block_num")
    private val _bestBlockNumber : String       = "",

    @SerializedName("inputs_address")
    private val _addressIn : Array<String> ,

    @SerializedName("inputs_amount")
    private val _amountIn : Array<String> ,

    @SerializedName("outputs_address")
    private val _addressOut : Array<String>,

    @SerializedName("outputs_amount")
    private val _amountOut : Array<String>
    )
{
    private var inputsTmp   : Array<ADATransactionInOut>? = null
    private var outputsTmp  : Array<ADATransactionInOut>? = null
    var amount              : Float             = 0.0f
    var fee                 : Float             = 0.0f
    val time                : Date      get()   = _time.toDate()
    val lastUpdate          : Date      get()   = _lastUpdate.toDate()
    val blockNumber         : Long      get()   = (_blockNumber.toLongOrNull() ?: 0)
    val bestBlockNumber     : Long      get()   = (_bestBlockNumber.toLongOrNull() ?: 0)
    var inputs              get()               = inputsTmp ?: ADATransactionInOut.parse(_amountIn    , _addressIn)
                            set(value)          {inputsTmp = value}
    var outputs             get()               = outputsTmp ?: ADATransactionInOut.parse(_amountOut   , _addressOut)
                            set(value)          {outputsTmp = value}
    var data                : ByteArray?        = null

    companion object {
        fun parser(json: JsonElement): Array<ADATransaction> {
            return try {
                Gson().fromJson(json, Array<ADATransaction>::class.java) ?: emptyArray()
            }catch (e: JsonSyntaxException) {
                emptyArray()
            }
        }
    }
}