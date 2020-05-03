package tech.act.coinkits.ripple.networking

import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap
import retrofit2.http.Url
import tech.act.coinkits.CoinsManager
import tech.act.coinkits.hdwallet.bip32.ACTCoin
import tech.act.coinkits.hdwallet.bip32.ACTPrivateKey
import tech.act.coinkits.hdwallet.bip44.ACTAddress
import tech.act.coinkits.ripple.model.*
import tech.act.coinkits.ripple.model.transaction.XRPMemo
import tech.act.coinkits.ripple.model.transaction.XRPTransactionRaw

class XRPAPI {
    companion object {
        const val server        = "https://data.ripple.com/v2/"
        const val serverTest    = "https://testnet.data.api.ripple.com/v2/"
        const val balance       = "accounts/xxx/balances?currency=XRP"
        const val transactions  = "accounts/xxx/transactions?limit=20&type=Payment"
    }
}

private interface IGxrp {
    @GET
    fun getBalance(@Url url: String): Call<JsonElement>

    @GET("accounts/{address}/transactions")
    fun transactions(@Path("address") address: String, @QueryMap options: Map<String, String>): Call<JsonElement>

    companion object {
        fun create(server: String): IGxrp {
            val retrofit = Retrofit.Builder()
                .baseUrl(server)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(IGxrp::class.java)
        }
    }
}

interface XRPBalanceHandle      {   fun completionHandler(balance: Double, err: Throwable?)}
interface XRPTransactionsHandle {   fun completionHandler(transactions:XRPTransaction?, err: Throwable?)}
interface XRPSubmitTxtHandle    {   fun completionHandler(transID: String, sequence: Int?, success: Boolean, errStr: String)}

class Gxrp {
    companion object {
        val shared = Gxrp()
    }
    private val apiService      = IGxrp.create(XRPAPI.server)
    private val apiTestService  = IGxrp.create(XRPAPI.serverTest)

    private fun getService(): IGxrp {
        val nw = CoinsManager.shared.currentNetwork(ACTCoin.Ripple) ?: return apiService
        return when (nw.isTestNet) {
            true -> apiTestService
            false -> apiService
        }
    }

    fun getBalance(address          : String,
                   completionHandler: XRPBalanceHandle)
    {
        val url = XRPAPI.balance.replace("xxx", address)
        val call = getService().getBalance(url)
        call.enqueue(object: Callback<JsonElement> {
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                val body = response.body() ?: return completionHandler.completionHandler(-1.0, null)
                if (body.isJsonObject) {
                    val balancesJson = body.asJsonObject["balances"]
                    if (balancesJson.isJsonArray) {
                        val balances = XRPBalance.parser(balancesJson.asJsonArray)
                        if (balances.isNotEmpty()) {
                            try {
                                val onlyXRP =
                                    balances.filter { it.currency.toLowerCase() == ACTCoin.Ripple.symbolName().toLowerCase() }.map { it.value }
                                return completionHandler.completionHandler(onlyXRP.first(), null)
                            }catch (e: NoSuchElementException){
                                return completionHandler.completionHandler(-1.0, null)
                            }

                        }
                    }
                }
                completionHandler.completionHandler(-1.0, null)
            }

            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                completionHandler.completionHandler(0.0, t)
            }
        })
    }

    fun getTransactions(address          : String,
                        marker           : String,
                        completionHandler: XRPTransactionsHandle) {
        val data = HashMap<String, String>()
        data["limit"] = "100"
        data["type"] = "Payment"
        data["descending"] = "true"
        if (marker.isNotEmpty()) {
            data["marker"] = marker
        }
        val call = getService().transactions(address, data)
        call.enqueue(object : Callback<JsonElement> {
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                val body = response.body()
                if (body!!.isJsonObject) {
                    completionHandler.completionHandler(XRPTransaction.parser(body.asJsonObject), null)
                }else{
                    completionHandler.completionHandler(null, null)
                }
            }

            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                completionHandler.completionHandler(null, t)
            }
        })
    }

    fun sendCoin(prvKey             : ACTPrivateKey,
                 address            : ACTAddress,
                 toAddressStr       : String,
                 amount             : Double,
                 memo               : XRPMemo?  = null,
                 sequence           : Int?      = null,
                 completionHandler  : XRPSubmitTxtHandle) {
        val nw      = prvKey.network
        val account = address.rawAddressString()
        val jsonRPC = XRPJsonRPC(nw.isTestNet)
        jsonRPC.getAccountInfo(account, object : XRPAccountInfoHandle {
            override fun completionHandler(accInfo: XRPAccountInfo?, err: Throwable?) {
                val acc = accInfo ?: return completionHandler.completionHandler("", null, false, err?.localizedMessage ?: "")
                val balance             = acc.accountData.balance.toDoubleOrNull() ?: 0.0
                // Around = 10 unit of XRP
//                if (balance < ((amount + nw.coin.feeDefault() + nw.coin.minimumValue()) * XRPCoin) - 10) {
                if (balance < ((amount + nw.coin.feeDefault() + nw.coin.minimumAmount()) * XRPCoin)) {
                    return completionHandler.completionHandler("", null, false, "Insufficient Funds")
                }
                val tranRaw             = XRPTransactionRaw()
                tranRaw.account         = account
                tranRaw.destination     = toAddressStr
                tranRaw.sequence        = sequence ?: acc.accountData.sequence
                /* Expire this transaction if it doesn't execute within ~5 minutes: "maxLedgerVersionOffset": 75 */
                tranRaw.lastLedgerSeq   = acc.ledgerIndex + 75
                tranRaw.amount          = amount * XRPCoin
                tranRaw.fee             = nw.coin.feeDefault() * XRPCoin
                tranRaw.memo            = memo
                val signed = tranRaw.sign(prvKey) ?: return completionHandler.completionHandler("", tranRaw.sequence, false, err?.localizedMessage ?: "")
                jsonRPC.submit(signed.txBlob, object : XRPSubmitHandle {
                    override fun completionHandler(submitRes: XRPSubmitResponse?, err: Throwable?) {
                        val res = submitRes ?: return completionHandler.completionHandler("", tranRaw.sequence, false, err?.localizedMessage ?: "")
                        val msg = res.engineResultMessage + "|" + res.engineResult + "|" + res.engineResultCode
                        completionHandler.completionHandler(signed.transactionID, tranRaw.sequence, res.engineResultCode == 0, msg)
                    }
                })
            }
        })
    }
}