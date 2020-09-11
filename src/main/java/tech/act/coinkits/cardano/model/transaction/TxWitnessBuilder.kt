package tech.act.coinkits.cardano.model.transaction

import tech.act.coinkits.cardano.model.CarKeyPair
import tech.act.coinkits.cardano.model.CarPublicKey
import tech.act.coinkits.hdwallet.core.helpers.fromHexToByteArray

class TxWitnessBuilder {
    companion object {
        fun builder(
            txId: String,
            prvKeys: Array<ByteArray>,
            chainCodes: Array<ByteArray>
        ): Array<TxWitness> {
            return when ((prvKeys.size == chainCodes.size) and txId.fromHexToByteArray()
                .isNotEmpty()) {
                true -> {
                    val rs = mutableListOf<TxWitness>()
                    for (i in prvKeys.indices) {
                        when ((prvKeys[i].size == 64) and (chainCodes[i].size == 32)) {
                            true -> {
                                val pub = CarPublicKey.derive(prvKeys[i])
                                val xPub = pub.bytes() + chainCodes[i]
                                val pairKey = CarKeyPair(pub.bytes(), prvKeys[i])
                                val signature = pairKey.sign(txId.fromHexToByteArray())
                                val witness = TxWitness(
                                    xPub,
                                    signature,
                                    chainCodes[i],
                                    byteArrayOf(0xa0.toByte())
                                )
                                rs.add(witness)
                            }
                            false -> return arrayOf()
                        }
                    }
                    rs.toTypedArray()
                }
                false -> arrayOf()
            }
        }
    }
}