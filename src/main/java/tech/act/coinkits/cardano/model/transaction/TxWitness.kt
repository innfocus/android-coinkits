package tech.act.coinkits.cardano.model.transaction

import co.nstant.`in`.cbor.CborBuilder
import co.nstant.`in`.cbor.CborEncoder
import co.nstant.`in`.cbor.model.ByteString
import co.nstant.`in`.cbor.model.DataItem
import java.io.ByteArrayOutputStream

class TxWitness(private val extendedPublicKey: ByteArray, private val signature: ByteArray) {

    fun serializer(): List<DataItem> {
        val output = ByteArrayOutputStream()
        val witness =
            CborBuilder().addArray().add(ByteString(extendedPublicKey)).add(ByteString(signature))
                .end().build()
        CborEncoder(output).encode(witness)
        val tagged = ByteString(output.toByteArray())
        tagged.setTag(24)
        return CborBuilder().addArray().add(0).add(tagged).end().build()
    }

}

fun Array<TxWitness>.serializer(isChunk: Boolean = false): List<DataItem> {
    val ls = mutableListOf<DataItem>()
    forEach {
        val item = it.serializer()
        ls.addAll(item)
    }
    return when (isChunk) {
        true -> {
            val rs = CborBuilder()
            ls.forEach {
                rs.add(it)
            }
            rs.build()
        }
        false -> {
            val rs = CborBuilder().addArray()
            ls.forEach {
                rs.add(it)
            }
            rs.end().build()
        }
    }
}