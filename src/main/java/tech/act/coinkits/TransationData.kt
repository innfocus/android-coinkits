package tech.act.coinkits

import tech.act.coinkits.bitcoin.helper.BTCCoin
import tech.act.coinkits.bitcoin.model.BTCTransactionData
import tech.act.coinkits.bitcoin.model.BTCTransactionInOutData
import tech.act.coinkits.cardano.helpers.ADACoin
import tech.act.coinkits.cardano.networking.models.ADATransaction
import tech.act.coinkits.cardano.networking.models.ADATransactionInOut
import tech.act.coinkits.hdwallet.bip32.ACTCoin
import java.io.Serializable
import java.util.*

class TransationData : Serializable {
    var amount          : Float = 0.0f
    var fee             : Float = 0.0f
    var iD              : String = ""
    var fromAddress     : String = ""
    var toAddress       : String = ""
    var date            : Date = Date()
    var coin            : ACTCoin = ACTCoin.Bitcoin
    var isSend          = false
}

/*
* For Bitcoin
*/

fun Array<BTCTransactionInOutData>.exclude(addresses: Array<String>): Array<BTCTransactionInOutData> {
    val converted = addresses.map {it.toLowerCase()}
    return filter { !converted.contains(it.address.toLowerCase())}.toTypedArray()
}

fun Array<BTCTransactionInOutData>.filter(addresses: Array<String>): Array<BTCTransactionInOutData> {
    val converted = addresses.map {it.toLowerCase()}
    return filter { converted.contains(it.address.toLowerCase())}.toTypedArray()
}

fun Array<BTCTransactionData>.toTransactionDatas(addresses: Array<String>): Array<TransationData> {
    return map { it.toTransactionData(addresses) }.sortedByDescending { it.date }.toTypedArray()
}

fun BTCTransactionData.toTransactionData(addresses: Array<String>): TransationData {
    val result          = TransationData()
    result.iD           = hashString
    val iPs             = inPuts.map{ it.address }.distinct()
    val oPs             = outPuts.map{ it.address }.distinct()
    result.fromAddress  = iPs.joinToString(separator = "\n")
    result.toAddress    = oPs.joinToString(separator = "\n")
    result.date         = timeCreate
    result.amount       = amount/ BTCCoin
    result.fee          = fee   / BTCCoin
    result.coin         = ACTCoin.Bitcoin
    result.isSend       = addresses.filter { result.fromAddress.contains(it, ignoreCase = true)}.isNotEmpty()
    return result
}

/*
* For Cardano
*/

fun Array<ADATransactionInOut>.exclude(addresses: Array<String>): Array<ADATransactionInOut> {
    val converted = addresses.map {it.toLowerCase()}
    return filter { !converted.contains(it.address.toLowerCase())}.toTypedArray()
}

fun Array<ADATransactionInOut>.filter(addresses: Array<String>): Array<ADATransactionInOut> {
    val converted = addresses.map {it.toLowerCase()}
    return filter { converted.contains(it.address.toLowerCase())}.toTypedArray()
}

fun Array<ADATransaction>.toTransactionDatas(addresses: Array<String>): Array<TransationData> {
    return map { it.toTransactionData(addresses) }.sortedByDescending { it.date }.toTypedArray()
}

fun ADATransaction.toTransactionData(addresses: Array<String>): TransationData {
    val result          = TransationData()
    result.iD           = transactionID
    val iPs             = inputs.map{ it.address }.distinct()
    val oPs             = outputs.map{ it.address }.distinct()
    result.fromAddress  = iPs.joinToString(separator = "\n")
    result.toAddress    = oPs.joinToString(separator = "\n")
    result.date         = time
    result.amount       = amount/ ADACoin
    result.fee          = fee   / ADACoin
    result.coin         = ACTCoin.Cardano
    result.isSend       = addresses.filter { result.fromAddress.contains(it, ignoreCase = true)}.isNotEmpty()
    return result
}

/* END */