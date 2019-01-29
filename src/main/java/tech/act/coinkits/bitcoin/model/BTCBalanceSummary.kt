package tech.act.coinkits.bitcoin.model

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName

class BTCBalanceSummary(
    @SerializedName("final_balance")
    var finalBalance: Float     = 0.0f,
    @SerializedName("n_tx")
    var txNumber: Int           = 0,
    @SerializedName("total_received")
    var totalReceived: Float    = 0.0f
) {
    var address: String = ""

    companion object {
        fun parser(json: JsonElement): BTCBalanceSummary? {
            return try {
                Gson().fromJson(json, BTCBalanceSummary::class.java)
            }catch (e: JsonSyntaxException) {
                null
            }
        }
    }
}