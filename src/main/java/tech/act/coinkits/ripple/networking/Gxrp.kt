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

class XRPAPI {
    companion object {
        const val server        = "https://data.ripple.com/v2/"
        const val serverTest    = "https://testnet.data.api.ripple.com/v2/"
        const val balance       = "accounts/xxx/balances?currency=XRP"
        const val transactions  = "accounts/xxx/transactions?limit=20&descending=true"
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

interface XRPBalanceHandle  {fun completionHandler(balance: Float, err: Throwable?)}

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
                val body = response.body()
                if ((body != null) && body!!.isJsonObject) {

                }else{
                    completionHandler.completionHandler(-1.0f, null)
                }
            }

            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                completionHandler.completionHandler(0.0f, t)
            }
        })
    }
}