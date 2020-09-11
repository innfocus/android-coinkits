package tech.act.coinkits.cardano.model.transaction

import android.util.Log
import co.nstant.`in`.cbor.CborBuilder
import co.nstant.`in`.cbor.CborEncoder
import co.nstant.`in`.cbor.model.DataItem
import co.nstant.`in`.cbor.model.Map
import co.nstant.`in`.cbor.model.UnsignedInteger
import tech.act.coinkits.hdwallet.core.helpers.blake2b
import tech.act.coinkits.hdwallet.core.helpers.toHexString
import java.io.ByteArrayOutputStream
import java.util.*

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

        var baos = ByteArrayOutputStream()
        val insCbor = inputs.toTypedArray().serializer(false)
        CborEncoder(baos).encode(insCbor)
        var byteData = baos.toByteArray()
//        Log.d("TEST_TX", "input body")
//        Log.d("TEST_TX", byteData.toUByteArray().contentToString())
        rs.put(0, byteData)

        baos = ByteArrayOutputStream()
        val outsCbor = outputs.toTypedArray().serializer(false)
        CborEncoder(baos).encode(outsCbor)
        byteData = baos.toByteArray()
//        Log.d("TEST_TX", "output body")
//        Log.d("TEST_TX", byteData.toUByteArray().contentToString())
        rs.put(1, byteData)

        rs.put(2, this.fee)
        rs.put(3, this.ttl)

        return rs.end().build()
    }

    fun encode(): ByteArray {
        val baos = ByteArrayOutputStream()
        CborEncoder(baos).nonCanonical().encode(this.serializer())

//        val builder = CborBuilder().addArray()
//        builder.add(UnsignedInteger(0))
//        val insCbor = inputs.toTypedArray().serializer(true)
//        val outsCbor = outputs.toTypedArray().serializer(true)
//        insCbor.forEach {
//            builder.add(it)
//        }
//        builder.add(UnsignedInteger(1))
////        builder.startArray()
//        outsCbor.forEach {
//            builder.add(it)
//        }
//        builder.add(UnsignedInteger(2))
//        builder.add(this.fee)
//        builder.add(UnsignedInteger(2))
//        builder.add(this.ttl)
//        val output = ByteArrayOutputStream()
//        CborEncoder(output).encode(builder.end().add(Map(4)).build())
//        val test = byteArrayOf(0xa0.toByte()) + output.toByteArray()
//        Log.d("TEST_TX", "tx body test")
//        Log.d("TEST_TX", test.toUByteArray().contentToString())

        val byteData = baos.toByteArray()

        Log.d("TEST_TX", "tx body")
        Log.d("TEST_TX", byteData.toUByteArray().contentToString())
        return byteData
    }
}