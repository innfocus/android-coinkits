package tech.act.coinkits.ripple.networking

import tech.act.coinkits.hdwallet.core.helpers.toHexString
import tech.act.coinkits.ripple.model.XRPAccountInfo
import tech.act.coinkits.ripple.model.XRPSubmitResponse
import tech.act.coinkits.ripple.networking.jsonRPCSimple.ACTClient
import tech.act.coinkits.ripple.networking.jsonRPCSimple.ACTJsonRPCRequest

private class XRPJsonRPCServer {
    companion object {
        const val mainnet  = "https://s1.ripple.com:51234"
        const val testnet  = "https://s.altnet.rippletest.net:51234"
    }
}

class XRPJsonRPC{

    val client: ACTClient

    constructor(testNet: Boolean) {
        val nodeEndpoint    = if (testNet) XRPJsonRPCServer.testnet else XRPJsonRPCServer.mainnet
        this.client         = ACTClient(nodeEndpoint)
    }

    class GetAccountInfo (private val account       : String,
                          private val strict        : Boolean,
                          private val ledgerIndex   : String): ACTJsonRPCRequest<XRPAccountInfo> {
        override var method: String = "account_info"
        override var parameters: Any? = {
            arrayOf(mutableMapOf("account" to account, "strict" to strict, "ledger_index" to ledgerIndex))
        }

        override fun response(resultObject: Any): XRPAccountInfo? {
            return null
        }
    }

    class Submit (private val txBlob: ByteArray): ACTJsonRPCRequest<XRPSubmitResponse> {
        override var method: String = "submit"
        override var parameters: Any? = {
            arrayOf(mutableMapOf("tx_blob" to txBlob.toHexString()))
        }
        override fun response(resultObject: Any): XRPSubmitResponse? {
            return null
        }
    }
}