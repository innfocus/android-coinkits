package tech.act.coinkits.hdwallet.core.crypto

import org.spongycastle.asn1.sec.SECNamedCurves
import org.spongycastle.crypto.digests.KeccakDigest
import org.spongycastle.crypto.digests.RIPEMD160Digest
import org.spongycastle.crypto.digests.SHA512Digest
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.spongycastle.crypto.macs.HMac
import org.spongycastle.crypto.params.ECDomainParameters
import org.spongycastle.crypto.params.KeyParameter
import tech.act.coinkits.hdwallet.core.helpers.sha256
import java.math.BigInteger

class ACTCryto {
    companion object {

        private val params      = SECNamedCurves.getByName("secp256k1")
        private val ecParams    = ECDomainParameters(params.curve, params.g, params.n,  params.h)

        fun hmacSHA512(key  : ByteArray,
                       data : ByteArray): ByteArray? {
            val digest  = SHA512Digest()
            val hMac    = HMac(digest)
            hMac.init(KeyParameter(key))
            hMac.update(data, 0, data.count())
            val resBuf  = ByteArray(digest.digestSize)
            hMac.doFinal(resBuf, 0)
            return resBuf
        }

        fun pbkdf2SHA512(password    : ByteArray,
                         salt        : ByteArray,
                         iterations  : Int = 2048,
                         keyLength   : Int = 64): ByteArray {
            val generator = PKCS5S2ParametersGenerator(SHA512Digest())
            generator.init(password, salt, iterations)
            val rs = generator.generateDerivedMacParameters(keyLength * 8) as KeyParameter
            return rs.key
        }

        fun ripemd160(data: ByteArray): ByteArray {
            val digest = RIPEMD160Digest()
            val resBuf = ByteArray(digest.digestSize)
            digest.update(data, 0, data.count())
            digest.doFinal(resBuf, 0)
            return resBuf
        }

        fun sha256ripemd160(data: ByteArray): ByteArray {
            return ripemd160(data.sha256())
        }

        fun doubleSHA256(data: ByteArray): ByteArray {
            return data.sha256().sha256()
        }

        fun hashSHA3256(data: ByteArray): ByteArray{
            val digest  = KeccakDigest(256)
            digest.reset()
            digest.update(data, 0, data.count())
            val resBuf  = ByteArray(digest.digestSize)
            digest.doFinal(resBuf, 0)
            return resBuf
        }

        fun generatePublicKey(priKey: ByteArray, compressed: Boolean): ByteArray {
            val priv = BigInteger(1, priKey)
            return ecParams.g.multiply(priv).getEncoded(compressed)
        }

        fun convertToUncompressed(publicKey: ByteArray): ByteArray {
            return params.curve.decodePoint(publicKey).getEncoded(false)
        }
    }
}