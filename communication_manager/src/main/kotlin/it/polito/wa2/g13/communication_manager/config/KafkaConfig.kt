package it.polito.wa2.g13.communication_manager.config

import it.polito.wa2.g13.communication_manager.configurations.KafkaConfigProperties
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.TopicConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.kafka.config.TopicBuilder

/**
 * Create Kafka topics, this is used during development. Topics
 * are actually created by running scripts inside the Kafka container.
 */
@Configuration
@Profile("dev")
class KafkaConfig(
    private val kafkaConfig: KafkaConfigProperties,
) {

    @Bean
    fun mailTopic() = NewTopic(kafkaConfig.mailTopic, 1, 1)

    @Bean
    fun attachmentTopic() = TopicBuilder.name(kafkaConfig.attachmentTopic)
        .partitions(1)
        .replicas(1)
        .configs(
            mapOf(
                TopicConfig.MAX_MESSAGE_BYTES_CONFIG to 10485760.toString(),
            )
        )
        .build()
}