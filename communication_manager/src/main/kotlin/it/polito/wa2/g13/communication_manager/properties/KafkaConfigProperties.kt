package it.polito.wa2.g13.communication_manager.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

/**
 * These properties are used to configure the topics to send
 * information on the kafka replicas.
 */
@ConfigurationProperties("kafka-config")
data class KafkaConfigProperties(
    /**
     * Topic of the mails in Kafka
     */
    @param:DefaultValue("mail.json")
    var mailTopic: String = "mail.json",
    /**
     * Topic of the attachments in Kafka
     */
    @param:DefaultValue("attachment.json")
    var attachmentTopic: String = "attachment.json",
)
