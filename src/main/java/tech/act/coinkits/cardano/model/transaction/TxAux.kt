package tech.act.coinkits.cardano.model.transaction

import android.util.Base64
import co.nstant.`in`.cbor.CborBuilder
import co.nstant.`in`.cbor.CborEncoder
import co.nstant.`in`.cbor.model.DataItem
import co.nstant.`in`.cbor.model.SimpleValue
import java.io.ByteArrayOutputStream

class TxAux(val tx: Tx, private val witnessSet: TransactionWitnessSet) {

//    body: &TransactionBody
//    witness_set: &TransactionWitnessSet
//    metadata: Option<TransactionMetadata>

    fun serializer(): List<DataItem> {
        val witnessCbor = witnessSet.serializer()
        val txCbor = tx.serializer()
        val rs = CborBuilder().addArray()
        txCbor.forEach {
            rs.add(it)
        }
        witnessCbor.forEach {
            rs.add(it)
        }
        rs.add(SimpleValue.NULL)
        return rs.end().build()
    }

    fun base64(): String {
        return Base64.encodeToString(encode(), 2)
    }

    fun encode(): ByteArray {
        val output = ByteArrayOutputStream()
        CborEncoder(output).encode(witnessSet.serializer())
        return byteArrayOf(0x82.toByte()) + tx.encode() + output.toByteArray()
    }
}