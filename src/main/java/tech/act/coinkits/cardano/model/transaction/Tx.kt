package tech.act.coinkits.cardano.model.transaction

import co.nstant.`in`.cbor.CborBuilder
import co.nstant.`in`.cbor.CborEncoder
import co.nstant.`in`.cbor.model.*
import co.nstant.`in`.cbor.model.Map
import tech.act.coinkits.hdwallet.core.helpers.blake2b
import tech.act.coinkits.hdwallet.core.helpers.toHexString
import java.io.ByteArrayOutputStream

class Tx {
    private var inputs  : MutableList<TxoPointer>   = mutableListOf()
    private var outputs : MutableList<TxOut>        = mutableListOf()

    fun getID(): String {
        return encode().blake2b(32).toHexString()
    }

    fun addInput(input: TxoPointer) {
        inputs.add(input)
    }

    fun addOutput(output: TxOut) {
        outputs.add(output)
    }

    fun getOutTotal(): Long {
        return outputs.map { it.value }.sum()
    }

    fun serializer(): List<DataItem> {
        val rs          = CborBuilder().addArray()
        val insCbor     = inputs.toTypedArray().serializer()
        val outsCbor    = outputs.toTypedArray().serializer()
        insCbor.forEach {
            rs.add(it)
        }
        outsCbor.forEach{
            rs.add(it)
        }
        rs.add(Map(0))
        return rs.end().build()
    }

    fun encode(): ByteArray{
        val builder   = CborBuilder().startArray()
        val insCbor     = inputs.toTypedArray().serializer(true)
        val outsCbor    = outputs.toTypedArray().serializer(true)
        insCbor.forEach {
            builder.add(it)
        }
        builder.end()
        builder.startArray()
        outsCbor.forEach{
            builder.add(it)
        }
        val output = ByteArrayOutputStream()
        CborEncoder(output).encode(builder.end().add(Map(0)).build())
        return byteArrayOf(0x83.toByte()) + output.toByteArray()
    }
}