package it.polito.wa2.g13.communication_manager

import it.polito.wa2.g13.communication_manager.configurations.GmailConfigProperties
import it.polito.wa2.g13.communication_manager.configurations.KafkaConfigProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(
    GmailConfigProperties::class,
    KafkaConfigProperties::class,
)
class CommunicationManagerApplication

fun main(args: Array<String>) {
    runApplication<CommunicationManagerApplication>(*args)
}
