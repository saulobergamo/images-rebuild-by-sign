package br.com.utfpr.images.rebuild.by.sign.model

data class EntrySignMessage(
    val clientId: String? = null,
    val documentNumber: String? = null,
    val entrySignDouble: List<Double>? = null
)
