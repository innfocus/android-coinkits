/*
 * Copyright (c) 2019.
 */

package tech.act.coinkits.bitcoin.model

data class BitcoinHistory(val addresses : List<XPubAddressModel>,val txs: List<TxsItem>?)
data class TxsItem(val ver: Long = 0,
                   val inputs: List<InputsItem>?,
                   val fee: Long = 0,
                   val weight: Long = 0,
                   val block_height: Long = 0,
                   val relayed_by: String = "",
                   val out: List<OutItem>?,
                   val lock_time: Long = 0,
                   val result: Long = 0,
                   val size: Long = 0,
                   val balance: Long = 0,
                   val double_spend: Boolean = false,
                   val time: Long = 0,
                   val tx_index: Long = 0,
                   val vin_sz: Long = 0,
                   val hash: String = "",
                   val vout_sz: Long = 0)
data class InputsItem(val sequence: Long = 0,
                      val witness: String = "",
                      val prev_out: PrevOut,
                      val script: String = "")
data class PrevOut(val spent: Boolean = false,
                   val txIndex: Long = 0,
                   val type: Long = 0,
                   val addr: String = "",
                   val value: Long = 0,
                   val n: Long = 0,
                   val script: String = "",
                   val xpub: Xpub?)
data class OutItem(val spent: Boolean = false,
                   val txIndex: Int = 0,
                   val type: Int = 0,
                   val addr: String = "",
                   val value: Int = 0,
                   val n: Int = 0,
                   val script: String = "",
                   val xpub: Xpub?)
data class XPubAddressModel(val finalBalance: Long = 0,
                            val address: String = "",
                            val account_index: Long = 0,
                            val n_tx: Long = 0,
                            val gap_limit: Long = 0,
                            val total_sent: Long = 0,
                            val total_received: Long = 0,
                            val change_index: Long = 0)