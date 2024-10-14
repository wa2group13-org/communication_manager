package it.polito.wa2.g13.communication_manager

import it.polito.wa2.g13.communication_manager.properties.GmailConfigProperties
import it.polito.wa2.g13.communication_manager.properties.KafkaConfigProperties
import it.polito.wa2.g13.communication_manager.properties.OpenapiConfigProperties
import it.polito.wa2.g13.communication_manager.properties.ProjectConfigProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(
    GmailConfigProperties::class,
    KafkaConfigProperties::class,
    OpenapiConfigProperties::class,
    ProjectConfigProperties::class,
)
class CommunicationManagerApplication

fun main(args: Array<String>) {
    runApplication<CommunicationManagerApplication>(*args)
}
