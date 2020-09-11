package tech.act.coinkits.cardano.model.transaction

import android.util.Log
import co.nstant.`in`.cbor.CborBuilder
import co.nstant.`in`.cbor.CborEncoder
import co.nstant.`in`.cbor.model.DataItem
import tech.act.coinkits.hdwallet.core.helpers.blake2b
import tech.act.coinkits.hdwallet.core.helpers.toHexString
import java.io.ByteArrayOutputStream

class Tx {
    private var inputs: MutableList<TxoPointer> = mutableListOf()
    private var outputs: MutableList<TxOut> = mutableListOf()
    private var fee: Long = 0
    private var ttl: Long = 0
    private var certs: String? = null
    private var withdrawals: String? = null
    private var update: String? = null
    private var metadataHash: String? = null

    fun getID(): String {
        return encode().blake2b(32).toHexString()
    }

    fun addInput(input: TxoPointer) {
        inputs.add(input)
    }

    fun addOutput(output: TxOut) {
        outputs.add(output)
    }

    fun setFee(fee: Long) {
        this.fee = fee
    }

    fun setTtl(ttl: Long) {
        this.ttl = ttl
    }

    fun getOutTotal(): Long {
        return outputs.map { it.value }.sum()
    }

    fun serializer(): List<DataItem> {
        val rs = CborBuilder().addMap()

        val inputArray = rs.putArray(0)
        val ls = mutableListOf<DataItem>()
        inputs.map { it.serializer() }.forEach {
            ls.addAll(it)
        }
        ls.forEach {
            inputArray.add(it)
        }


        val outputArray = rs.putArray(1)
        val lso = mutableListOf<DataItem>()
        outputs.forEach {
            val item = it.serializer()
            if (item != null) {
                lso.addAll(item)
            }
        }
        lso.forEach {
            outputArray.add(it)
        }

        rs.put(2, this.fee)
        rs.put(3, this.ttl)

        return rs.end().build()
    }

    fun encode(): ByteArray {
        val baos = ByteArrayOutputStream()
        CborEncoder(baos).nonCanonical().encode(this.serializer())
        val byteData = baos.toByteArray()

        Log.d("TEST_TX", "tx body")
        Log.d("TEST_TX", byteData.toUByteArray().contentToString())
        return byteData
    }
}