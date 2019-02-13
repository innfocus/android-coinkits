/*
 * Copyright (c) 2019.
 */

package tech.act.coinkits.bitcoin.model

data class UnspentOutputsItem(val tx_output_n: Long = 0,
                              val tx_hash: String = "",
                              val tx_hash_big_endian: String = "",
                              val tx_index: Long = 0,
                              val value_hex: String = "",
                              val confirmations: Long = 0,
                              val xpub: Xpub,
                              val value: Long = 0,
                              val script: String = "")


data class UnspentOutput(val unspent_outputs: List<UnspentOutputsItem>?)


data class Xpub(val path: String = "",
                val m: String = "")


