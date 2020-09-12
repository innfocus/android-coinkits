package tech.act.coinkits.cardano.model.transaction

import co.nstant.`in`.cbor.CborBuilder
import co.nstant.`in`.cbor.CborException
import co.nstant.`in`.cbor.model.DataItem
import tech.act.coinkits.hdwallet.core.helpers.Base58

class TxOut(val address: String, val value: Long) {
    fun serializer(): List<DataItem>? {
        return try {
            val addData = Base58.decode(address)
            val rs = CborBuilder().addArray()
            rs.add(addData)
            rs.add(value)
            rs.end().build()
        } catch (e: CborException) {
            null
        }
    }
}