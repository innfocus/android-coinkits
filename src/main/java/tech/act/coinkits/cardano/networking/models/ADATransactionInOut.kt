package tech.act.coinkits.cardano.networking.models

class ADATransactionInOut {
    var address : String = ""
    var value   : Float = 0.0f
    companion object {
        fun parse(amount    : Array<String>,
                addresses   : Array<String>): Array<ADATransactionInOut> {
            val items = mutableListOf<ADATransactionInOut>()
            if (amount.size == addresses.size) {
                for (idx in 0 until amount.size) {
                    val item        = ADATransactionInOut()
                    item.value      = amount[idx].toFloatOrNull() ?: 0.0f
                    item.address    = addresses[idx]
                    val exist       = items.filter { it.address == item.address }
                    if (exist.isNotEmpty()) {
                        exist.first().value += item.value
                    }else{
                        items.add(item)
                    }
                }
            }
            return items.toTypedArray()
        }
    }
}