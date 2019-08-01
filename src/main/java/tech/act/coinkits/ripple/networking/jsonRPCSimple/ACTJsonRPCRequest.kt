package tech.act.coinkits.ripple.networking.jsonRPCSimple

interface ACTJsonRPCRequest<T> {
    var method      : String
    var parameters  : Any?
    fun response(resultObject: Any): T?
}