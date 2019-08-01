package tech.act.coinkits.ripple.networking.jsonRPCSimple

class ACTClient (private val nodeEndpoint   : String,
                 private val version        : String = "2.0") {
    private var idGenerator = ACTIDGenerator()

}