package br.com.utfpr.images.rebuild.by.sign.controller

import br.com.utfpr.images.rebuild.by.sign.service.ImagesService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@Tag(name = "Imgages rebuild by sign - CSM30")
@Validated
@RestController
@RequestMapping("/images/{documentNumber}")
class ImagesController(private val imagesService: ImagesService) {

    private val logger = KotlinLogging.logger { }

    @GetMapping("/download")
    @Operation(summary = "get images by user documentNumber")
    fun getImages(
        @PathVariable @Valid @NotBlank
        documentNumber: String
    ): String {
        logger.info { "getImages: getting images for user=$documentNumber" }
        return imagesService.getImages().also {
            logger.info { "getImages: success getting images for user=$documentNumber" }
        }
    }

    @PostMapping("/upload")
    @Operation(summary = "post csv document")
    fun postSignCSV(
        @RequestParam csv: MultipartFile,
        @PathVariable @Valid @NotBlank
        documentNumber: String,
        @RequestParam size: Int
    ): String {
        logger.info {
            "postSignCSV: processing file uploaded by user=$documentNumber\""
        }
        return imagesService.processSign(documentNumber, csv, size).also {
            logger.info {
                "processSign: file uploaded from user=$documentNumber"
            }
        }
    }
}
