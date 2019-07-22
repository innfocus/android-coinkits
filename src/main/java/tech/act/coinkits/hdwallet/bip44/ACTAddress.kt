package tech.act.coinkits.hdwallet.bip44

import tech.act.coinkits.cardano.model.CarAddress
import tech.act.coinkits.cardano.model.CarPublicKey
import tech.act.coinkits.hdwallet.bip32.ACTCoin
import tech.act.coinkits.hdwallet.bip32.ACTNetwork
import tech.act.coinkits.hdwallet.bip32.ACTPublicKey
import tech.act.coinkits.hdwallet.core.crypto.ACTCryto
import tech.act.coinkits.hdwallet.core.helpers.*

class ACTAddress {

    private var publicKey  : ACTPublicKey?  = null
            var network    : ACTNetwork
    private var addressStr : String?        = null

    constructor(publicKey: ACTPublicKey) {
        this.publicKey  = publicKey
        this.network    = publicKey.network
    }

    constructor(addressStr: String,
                network: ACTNetwork){
        this.addressStr = addressStr
        this.network    = network
    }

    fun raw(): ByteArray? {
        if (publicKey != null) {
            when(publicKey!!.network.coin) {
                ACTCoin.Bitcoin,
                ACTCoin.Ripple -> {
                    return  byteArrayOf(publicKey!!.network.pubkeyhash()) + ACTCryto.sha256ripemd160(publicKey!!.raw!!)
                }
                ACTCoin.Ethereum -> {
                    val pubKeyUnpressedData = ACTCryto.convertToUncompressed(publicKey!!.raw!!)
                    return  ACTCryto.hashSHA3256(pubKeyUnpressedData.dropFirst()).suffix(20)
                }
                ACTCoin.Cardano -> {
                    val pub = CarPublicKey(publicKey!!.raw!!)
                    return CarAddress(pub, publicKey!!.chainCode).raw()
                }
            }
        }else if (((addressStr != null) and (network != null))) {
            when(publicKey!!.network.coin) {
                ACTCoin.Bitcoin -> {
                    return  null
                }
                ACTCoin.Ethereum -> {
                    return addressStr!!.substring(network!!.addressPrefix().length).fromHexToByteArray()
                }
                ACTCoin.Cardano -> {
                    return CarAddress(addressStr = addressStr).raw()
                }
            }

        }
        return null
    }

    fun rawAddressString(): String {
        if (addressStr != null) {
            return  addressStr!!
        }else{
            val r = raw()
            if ((r != null) and (network != null)) {
                return when(network!!.coin) {
                    ACTCoin.Bitcoin,
                    ACTCoin.Ripple -> {
                        val cs = ACTCryto.doubleSHA256(r!!).copyOfRange(0, 4)
                        val type = if (network!!.coin == ACTCoin.Ripple) Base58.Base58Type.Ripple else Base58.Base58Type.Basic
                        network!!.addressPrefix() + Base58.encode(r!! + cs, type)
                    }
                    ACTCoin.Ethereum -> {
                        network!!.addressPrefix() + ACTEIP55.encode(r!!)
                    }
                    ACTCoin.Cardano -> {
                        Base58.encode(r!!)
                    }
                }
            }else{
                return ""
            }
        }
    }
}