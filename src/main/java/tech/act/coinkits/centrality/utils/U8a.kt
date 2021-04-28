package tech.act.coinkits.centrality.utils

import java.math.BigInteger

object U8a {
    var MAX_U8 = 2.toBigInteger().pow(8 - 2) - 1.toBigInteger()
    var MAX_U16 = 2.toBigInteger().pow(16 - 2) - 1.toBigInteger()
    var MAX_U32 = 2.toBigInteger().pow(32 - 2) - 1.toBigInteger()

    fun compactAddLength(input: ByteArray): ByteArray {
        val length = input.size
        var rs = compactToU8a(length.toBigInteger())
        rs += input
        return rs
    }

    fun toArrayLikeLE(value: BigInteger, byteLength: Int): ByteArray {
        var q = value
        val res = ByteArray(byteLength)
        var i = 0
        while (q != 0.toBigInteger()) {
            val b = q.and(0xff.toBigInteger())
            q = q shr (8)
            res[i] = b.toByte()
            i += 1
        }
        while (i < byteLength) {
            res[i] = 0;
            i += 1
        }
        return res
    }

    fun compactToU8a(value: BigInteger): ByteArray {
        if (value < MAX_U8) {
            val result = ByteArray(1)
            result[0] = (value shl 2).toByte()
            return result
        } else if (value < MAX_U16) {
            var re = value
            re = re shl 2
            re += 1.toBigInteger()
            return toArrayLikeLE(re, 2)
        } else {
            var re = value
            re = re shl 2
            re += 2.toBigInteger()
            return toArrayLikeLE(re, 4)
        }
    }
}
