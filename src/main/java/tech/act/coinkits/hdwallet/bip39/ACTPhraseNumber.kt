package tech.act.coinkits.hdwallet.bip39

enum class ACTPhraseNumber(val value: Int) {
    Phrase12(12) {
        override fun bitsNumber(): Int      {return 128}
        override fun namePhrase(): String   {return "Phrase12"}
    },
    Phrase15(15){
        override fun bitsNumber(): Int      {return 160}
        override fun namePhrase(): String   {return "Phrase15"}
    },
    Phrase18(18){
        override fun bitsNumber(): Int      {return 192}
        override fun namePhrase(): String   {return "Phrase18"}
    },
    Phrase21(21){
        override fun bitsNumber(): Int      {return 224}
        override fun namePhrase(): String   {return "Phrase21"}
    },
    Phrase24(24){
        override fun bitsNumber(): Int      {return 256}
        override fun namePhrase(): String   {return "Phrase24"}
    };
    abstract fun bitsNumber()   : Int
    abstract fun namePhrase()   : String
    companion object {
        fun all(): Array<ACTPhraseNumber> = arrayOf(ACTPhraseNumber.Phrase12, ACTPhraseNumber.Phrase15, ACTPhraseNumber.Phrase18, ACTPhraseNumber.Phrase21, ACTPhraseNumber.Phrase24)
    }
}