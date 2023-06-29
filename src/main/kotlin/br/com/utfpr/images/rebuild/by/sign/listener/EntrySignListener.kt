package br.com.utfpr.images.rebuild.by.sign.listener

import br.com.utfpr.images.rebuild.by.sign.model.EntrySignMessage
import br.com.utfpr.images.rebuild.by.sign.service.ImagesService
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component

@Component
class EntrySignListener(
    private val imagesService: ImagesService,
    private val objectMapper: ObjectMapper
) {

    private val logger = KotlinLogging.logger {}

    @JmsListener(destination = "\${spring.activemq.queue.entry-sign}")
    fun receiveEntrySign(entrySignAsString: String) {
        lateinit var message: EntrySignMessage
        logger.info {
            "receiveEntrySign: parsing message"
        }
        try {
            message = objectMapper.readValue(
                entrySignAsString,
                EntrySignMessage::class.java
            )
            imagesService.processSign(message)
        } catch (e: Exception) {
            logger.error(e) {
                "receiveEntrySign: error while parsing message from queue"
            }
        }
    }
}
