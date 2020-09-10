package tech.act.coinkits.cardano.model.transaction

import android.util.Base64
import co.nstant.`in`.cbor.CborBuilder
import co.nstant.`in`.cbor.CborEncoder
import co.nstant.`in`.cbor.model.DataItem
import java.io.ByteArrayOutputStream

class TxAux(val tx: Tx, val witness: Array<TxWitness>) {

    fun serializer(): List<DataItem> {
        val witnessCbor = witness.serializer()
        val txCbor = tx.serializer()
        val rs = CborBuilder().addArray()
        txCbor.forEach {
            rs.add(it)
        }
        witnessCbor.forEach {
            rs.add(it)
        }
        return rs.end().build()
    }

    fun base64(): String {
        return Base64.encodeToString(encode(), 2)
    }

    fun encode(): ByteArray {
        val output = ByteArrayOutputStream()
        CborEncoder(output).encode(witness.serializer())
        return byteArrayOf(0x82.toByte()) + tx.encode() + output.toByteArray()
    }
}