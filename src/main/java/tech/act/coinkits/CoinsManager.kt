package tech.act.coinkits

import tech.act.coinkits.bitcoin.model.BTCTransactionData
import tech.act.coinkits.bitcoin.networking.BTCBalanceHandle
import tech.act.coinkits.bitcoin.networking.BTCTransactionsHandle
import tech.act.coinkits.bitcoin.networking.Gbtc
import tech.act.coinkits.cardano.networking.*
import tech.act.coinkits.cardano.networking.models.ADATransaction
import tech.act.coinkits.hdwallet.bip32.ACTCoin
import tech.act.coinkits.hdwallet.bip32.ACTNetwork
import tech.act.coinkits.hdwallet.bip32.ACTPrivateKey
import tech.act.coinkits.hdwallet.bip32.Change
import tech.act.coinkits.hdwallet.bip39.ACTBIP39Exception
import tech.act.coinkits.hdwallet.bip44.ACTAddress
import tech.act.coinkits.hdwallet.bip44.ACTHDWallet
import tech.act.coinkits.ripple.model.XRPTransaction
import tech.act.coinkits.ripple.model.transaction.XRPMemo
import tech.act.coinkits.ripple.networking.Gxrp
import tech.act.coinkits.ripple.networking.XRPBalanceHandle
import tech.act.coinkits.ripple.networking.XRPSubmitTxtHandle
import tech.act.coinkits.ripple.networking.XRPTransactionsHandle
import kotlin.math.acos

interface BalanceHandle         { fun completionHandler(balance: Float, success: Boolean)}
interface TransactionsHandle    { fun completionHandler(transactions:Array<TransationData>?, moreParam: String, errStr: String)}
interface SendCoinHandle        { fun completionHandler(transID: String, success: Boolean, errStr: String)}
interface EstimateFeeHandle     { fun completionHandler(estimateFee: Double, errStr: String)}

interface ICoinsManager {
    fun getHDWallet     (): ACTHDWallet?
    fun setNetworks     (networks: Array<ACTNetwork>)
    fun currentNetwork  (coin: ACTCoin) : ACTNetwork?
    fun cleanAll        ()
    fun firstAddress    (coin: ACTCoin): ACTAddress?
    fun addresses       (coin: ACTCoin): Array<ACTAddress>?
    fun getBalance      (coin: ACTCoin, completionHandler: BalanceHandle)
    fun getTransactions (coin: ACTCoin, moreParam: String = "", completionHandler: TransactionsHandle)
    fun sendCoin        (fromAddress       : ACTAddress,
                         toAddressStr      : String,
                         serAddressStr     : String,
                         amount            : Double,
                         networkFee        : Double,
                         serviceFee        : Double,
                         networkMemo       : MemoData? = null,
                         completionHandler : SendCoinHandle)

    fun estimateFee      (serAddressStr     : String,
                          paramFee          : Double,
                          networkMinFee     : Double = 0.0,
                          network           : ACTNetwork,
                          completionHandler : EstimateFeeHandle)
}

class CoinsManager: ICoinsManager {
    companion object {
        val shared = CoinsManager()
    }
    private var hdWallet            : ACTHDWallet? = null
    private var prvKeysManager      = mutableMapOf<String, Array<ACTPrivateKey>>()
    private var extendPrvKeysNumber = mutableMapOf<String, Int>()
    private var addressesManager    = mutableMapOf<String, Array<ACTAddress>>()
    private var coinsSupported      = arrayListOf(ACTCoin.Bitcoin, ACTCoin.Ethereum, ACTCoin.Cardano, ACTCoin.Ripple)
    private var networkManager      = mutableMapOf( ACTCoin.Bitcoin.symbolName()    to ACTNetwork(ACTCoin.Bitcoin   , true),
                                                    ACTCoin.Ethereum.symbolName()   to ACTNetwork(ACTCoin.Ethereum  , true),
                                                    ACTCoin.Cardano.symbolName()    to ACTNetwork(ACTCoin.Cardano   , false),
                                                    ACTCoin.Ripple.symbolName()    to ACTNetwork(ACTCoin.Ripple   , true))
            var mnemonicRecover     = ""
            var mnemonic            = ""

    override fun getHDWallet(): ACTHDWallet? {
        return when (hdWallet != null) {
            true -> hdWallet
            false -> {
                try {
                    hdWallet = ACTHDWallet(mnemonic)
                    hdWallet
                } catch (e: ACTBIP39Exception) {
                    null
                }
            }
        }
    }

    override fun setNetworks(networks: Array<ACTNetwork>) {
        /* Store mnemonic before clean data */
        val mn = mnemonic
        cleanAll()
        /* Restore mnemonic */
        mnemonic = mn
        coinsSupported.clear()
        networkManager.clear()
        networks.forEach {
            val coin = it.coin
            coinsSupported.add(coin)
            networkManager[coin.symbolName()] = it
        }
    }

    override fun currentNetwork(coin: ACTCoin): ACTNetwork? {
        return networkManager[coin.symbolName()]
    }

    override fun cleanAll() {
        hdWallet        = null
        mnemonic        = ""
        mnemonicRecover = ""
        addressesManager.clear()
        extendPrvKeysNumber.clear()
        prvKeysManager.clear()
    }

    override fun firstAddress(coin: ACTCoin): ACTAddress? {
        val adds = addresses(coin)
        return if ((adds != null) && adds.isNotEmpty()) {
            adds.first()
        } else {
            null
        }
    }

    override fun addresses(coin: ACTCoin): Array<ACTAddress>? {
        return when(coinsSupported.contains(coin)) {
            false   -> null
            true    -> {
                val symbolName  = coin.symbolName()
                val adds        = addressesManager[symbolName]
                when (adds != null) {
                    true    -> adds
                    false   -> {
                        val prvKeys = privateKeys(coin)
                        if (prvKeys != null) {
                            addressesManager[symbolName] = prvKeys.map { ACTAddress(it.publicKey()) }.toTypedArray()
                            addressesManager[symbolName]
                        }else{
                            null
                        }
                    }
                }
            }
        }
    }

    override fun getBalance(coin: ACTCoin, completionHandler: BalanceHandle) {
        val adds = addresses(coin)
        if ((adds != null) && adds.isNotEmpty()) {
            when(coin) {
                ACTCoin.Bitcoin -> {
                    getBTCBalance(adds, completionHandler)
                }
                ACTCoin.Ethereum -> {
                    getETHBalance(adds.first(), completionHandler)
                }
                ACTCoin.Cardano -> {
                    getADABalance(adds, completionHandler)
                }
                ACTCoin.Ripple -> {
                    getXRPBalance(adds.first(), completionHandler)
                }
            }
        }else{
            completionHandler.completionHandler(0.0f, false)
        }
    }

    override fun getTransactions(coin: ACTCoin, moreParam: String, completionHandler: TransactionsHandle) {
        val adds = addresses(coin)
        if ((adds != null) && adds.isNotEmpty()) {
            when (coin) {
                ACTCoin.Bitcoin -> {
                    getBTCTransactions(adds, completionHandler)
                }
                ACTCoin.Ethereum -> {
                    getETHTransactions(adds.first(), completionHandler)
                }
                ACTCoin.Cardano -> {
                    getADATransactions(adds, completionHandler)
                }
                ACTCoin.Ripple -> {
                    getXRPTransactions(adds.first(), moreParam, completionHandler)
                }
            }
        } else {
            completionHandler.completionHandler(arrayOf(), "", "")
        }
    }

    override fun estimateFee(serAddressStr      : String,
                             paramFee           : Double,
                             networkMinFee      : Double,
                             network            : ACTNetwork,
                             completionHandler  : EstimateFeeHandle)
    {
        when(network.coin) {
            ACTCoin.Bitcoin     -> {completionHandler.completionHandler(0.0, "TO DO")   }
            ACTCoin.Ethereum    -> { completionHandler.completionHandler(0.0, "TO DO")  }
            ACTCoin.Cardano     -> {
                val prvKeys     = privateKeys(ACTCoin.Cardano)  ?: arrayOf()
                val addresses   = addresses(ACTCoin.Cardano)    ?: arrayOf()
                if (prvKeys.isNotEmpty() and addresses.isNotEmpty() and (prvKeys.size == addresses.size)) {
                    val unspentAddresses = addresses.map { it.rawAddressString() }.toTypedArray()
                    Gada.shared.calculateEstimateFee(prvKeys,
                        unspentAddresses,
                        addresses.first(),
                        addresses.first().rawAddressString(),
                        serAddressStr,
                        paramFee,
                        networkMinFee,
                        object : ADAEstimateFeeHandle {
                            override fun completionHandler(estimateFee: Double, errStr: String) {
                                completionHandler.completionHandler(estimateFee, errStr)
                            }
                        })
                }else{
                    completionHandler.completionHandler(0.0, "Error")
                }
            }
        }
    }

    override fun sendCoin(fromAddress       : ACTAddress,
                          toAddressStr      : String,
                          serAddressStr     : String,
                          amount            : Double,
                          networkFee        : Double,
                          serviceFee        : Double,
                          networkMemo       : MemoData?,
                          completionHandler : SendCoinHandle) {
        when(fromAddress.network.coin) {
            ACTCoin.Bitcoin -> {
                sendBTCCoin(
                    fromAddress,
                    toAddressStr,
                    serAddressStr,
                    amount,
                    networkFee,
                    serviceFee,
                    completionHandler)
            }
            ACTCoin.Ethereum -> {
                sendETHCoin(
                    fromAddress,
                    toAddressStr,
                    serAddressStr,
                    amount,
                    networkFee,
                    serviceFee,
                    completionHandler)
            }
            ACTCoin.Cardano -> {
                sendADACoin(
                    fromAddress,
                    toAddressStr,
                    serAddressStr,
                    amount,
                    networkFee,
                    serviceFee,
                    completionHandler)
            }
            ACTCoin.Ripple -> {
                sendXRPCoin(
                    fromAddress,
                    toAddressStr,
                    serAddressStr,
                    amount,
                    networkFee,
                    serviceFee,
                    networkMemo,
                    completionHandler)
            }
        }
    }

    /*
    * Private methods
    */
    private fun privateKeys(coin: ACTCoin): Array<ACTPrivateKey>? {
        val symbolName  = coin.symbolName()
        val extName     = symbolName + Change.External.value.toString()
        val inName      = symbolName + Change.Internal.value.toString()
        val extPrvKeys  = prvKeysManager[extName]   ?: arrayOf()
        val inPrvKeys   = prvKeysManager[inName]    ?: arrayOf()

        val checkExt    = checkExtendPrvKeys(Change.External, coin)
        val checkIn     = checkExtendPrvKeys(Change.Internal, coin)

        return when (checkExt.isNeed or checkIn.isNeed) {
            false -> (extPrvKeys).plus(inPrvKeys)
            true -> {
                val wallet = getHDWallet()
                when (wallet != null) {
                    false   -> null
                    true    -> {
                        val nw = networkManager[symbolName]
                        if (nw != null) {
                            val keys: ACTHDWallet.Result        = wallet!!.generatePrivateKeys( checkExt.count,
                                                                                                checkExt.fromIdx,
                                                                                                checkIn.count,
                                                                                                checkIn.fromIdx,
                                                                                                nw!!)
                            prvKeysManager[checkExt.keyName]    = extPrvKeys.plus(keys.extKeys)
                            prvKeysManager[checkIn.keyName]     = inPrvKeys.plus(keys.intKeys)
                            prvKeysManager[checkExt.keyName]!!.plus(prvKeysManager[checkIn.keyName]!!)
                        }else{
                            null
                        }
                    }
                }

            }
        }
    }

    private data class Result(val isNeed: Boolean, val fromIdx: Int, val count: Int, val keyName: String)
    private fun checkExtendPrvKeys(change: Change, coin: ACTCoin): Result {
        val symbolName  = coin.symbolName()
        val keyName     = symbolName + change.value.toString()
        val nw          = networkManager[symbolName]
        val prvKeys     = prvKeysManager[keyName]       ?: arrayOf()
        var extNum      = extendPrvKeysNumber[keyName]  ?: 0
        return when(nw != null) {
            false   -> Result(false, 0, 0, keyName)
            true    -> {
                val begin = nw!!.derivateIdxMax(change)
                val total = begin + extNum
                val count = total - prvKeys.count()
                Result(count > 0, prvKeys.count(), count, keyName)
            }
        }
    }

    private fun getBTCBalance(addresses: Array<ACTAddress>, completionHandler: BalanceHandle) {
        val adds = addresses.map { it.rawAddressString() }
        if (adds.isNotEmpty()) {
            Gbtc.shared.getBalance(adds.toTypedArray(), object : BTCBalanceHandle {
                override fun completionHandler(balance: Float, err: Throwable?) {
                    if ((err != null) or (balance < 0)) {
                        completionHandler.completionHandler(0.0f, false)
                    } else {
                        completionHandler.completionHandler(balance, true)
                    }
                }
            })
        } else {
            completionHandler.completionHandler(0.0f, false)
        }
    }

    private fun getETHBalance(address: ACTAddress, completionHandler: BalanceHandle) {
        completionHandler.completionHandler(0.0f, false)
    }

    private fun getADABalance(addresses: Array<ACTAddress>, completionHandler: BalanceHandle) {
        val adds = addresses.map { it.rawAddressString() }
        if (adds.isNotEmpty()) {
            Gada.shared.getBalance(adds.toTypedArray(), object : ADABalanceHandle {
                override fun completionHandler(balance: Float, err: Throwable?) {
                    if ((err != null) or (balance < 0)) {
                        completionHandler.completionHandler(0.0f, false)
                    } else {
                        completionHandler.completionHandler(balance, true)
                    }
                }
            })
        } else {
            completionHandler.completionHandler(0.0f, false)
        }
    }

    private fun getXRPBalance(address: ACTAddress, completionHandler: BalanceHandle) {
        val addString = address.rawAddressString()
        Gxrp.shared.getBalance(addString, object : XRPBalanceHandle {
            override fun completionHandler(balance: Float, err: Throwable?) {
                if ((err != null) or (balance < 0)) {
                    completionHandler.completionHandler(0.0f, false)
                } else {
                    completionHandler.completionHandler(balance, true)
                }
            }
        })
    }

    private fun getBTCTransactions(addresses: Array<ACTAddress>, completionHandler: TransactionsHandle) {
        val adds = addresses.map { it.rawAddressString() }
        if (adds.isNotEmpty()) {
            Gbtc.shared.transactions(adds.toTypedArray(), object : BTCTransactionsHandle {
                override fun completionHandler(transactions: Array<BTCTransactionData>, err: Throwable?) {
                    completionHandler.completionHandler(transactions.toTransactionDatas(adds.toTypedArray()), "", err?.localizedMessage
                            ?: "Error")
                }
            })
        } else {
            completionHandler.completionHandler(null, "", "Error")
        }
    }

    private fun getETHTransactions(address: ACTAddress, completionHandler: TransactionsHandle) {
        completionHandler.completionHandler(arrayOf(), "", "TO DO")
    }

    private fun getADATransactions(addresses: Array<ACTAddress>, completionHandler: TransactionsHandle) {
        val adds = addresses.map { it.rawAddressString() }
        if (adds.isNotEmpty()) {
            Gada.shared.addressUsed(adds.toTypedArray(), completionHandler = object : ADAAddressUsedHandle {
                override fun completionHandler(addressUsed: Array<String>, err: Throwable?) {
                    Gada.shared.transactions(addressUsed,
                            ignoreAddsUsed = true,
                            completionHandler = object : ADATransactionsHandle {
                                override fun completionHandler(transactions: Array<ADATransaction>?, err: Throwable?) {
                                    if (transactions != null) {
                                        completionHandler.completionHandler(transactions.toTransactionDatas(addressUsed), "", "")
                                    } else {
                                        completionHandler.completionHandler(null, "", err?.localizedMessage
                                                ?: "Error")
                                    }
                                }
                            })
                }
            })
        } else {
            completionHandler.completionHandler(null, "", "Error")
        }
    }

    private fun getXRPTransactions(address: ACTAddress, moreParam: String, completionHandler: TransactionsHandle) {
        Gxrp.shared.getTransactions(address.rawAddressString(), moreParam, object : XRPTransactionsHandle {
            override fun completionHandler(transactions: XRPTransaction?, err: Throwable?) {
                if (transactions != null) {
                    val trans = transactions.transactions!!.toTransactionDatas(address.rawAddressString())
                    if (transactions.marker == null) {
                        completionHandler.completionHandler(trans, "", "")
                    } else {
                        completionHandler.completionHandler(trans, transactions.marker, "")
                    }
                } else {
                    completionHandler.completionHandler(null, "", err?.localizedMessage ?: "Error")
                }
            }
        })
    }

    private fun sendBTCCoin(fromAddress       : ACTAddress,
                            toAddressStr      : String,
                            serAddressStr     : String,
                            amount            : Double,
                            networkFee        : Double,
                            serviceFee        : Double,
                            completionHandler : SendCoinHandle) {
        completionHandler.completionHandler("", false, "TO DO")
    }

    private fun sendETHCoin(fromAddress       : ACTAddress,
                            toAddressStr      : String,
                            serAddressStr     : String,
                            amount            : Double,
                            networkFee        : Double,
                            serviceFee        : Double,
                            completionHandler : SendCoinHandle) {
            completionHandler.completionHandler("", false, "TO DO")
    }

    private fun sendADACoin(fromAddress       : ACTAddress,
                            toAddressStr      : String,
                            serAddressStr     : String,
                            amount            : Double,
                            networkFee        : Double,
                            serviceFee        : Double,
                            completionHandler : SendCoinHandle) {
        val prvKeys     = privateKeys(ACTCoin.Cardano)  ?: arrayOf()
        val addresses   = addresses(ACTCoin.Cardano)    ?: arrayOf()
        if (prvKeys.isNotEmpty() and addresses.isNotEmpty() and (prvKeys.size == addresses.size)) {
            val unspentAddresses = addresses.map { it.rawAddressString() }.toTypedArray()
            Gada.shared.sendCoin(   prvKeys,
                                    unspentAddresses,
                                    fromAddress,
                                    toAddressStr,
                                    serAddressStr,
                                    amount,
                                    networkFee,
                                    serviceFee,
                completionHandler = object : ADASendCoinHandle {
                    override fun completionHandler(transID: String, success: Boolean, errStr: String) {
                        completionHandler.completionHandler(transID, success, errStr)
                    }
                })
        }else{
           completionHandler.completionHandler("", false, "")
        }
    }

    private fun sendXRPCoin(fromAddress       : ACTAddress,
                            toAddressStr      : String,
                            serAddressStr     : String,
                            amount            : Double,
                            networkFee        : Double,
                            serviceFee        : Double,
                            networkMemo       : MemoData?,
                            completionHandler : SendCoinHandle){
        val prvKeys = privateKeys(ACTCoin.Ripple) ?: return completionHandler.completionHandler("", false, "Not supported")
        val priKey  = prvKeys.first()
        val memo = if (networkMemo != null) XRPMemo(networkMemo!!.memo, networkMemo!!.destinationTag) else null
        Gxrp.shared.sendCoin(priKey, fromAddress, toAddressStr, amount, memo , object : XRPSubmitTxtHandle {
            override fun completionHandler(transID: String, success: Boolean, errStr: String) {
                completionHandler.completionHandler(transID, success, errStr)
            }
        })
    }
}