package it.polito.wa2.g13.communication_manager.kafka

import it.polito.wa2.g13.communication_manager.properties.KafkaConfigProperties
import it.polito.wa2.g13.communication_manager.dtos.CreateAttachmentDTO
import it.polito.wa2.g13.communication_manager.dtos.CreateMessageDTO
import org.apache.camel.Body
import org.apache.camel.Header
import org.apache.camel.component.google.mail.stream.GoogleMailStreamConstants
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaSender(
    private val templateMessage: KafkaTemplate<String, CreateMessageDTO>,
    private val templateAttachment: KafkaTemplate<String, CreateAttachmentDTO>,
    private val kafkaConfigProperties: KafkaConfigProperties,
) {
    fun sendMessage(
        @Body message: CreateMessageDTO,
        @Header(GoogleMailStreamConstants.MAIL_ID) gmailId: String,
    ) {
        templateMessage.send(kafkaConfigProperties.mailTopic, gmailId, message)
    }

    fun sendAttachment(
        @Body attachment: CreateAttachmentDTO,
        @Header(GoogleMailStreamConstants.MAIL_ID) gmailId: String,
    ) {
        templateAttachment.send(kafkaConfigProperties.attachmentTopic, gmailId, attachment)
    }
}