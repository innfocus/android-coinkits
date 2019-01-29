package tech.act.coinkits.hdwallet.bip32

enum class Change(val value: Int) {
    External(0),
    Internal(1)
}

enum class Algorithm {
    Ed25519,
    Secp256k1
}

enum class ACTCoin {
    Bitcoin{
        override fun nameCoin()     = "Bitcoin"
        override fun symbolName()   = "BTC"
        override fun minimumValue() = 0.00001
        override fun regex()        = "(?:([a-km-zA-HJ-NP-Z1-9]{26,35}))"
        override fun algorithm()    = Algorithm.Secp256k1
    },
    Ethereum{
        override fun nameCoin()     = "Ethereum"
        override fun symbolName()   = "ETH"
        override fun minimumValue() = 0.00001
        override fun regex()        = "(?:((0x|0X|)[a-fA-F0-9]{40,}))"
        override fun algorithm()    = Algorithm.Secp256k1
    },
    Cardano{

        override fun nameCoin()     = "Cardano"
        override fun symbolName()   = "ADA"
        override fun minimumValue() = 0.1
        override fun regex()        = "(?:([a-km-zA-HJ-NP-Z1-9]{25,}))"
        override fun algorithm()    = Algorithm.Ed25519
    };
    abstract fun nameCoin()     : String
    abstract fun symbolName()   : String
    abstract fun minimumValue() : Double
    abstract fun regex()        : String
    abstract fun algorithm()    : Algorithm
}

class ACTNetwork constructor(val coin: ACTCoin, private val isTestNet: Boolean) {

    fun coinType(): Int {
        return when(coin) {
            ACTCoin.Bitcoin     -> 0
            ACTCoin.Ethereum    -> 60
            ACTCoin.Cardano     -> 1815
        }
    }

    fun privateKeyPrefix(): Int {
        return when{
            isTestNet   -> 0x0488ADE4
            else        -> 0x0488ADE4
        }
    }

    fun publicKeyPrefix(): Int {
        return when{
            isTestNet   -> 0x043587cf
            else        -> 0x0488b21e
        }
    }

    fun pubkeyhash(): Byte {
        return when{
            !isTestNet          -> 0x00
            else -> when(coin) {
                ACTCoin.Bitcoin -> 0x6f
                else            -> 0x00
            }
        }
    }

    fun addressPrefix() : String {
       return if (coin == ACTCoin.Ethereum) "0x" else ""
    }

    fun derivationPath(): String {
        return if (coin == ACTCoin.Bitcoin) (coinType().toString() + "'") else ("44'/" + coinType().toString() + "'/0'")
    }

    fun derivateIdxMax(chain: Change): Int {
        return when(coin) {
            ACTCoin.Bitcoin     -> if (chain == Change.Internal) 10 else 100
            ACTCoin.Ethereum    -> if (chain == Change.Internal) 0  else 1
            ACTCoin.Cardano     -> if (chain == Change.Internal) 0  else 50
        }
    }

    fun extendAddresses(chain: Change): Int {
        return when(coin) {
            ACTCoin.Bitcoin     -> if (chain == Change.Internal) 0 else 10
            ACTCoin.Ethereum    -> if (chain == Change.Internal) 0 else 0
            ACTCoin.Cardano     -> if (chain == Change.Internal) 0 else 0
        }
    }

    fun explorer(): String {
        return when(isTestNet) {
            false -> {
                when(coin) {
                    ACTCoin.Bitcoin     -> "https://www.blockchain.com/btc"
                    ACTCoin.Ethereum    -> "https://etherscan.io"
                    ACTCoin.Cardano     -> "https://cardanoexplorer.com"
                }
            }
            true -> {
                when(coin) {
                    ACTCoin.Bitcoin     -> "https://testnet.blockchain.info"
                    ACTCoin.Ethereum    -> "https://ropsten.etherscan.io"
                    ACTCoin.Cardano     -> "https://cardanoexplorer.com"
                }
            }
        }

    }

    fun explorerForTX(): String {
        return explorer() + "/tx/"
    }
}