package tech.act.coinkits.cardano.model.transaction

import co.nstant.`in`.cbor.CborBuilder
import co.nstant.`in`.cbor.CborDecoder
import co.nstant.`in`.cbor.CborException
import co.nstant.`in`.cbor.model.DataItem
import tech.act.coinkits.hdwallet.core.helpers.Base58

class TxOut(val address: String, val value: Long) {
    fun serializer(): List<DataItem>? {
        return try {
            val addData = Base58.decode(address)
            val addCbor = CborDecoder.decode(addData)
            val rs = CborBuilder().addArray()
            addCbor.forEach {
                rs.add(it)
            }
            rs.add(value)
            rs.end().build()
        } catch (e: CborException) {
            null
        }
    }
}

fun Array<TxOut>.serializer(isChunk: Boolean = false): List<DataItem> {
    val ls = mutableListOf<DataItem>()
    forEach {
        val item = it.serializer()
        if (item != null) {
            ls.addAll(item)
        }
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