package tech.act.coinkits.ripple.networking

import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import tech.act.coinkits.CoinsManager
import tech.act.coinkits.hdwallet.bip32.ACTCoin
import tech.act.coinkits.ripple.model.XRPBalance
import tech.act.coinkits.ripple.model.XRPTransaction
import java.math.BigDecimal

class XRPAPI {
    companion object {
        const val server        = "https://data.ripple.com/v2/"
        const val serverTest    = "https://testnet.data.api.ripple.com/v2/"
        const val balance       = "accounts/xxx/balances?currency=XRP"
        const val transactions  = "accounts/xxx/transactions?limit=20&descending=true"
        val XRP_TO_DROP   = BigDecimal(1000000)
    }
}

private interface IGxrp {
    @GET
    fun getBalance(@Url url: String): Call<JsonElement>

    @GET
    fun transactions(@Url url: String): Call<JsonElement>

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

interface XRPBalanceHandle      {   fun completionHandler(balance: Float, err: Throwable?)}
interface XRPTransactionsHandle {   fun completionHandler(transactions:XRPTransaction?, err: Throwable?)}

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
                val body = response.body() ?: return completionHandler.completionHandler(-1.0f, null)
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
                                return completionHandler.completionHandler(-1.0f, null)
                            }

                        }
                    }
                }
                completionHandler.completionHandler(-1.0f, null)
            }

            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                completionHandler.completionHandler(0.0f, t)
            }
        })
    }

    fun getTransactions(address          : String,
                        market           : String,
                        completionHandler: XRPTransactionsHandle) {
        var url = XRPAPI.transactions.replace("xxx", address)
        if (market.isNotEmpty()) {
            url +=  "&marker=" + market
        }
        val call = getService().transactions(url)
        call.enqueue(object : Callback<JsonElement> {
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                val body = response.body()
                if (body!!.isJsonObject) {
                    completionHandler.completionHandler(XRPTransaction.parser(body!!.asJsonObject), null)
                }else{
                    completionHandler.completionHandler(null, null)
                }
            }

            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                completionHandler.completionHandler(null, t)
            }
        })
    }
}