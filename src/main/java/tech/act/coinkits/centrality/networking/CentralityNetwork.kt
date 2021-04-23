package tech.act.coinkits.centrality.networking


class CENNZ_API {
    companion object {
        private const val server = "https://service.eks.centralityapp.com"
        const val rpcServer = "https://cennznet.unfrastructure.io/public"
        const val scanAccount = "$server/cennznet-explorer-api/api/scan/account"
        const val scanTransfers = "$server/cennznet-explorer-api/api/scan/transfers"
        const val scanExtrinsic = "$server/cennznet-explorer-api/api/scan/extrinsic"
        const val BASE_UNIT = 10000
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

//    0x6211cb => 6427083
//    [182, 4]
    @ExperimentalUnsignedTypes
    fun makeEraOption(current: Int): UByteArray {
        val result = UByteArray(2)
        val calPeriod = 128
        val quantizedPhase = current % calPeriod
        val encoded = 6 + (quantizedPhase shl 4)

        val first = encoded shr 8
        val second = encoded and 0xff
        result[0] = second.toUByte()
        result[1] = first.toUByte()
        return result
    }
}