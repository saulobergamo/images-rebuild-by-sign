package br.com.utfpr.images.rebuild.by.sign.service

import br.com.utfpr.images.rebuild.by.sign.model.EntrySignMessage
import br.com.utfpr.images.rebuild.by.sign.python.PythonWrapper
import br.com.utfpr.images.rebuild.by.sign.util.PYTHON_PATH
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class ImagesService(
    private val pythonWrapper: PythonWrapper
) {
    private val logger = KotlinLogging.logger {}

    fun processSign(
        entrySignMessage: EntrySignMessage
    ) {
        logger.info { "processSign: processing file uploaded by user=${entrySignMessage.userName}" }
        pythonWrapper.executePythonScriptAsProcess(
            PYTHON_PATH,
            entrySignMessage.imageId,
            entrySignMessage.signType,
            entrySignMessage.userName
        )
    }
}
