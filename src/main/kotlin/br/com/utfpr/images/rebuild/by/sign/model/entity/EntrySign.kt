package br.com.utfpr.images.rebuild.by.sign.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document("entry_sign_to_rebuild_image")
class EntrySign(
    @Id
    var id: String? = null,
    @Indexed
    var clientId: String? = null,
    @Indexed
    var documentNumber: String? = null,
    var entrySignDouble: List<Double>? = null
)
