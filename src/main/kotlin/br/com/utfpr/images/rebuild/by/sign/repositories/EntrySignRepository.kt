package br.com.utfpr.images.rebuild.by.sign.repositories

import br.com.utfpr.images.rebuild.by.sign.model.entity.EntrySign
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository

@Document
interface EntrySignRepository : MongoRepository<EntrySign, String> {
    fun findByDocumentNumber(documentNumber: String): EntrySign?
}
