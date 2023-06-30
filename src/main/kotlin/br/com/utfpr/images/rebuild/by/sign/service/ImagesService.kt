package br.com.utfpr.images.rebuild.by.sign.service

import br.com.utfpr.images.rebuild.by.sign.model.EntrySignMessage
import br.com.utfpr.images.rebuild.by.sign.python.PythonWrapper
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
    companion object{
        const val PYTHON_PATH = "src/main/python/main.py"
    }
}
