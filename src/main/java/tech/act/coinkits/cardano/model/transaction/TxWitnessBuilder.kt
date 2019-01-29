package tech.act.coinkits.cardano.model.transaction

import co.nstant.`in`.cbor.CborEncoder
import co.nstant.`in`.cbor.model.ByteString
import co.nstant.`in`.cbor.model.UnsignedInteger
import tech.act.coinkits.cardano.model.CarKeyPair
import tech.act.coinkits.cardano.model.CarPublicKey
import tech.act.coinkits.hdwallet.core.helpers.fromHexToByteArray
import java.io.ByteArrayOutputStream

class TxWitnessBuilder {
    companion object {
        val protocolMagic:Long = 764824073
        fun builder(txId: String, prvKeys:Array<ByteArray>, chainCodes:Array<ByteArray>): Array<TxWitness> {
            return when((prvKeys.size == chainCodes.size) and txId.fromHexToByteArray().isNotEmpty()) {
                true -> {
                    var rs = mutableListOf<TxWitness>()
                    for (i in 0 until prvKeys.size) {
                        when((prvKeys[i].size == 64) and (chainCodes[i].size == 32)) {
                            true -> {
                                val output = ByteArrayOutputStream()
                                val pub         = CarPublicKey.derive(prvKeys[i])
                                val xPub        = pub.bytes() + chainCodes[i]
                                val pairKey     = CarKeyPair(pub.bytes(), prvKeys[i])
                                var message     = byteArrayOf()
                                message         += 0x01
                                CborEncoder(output).encode(UnsignedInteger(protocolMagic))
                                message         += output.toByteArray()
                                output.reset()
                                CborEncoder(output).encode(ByteString(txId.fromHexToByteArray()))
                                message         += output.toByteArray()
                                val signature   = pairKey.sign(message)
                                val witness     = TxWitness(xPub, signature)
                                rs.add(witness)
                            }
                            false -> return arrayOf()
                        }
                    }
                    rs.toTypedArray()}
                false -> arrayOf()
            }
        }
    }
}