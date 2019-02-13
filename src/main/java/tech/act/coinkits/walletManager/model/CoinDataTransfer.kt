/*
 * Copyright (c) 2019.
 */

package tech.act.coinkits.walletManager.model

import java.io.Serializable

data class CoinDataTransfer(val fromAddress : String,
                            val toAddressStr : String,
                            val serAddressStr : String? = null,
                            val amount : Double = 0.0,
                            val networkFee : Double = 0.0,
                            val serviceFee : Double = 0.0,
                            val noServerFee : Boolean) : Serializable