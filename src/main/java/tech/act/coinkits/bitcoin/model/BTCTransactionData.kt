package tech.act.coinkits.bitcoin.model

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import tech.act.coinkits.cardano.networking.models.ADATransactionInOut
import java.util.*

class BTCTransactionData(
    @SerializedName("ver")
    val ver                 : Int       = 0,
    @SerializedName("time")
    private val _time       : Long      = 0,
    @SerializedName("fee")
    val fee                 : Float     = 0.0f,
    @SerializedName("hash")
    val hashString          : String    = "",
    @SerializedName("tx_index")
    val index               : Int       = 0,
    @SerializedName("size")
    val size                : Int       = 0,
    @SerializedName("vin_sz")
    val vinSZ               : Int       = 0,
    @SerializedName("vout_sz")
    val voutSZ              : Int       = 0,
    @SerializedName("inputs")
    private val _inputs     : JsonElement,
    @SerializedName("out")
    private val _out        : JsonElement
) {
    private var inputsTmp   : Array<BTCTransactionInOutData>? = null
    private var outputsTmp  : Array<BTCTransactionInOutData>? = null
    var amount              : Float         = 0.0f
    val timeCreate          : Date get()    = Date(_time)
    var inPuts              get()           = inputsTmp ?: BTCTransactionInOutData.parser(_inputs)
        set(value)          {inputsTmp = value}
    var outPuts             get()           = outputsTmp ?: BTCTransactionInOutData.parser(_out)
        set(value)          {outputsTmp = value}

    companion object {
        fun parser(json: JsonElement): Array<BTCTransactionData> {
            return try {
                Gson().fromJson(json, Array<BTCTransactionData>::class.java) ?: emptyArray()
            }catch (e: JsonSyntaxException) {
                emptyArray()
            }
        }
    }
}