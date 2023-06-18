package br.com.utfpr.images.rebuild.by.sign.service

import br.com.utfpr.images.rebuild.by.sign.enums.ImageSize
import br.com.utfpr.images.rebuild.by.sign.util.FILE_PATH
import br.com.utfpr.images.rebuild.by.sign.util.cgne2
import br.com.utfpr.images.rebuild.by.sign.util.saveImage
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ImagesService {
    private val logger = KotlinLogging.logger {}

    fun getImages(): String {
        return FILE_PATH
    }

    fun processSign(
        documentNumber: String,
        csv: MultipartFile,
        size: ImageSize
    ): String {
        logger.info { "processSign: processing file uploaded by user=$documentNumber" }



        try {
            cgne2(csv).also {
                saveImage(it, size, "$documentNumber.png")
            }
        } catch (e: Exception) {
            logger.error { "Could not process sign and save image" }
        }

        return "Success".also {
            logger.info {
                "processSign: file received from user=$documentNumber"
            }
        }
    }
}
