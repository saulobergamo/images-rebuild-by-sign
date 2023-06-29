package br.com.utfpr.images.rebuild.by.sign.service

import br.com.utfpr.images.rebuild.by.sign.model.EntrySignMessage
import br.com.utfpr.images.rebuild.by.sign.model.entity.EntrySign
import br.com.utfpr.images.rebuild.by.sign.python.PythonWrapper
import br.com.utfpr.images.rebuild.by.sign.repositories.EntrySignRepository
import br.com.utfpr.images.rebuild.by.sign.util.PYTHON_PATH
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class ImagesService(
    private val pythonWrapper: PythonWrapper,
    private val entrySignRepository: EntrySignRepository
) {
    private val logger = KotlinLogging.logger {}

    fun processSign(
        entrySignMessage: EntrySignMessage
    ) {
        logger.info { "processSign: processing file uploaded by user=${entrySignMessage.documentNumber}" }
//        val stringList = entrySignMessage.entrySignAsString?.split(";")
//        val output = stringList?.let { DoubleMatrix(it.size) }

        entrySignRepository.save(prepareData(entrySignMessage))

        pythonWrapper.executePythonScriptAsProcess(PYTHON_PATH, entrySignMessage.clientId)
    }

    private fun prepareData(entrySignMessage: EntrySignMessage) = EntrySign(
        clientId = entrySignMessage.clientId,
        documentNumber = entrySignMessage.documentNumber,
        entrySignDouble = entrySignMessage.entrySignDouble
    )
}
