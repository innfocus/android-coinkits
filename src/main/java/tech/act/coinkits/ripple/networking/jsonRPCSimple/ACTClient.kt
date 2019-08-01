package tech.act.coinkits.ripple.networking.jsonRPCSimple

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

private interface IRPCJson {
    @POST
    fun sendTo(@Body params: JsonObject): Call<JsonElement>

    companion object {
        fun create(server: String): IRPCJson {
            val retrofit = Retrofit.Builder()
                .baseUrl(server)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(IRPCJson::class.java)
        }
    }
}

interface RPCJSONHandle<T>{ fun completionHandler(response: T, errStr: String)}
class ACTClient
{
    private val nodeEndpoint    : String
    private val version         : String
    private val service         : IRPCJson
    private var idGenerator     = ACTIDGenerator()

    constructor(nodeEndpoint   : String,
                version        : String = "2.0") {
        this.nodeEndpoint   = nodeEndpoint
        this.version        = version
        this.service        = IRPCJson.create(this.nodeEndpoint)
    }

    fun <T> send(request            : ACTJsonRPCRequest<T>,
                 completionHandler  : RPCJSONHandle<T>) {
        val r       = ACTBatchElement(request, version, idGenerator.next())
        val params  = request.parameters
    }
}