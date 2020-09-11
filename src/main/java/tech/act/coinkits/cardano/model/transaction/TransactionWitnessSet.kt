package tech.act.coinkits.cardano.model.transaction

import android.util.Log
import co.nstant.`in`.cbor.CborBuilder
import co.nstant.`in`.cbor.CborEncoder
import co.nstant.`in`.cbor.model.DataItem
import java.io.ByteArrayOutputStream

class TransactionWitnessSet(val bootstraps: Array<TxWitness>) {

    fun serializer(): List<DataItem> {
        val rs = CborBuilder().startArray()
        rs.add(2)
        bootstraps.map {
            rs.add(it.serializer().first())
        }
        return rs.end().build()
    }

    fun encode(): ByteArray {
        val baos = ByteArrayOutputStream()
        CborEncoder(baos).nonCanonical().encode(this.serializer())

        val byteData = baos.toByteArray()

        Log.d("TEST_TX", "tx TransactionWitnessSet")
        Log.d("TEST_TX", byteData.toUByteArray().contentToString())
        return byteData
    }
}