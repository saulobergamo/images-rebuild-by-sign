package br.com.utfpr.images.rebuild.by.sign.service

import br.com.utfpr.images.rebuild.by.sign.util.cgne
import br.com.utfpr.images.rebuild.by.sign.util.saveImage
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ImagesService(

) {
    private val logger = KotlinLogging.logger {}

    fun getImages(): String {
        return "TILT test"
    }

    fun processSign(
        documentNumber: String,
        csv: MultipartFile,
        size: Int
    ): String {
        logger.info { "processSign: processing file uploaded by user=$documentNumber" }

        try {
            cgne(csv).also {
                if (it != null) {
                    saveImage(it, size, "$documentNumber.png")
                }
            }
        }catch (e: Exception){
            logger.error { "Could not process sign and save image" }
        }

        return "Success".also {
            logger.info {
                "processSign: file received from user=$documentNumber"
            }
        }
    }


}