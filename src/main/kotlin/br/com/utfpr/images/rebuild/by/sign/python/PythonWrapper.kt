package br.com.utfpr.images.rebuild.by.sign.python

import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class PythonWrapper {

    private val logger = KotlinLogging.logger {}
    fun executePythonScriptAsProcess(scriptPath: String, imageId: String?, signType: String?, userName: String?) {
        try {
            logger.info {
                "executePythonScriptAsProcess: try to run python process and " +
                    "rebuild image for user=$userName and imageId=$imageId"
            }
            val processBuilder = ProcessBuilder("python3", scriptPath, imageId, signType, userName)
            val process = processBuilder.start()

            val exitCode = process.waitFor()
            if (exitCode == 0) logger.info {
                "executePythonScriptAsProcess: end of python process - exit with success"
            } else {
                logger.warn { "executePythonScriptAsProcess: process python failed - exit=$exitCode" }
            }
        } catch (e: Exception) {
            logger.error(e) { "executePythonScriptAsProcess: error when starting python process" }
        }
    }
}
