package tech.act.coinkits.cardano.model.transaction

import co.nstant.`in`.cbor.CborBuilder
import co.nstant.`in`.cbor.CborEncoder
import co.nstant.`in`.cbor.model.ByteString
import co.nstant.`in`.cbor.model.DataItem
import co.nstant.`in`.cbor.model.UnsignedInteger
import tech.act.coinkits.hdwallet.core.helpers.fromHexToByteArray
import java.io.ByteArrayOutputStream

class TxoPointer(val txId: String, val index: Long) {
    fun serializer(): List<DataItem> {
        val output      = ByteArrayOutputStream()
        val utxoCbor    = CborBuilder().addArray().add(ByteString(txId.fromHexToByteArray())).add(UnsignedInteger(index)).end().build()
        CborEncoder(output).encode(utxoCbor)
        val tagged      = ByteString(output.toByteArray())
        tagged.setTag(24)
        return  CborBuilder().addArray().add(0).add(tagged).end().build()
    }
}

fun Array<TxoPointer>.serializer(isChunk: Boolean = false): List<DataItem> {
    val ls = mutableListOf<DataItem>()
    map { it.serializer()}.forEach {
        ls.addAll(it)
    }
    return when (isChunk) {
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