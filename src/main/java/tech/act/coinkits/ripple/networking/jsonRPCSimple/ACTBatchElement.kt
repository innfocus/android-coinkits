package tech.act.coinkits.ripple.networking.jsonRPCSimple

class ACTBatchElement<T> (private val request   : ACTJsonRPCRequest<T>,
                          private val version   : String,
                          private val id        : Int){
    val body: Any get() = {
        var bd:MutableMap<String, Any> = mutableMapOf(  "jsonrpc"   to version,
                                                        "method"    to request.method,
                                                        "id"        to id)
        if (request.parameters != null) {
            bd["params"] = request.parameters!!
        }
        bd
    }
}