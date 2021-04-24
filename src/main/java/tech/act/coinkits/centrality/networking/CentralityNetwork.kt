package tech.act.coinkits.centrality.networking

import android.util.Log
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import tech.act.coinkits.centrality.model.*


class CENNZ_API {
    companion object {
        const val server = "https://service.eks.centralityapp.com"
        const val rpcServer = "https://cennznet.unfrastructure.io"
        const val scanAccount = "/cennznet-explorer-api/api/scan/account"
        const val scanTransfers = "/cennznet-explorer-api/api/scan/transfers"
        const val scanExtrinsic = "/cennznet-explorer-api/api/scan/extrinsic"
        const val BASE_UNIT = 10000
    }
}

interface CennzGetBalanceHandle {
    fun completionHandler(balance: Long, error: String)
}

interface CennzGetTransactionsHandle {
    fun completionHandler(transactions: List<CennzTransfer>, error: String)
}

interface CennzSubmitExtrinsicHandle {
    fun completionHandler(extrinsicHash: String, success: Boolean, error: String)
}

private interface CentralityApiServices {

    @POST(CENNZ_API.rpcServer)
    fun state_getRuntimeVersion(@Body params: JSONObject): Call<JSONObject>

    @POST(CENNZ_API.rpcServer)
    fun chain_getHeader(@Body params: JSONObject): Call<JSONObject>

    @POST(CENNZ_API.rpcServer)
    fun chain_getFinalizedHead(@Body params: JSONObject): Call<JSONObject>

    @POST(CENNZ_API.rpcServer)
    fun chain_getBlockHash(@Body params: JSONObject): Call<JSONObject>

    @POST(CENNZ_API.rpcServer)
    fun payment_queryInfo(@Body params: JSONObject): Call<JSONObject>

    @POST(CENNZ_API.rpcServer)
    fun system_accountNextIndex(@Body params: JSONObject): Call<JSONObject>

    @POST("/public")
    fun submitExtrinsic(@Body params: JSONObject): Call<JSONObject>

    @POST(CENNZ_API.scanExtrinsic)
    fun scanExtrinsic(@Body params: JsonObject): Call<JsonElement>

    @POST(CENNZ_API.scanTransfers)
    fun transactions(@Body params: JsonObject): Call<CentralityAppResponse<ScanTransfer>>

    @POST(CENNZ_API.scanAccount)
    fun scanAccount(@Body params: JsonObject): Call<CentralityAppResponse<ScanAccount>>


    companion object {
        fun create(): CentralityApiServices {

            val client = OkHttpClient.Builder()
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(CENNZ_API.server)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(CentralityApiServices::class.java)
        }
    }
}

class CentralityNetwork {

    companion object {
        val shared = CentralityNetwork()
        var specVersion = 39
        var transactionVersion = 5
        var genesisHash = ""
        var blockHash = ""
        var mortalLength = 65
        var current = 6425936
    }

    private val apiService = CentralityApiServices.create()

    //    0x6211cb => 6427083
    fun convertNumber(number: String): Long {
        return number.toLong(radix = 16)
    }

    //    [182, 4]
    fun makeEraOption(current: Int): ByteArray {
        val result = ByteArray(2)
        val calPeriod = 128
        val quantizedPhase = current % calPeriod
        val encoded = 6 + (quantizedPhase shl 4)

        val first = encoded shr 8
        val second = encoded and 0xff
        result[0] = second.toByte()
        result[1] = first.toByte()
        return result
    }

    fun submitExtrinsic(
        hash: String,
        completionHandler: CennzSubmitExtrinsicHandle
    ) {
        val params = JSONArray()
        params.put(hash)

        val payload = JSONObject()
        payload.put("id", 1)
        payload.put("jsonrpc", "2.0")
        payload.put("method", "author_submitExtrinsic")
        payload.put("params", params)

//        val payload = JsonObject()
//        payload.addProperty("id", 1)
//        payload.addProperty("jsonrpc", "2.0")
//        payload.addProperty("method", "author_submitExtrinsic")
//        payload.put("params", params)

        val call = apiService.submitExtrinsic(payload)
        call.enqueue(object : Callback<JSONObject> {
            override fun onResponse(call: Call<JSONObject>, response: Response<JSONObject>) {
                Log.d("SENDING_CENNZ", response.toString())
                Log.d("SENDING_CENNZ", response.body().toString())
                val errorBody = response.errorBody()
                if ((errorBody != null)) {
                    val json = JSONObject(errorBody.string())
                    val error = json.getJSONObject("error")
                    val message = error.getString("message")
                    val code = error.getInt("code")
                    completionHandler.completionHandler(
                        "",
                        false,
                        "$message - Error code: $code - Tx: $hash"
                    )
                } else {
                    completionHandler.completionHandler("", true, "")
                }
            }

            override fun onFailure(call: Call<JSONObject>, t: Throwable) {
                completionHandler.completionHandler("", false, t.localizedMessage)
            }
        })
    }

    fun scanAccount(
        address: String,
        assetId: Int,
        completionHandler: CennzGetBalanceHandle
    ) {

        val payload = JsonObject()
        payload.addProperty("address", address)

        val call = apiService.scanAccount(payload)
        call.enqueue(object : Callback<CentralityAppResponse<ScanAccount>> {
            override fun onResponse(
                call: Call<CentralityAppResponse<ScanAccount>>,
                response: Response<CentralityAppResponse<ScanAccount>>
            ) {
                if (response.isSuccessful) {
                    val data: CentralityAppResponse<ScanAccount>? = response.body()
                    if (data != null) {
                        val check = data.data!!.balances.iterator()
                        while (check.hasNext()) {
                            val asset = check.next()
                            if (asset.assetID == assetId) {
                                return completionHandler.completionHandler(asset.free, "")
                            }
                        }
                        completionHandler.completionHandler(0, "")
                    } else {
                        completionHandler.completionHandler(0, "")
                    }

                } else {
                    val errorBody = response.errorBody()
                    if ((errorBody != null)) {
                        val json = JSONObject(errorBody.string())
                        val message = json.getString("message")
                        completionHandler.completionHandler(
                            0,
                            message
                        )
                    } else {
                        completionHandler.completionHandler(0, "")
                    }
                }
            }

            override fun onFailure(call: Call<CentralityAppResponse<ScanAccount>>, t: Throwable) {
                completionHandler.completionHandler(0, t.localizedMessage)
            }
        })
    }

    fun transactions(
        address: String,
        assetId: Int,
        row: Int = 100,
        page: Int = 0,
        completionHandler: CennzGetTransactionsHandle
    ) {

        val payload = JsonObject()
        payload.addProperty("address", address)
        payload.addProperty("row", row)
        payload.addProperty("page", page)

        val call = apiService.transactions(payload)
        call.enqueue(object : Callback<CentralityAppResponse<ScanTransfer>> {
            override fun onResponse(
                call: Call<CentralityAppResponse<ScanTransfer>>,
                response: Response<CentralityAppResponse<ScanTransfer>>
            ) {
                if (response.isSuccessful) {
                    val data: CentralityAppResponse<ScanTransfer>? = response.body()
                    if (data != null) {
                        if (data.data != null) {
                            val transfers = data.data!!.transfers.filter { it.assetID == assetId }
                            completionHandler.completionHandler(transfers, "")
                        } else {
                            completionHandler.completionHandler(emptyList(), "")
                        }
                    } else {
                        completionHandler.completionHandler(emptyList(), "")
                    }

                } else {
                    val errorBody = response.errorBody()
                    if ((errorBody != null)) {
                        val json = JSONObject(errorBody.string())
                        val message = json.getString("message")
                        completionHandler.completionHandler(
                            emptyList(),
                            message
                        )
                    } else {
                        completionHandler.completionHandler(emptyList(), "")
                    }
                }
            }

            override fun onFailure(call: Call<CentralityAppResponse<ScanTransfer>>, t: Throwable) {
                completionHandler.completionHandler(emptyList(), t.localizedMessage)
            }
        })
    }
}