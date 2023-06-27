package br.com.utfpr.images.rebuild.by.sign.python

import org.springframework.stereotype.Service

@Service
class PythonWrapper {
    fun executePythonScriptAsProcess(scriptPath: String, clientId: String?) {
        try {
            val processBuilder = ProcessBuilder("python3", scriptPath, clientId)
            val process = processBuilder.start()

            val exitCode = process.waitFor()
            println("O script Python foi executado com código de saída: $exitCode")
        } catch (e: Exception) {
            print("erro ao iniciar processo python")
        }
    }
}
