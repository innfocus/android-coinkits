package tech.act.coinkits.cardano.model.transaction

import co.nstant.`in`.cbor.CborBuilder
import co.nstant.`in`.cbor.model.ByteString
import co.nstant.`in`.cbor.model.DataItem
import co.nstant.`in`.cbor.model.UnsignedInteger
import tech.act.coinkits.hdwallet.core.helpers.fromHexToByteArray

class TxoPointer(private val txId: String, val index: Long) {
    fun serializer(): List<DataItem> {
        val utxoCbor = CborBuilder().addArray().add(ByteString(txId.fromHexToByteArray()))
            .add(UnsignedInteger(index)).end().build()
        return utxoCbor
    }
}

fun Array<TxoPointer>.serializer(isChunk: Boolean = false): List<DataItem> {
    val ls = mutableListOf<DataItem>()
    map { it.serializer() }.forEach {
        ls.addAll(it)
    }
    when (isChunk) {
        true -> {
            val rs = CborBuilder()
            ls.forEach {
                rs.add(it)
            }
            return rs.build()
        }
        false -> {
            val rs = CborBuilder().addArray()
            ls.forEach {
                rs.add(it)
            }
            return rs.end().build()
        }
    }

}